package com.example.eventra1.view.absen

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.example.eventra1.R
import com.example.eventra1.utils.BitmapManager.bitmapToBase64
import com.example.eventra1.viewmodel.AbsenViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.location.LocationServices
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.BuildConfig
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.*
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AbsenActivity : AppCompatActivity() {
    var REQ_GALLERY = 1001
    var strCurrentLatitude = 0.0
    var strCurrentLongitude = 0.0
    var strFilePath: String = ""
    var strLatitude = "0"
    var strLongitude = "0"
    lateinit var fileDirectoty: File
    lateinit var imageFilename: File
    lateinit var exifInterface: ExifInterface
    lateinit var strBase64Photo: String
    lateinit var strCurrentLocation: String
    lateinit var strTitle: String
    lateinit var strTimeStamp: String
    lateinit var strImageName: String
    lateinit var absenViewModel: AbsenViewModel
    lateinit var progressDialog: ProgressDialog

    // Deklarasi view sebagai lateinit
    private lateinit var btnAbsen: com.google.android.material.button.MaterialButton
    private lateinit var inputNama: EditText
    private lateinit var inputTanggal: EditText
    private lateinit var inputKeterangan: EditText
    private lateinit var storageReference: FirebaseStorage
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absen)

        storageReference = FirebaseStorage.getInstance()

        setInitLayout()
        setCurrentLocation()
        // setUploadData() akan dipanggil di dalam setInitLayout() setelah view diinisialisasi

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImageUri = result.data?.data
                if (selectedImageUri != null) {
                    uploadImageToFirebase(selectedImageUri)
                } else {
                    Toast.makeText(this, "Gagal memilih gambar", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun setCurrentLocation() {
        progressDialog.show()

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            progressDialog.dismiss()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                strCurrentLatitude = location.latitude
                strCurrentLongitude = location.longitude

                // Jalankan geocoding di coroutine dengan timeout
                CoroutineScope(Dispatchers.Main).launch {
                    val address = withContext(Dispatchers.IO) {
                        try {
                            withTimeout(3000L) {  // timeout 3 detik
                                val geocoder = Geocoder(this@AbsenActivity, Locale.getDefault())
                                val addressList = geocoder.getFromLocation(
                                    strCurrentLatitude,
                                    strCurrentLongitude,
                                    1
                                )
                                if (addressList != null && addressList.isNotEmpty()) {
                                    return@withTimeout addressList[0].getAddressLine(0)
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "Gagal mendapatkan Lokasi",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: TimeoutCancellationException) {
                            // Timeout
                            null
                        } catch (e: IOException) {
                            e.printStackTrace()
                            null
                        }
                    }
                    progressDialog.dismiss()

                    if (address != null) {
                        strCurrentLocation = address.toString()
                        findViewById<EditText>(R.id.inputLokasi).setText(strCurrentLocation)
                    } else {
                        Toast.makeText(
                            this@AbsenActivity,
                            "Gagal mendapatkan alamat (timeout).",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                progressDialog.dismiss()
                Toast.makeText(
                    this@AbsenActivity,
                    "Ups, gagal mendapatkan lokasi. Silahkan periksa GPS atau koneksi internet Anda!",
                    Toast.LENGTH_SHORT
                ).show()
                strLatitude = "0"
                strLongitude = "0"
            }
        }
    }

    private fun setInitLayout() {
        progressDialog = ProgressDialog(this)
        val DATA_TITLE = "TITLE";
        strTitle = intent.extras?.getString(DATA_TITLE).toString()

        if (strTitle != null) {
            val tvTitle = findViewById<TextView>(R.id.tvTitle)
            tvTitle.text = strTitle
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        // Inisialisasi View
        btnAbsen = findViewById(R.id.btnAbsen)
        inputNama = findViewById(R.id.inputNama)
        inputTanggal = findViewById(R.id.inputTanggal)
        inputKeterangan = findViewById(R.id.inputKeterangan)

        // Panggil setUploadData setelah inisialisasi View
        setUploadData()

        val inputTanggal = findViewById<EditText>(R.id.inputTanggal)
        inputTanggal.setOnClickListener {
            val tanggalAbsen = Calendar.getInstance()
            val date =
                OnDateSetListener { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    tanggalAbsen.set(Calendar.YEAR, year)
                    tanggalAbsen.set(Calendar.MONTH, monthOfYear)
                    tanggalAbsen.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val strFormatDefault = "dd MMMM yyyy HH:mm"
                    val simpleDateFormat = SimpleDateFormat(strFormatDefault, Locale.getDefault())
                    inputTanggal.setText(simpleDateFormat.format(tanggalAbsen.time))
                }
            DatePickerDialog(
                this@AbsenActivity, date,
                tanggalAbsen.get(Calendar.YEAR),
                tanggalAbsen.get(Calendar.MONTH),
                tanggalAbsen.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val layoutImage = findViewById<LinearLayout>(R.id.layoutImage)

        layoutImage.setOnClickListener {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            // Cek dan minta izin pakai ActivityCompat
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery() // lanjut buka galeri
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
            }

            Dexter.withContext(this@AbsenActivity)
                .withPermission(permission)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        val galleryIntent = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                        startActivityForResult(galleryIntent, REQ_GALLERY)
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        Toast.makeText(
                            this@AbsenActivity,
                            "Izin dibutuhkan untuk mengakses galeri",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).check()
        }
}

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    fun setUploadData() {
        btnAbsen.setOnClickListener {
            val strNama = inputNama.text.toString()
            val strTanggal = inputTanggal.text.toString()
            val strKeterangan = inputKeterangan.text.toString()

            if (strFilePath.isEmpty() || strNama.isEmpty() || strCurrentLocation.isEmpty()
                || strTanggal.isEmpty() || strKeterangan.isEmpty()
            ) {
                Toast.makeText(
                    this@AbsenActivity,
                    "Data tidak boleh ada yang kosong!", Toast.LENGTH_SHORT
                ).show()
            } else {
                // MENGIRIM URL, BUKAN BASE64
                absenViewModel.addDataAbsen(
                    strBase64Photo,  // SEKARANG INI ADALAH URL
                    strNama,
                    strTanggal,
                    strCurrentLocation,
                    strKeterangan
                )
                Toast.makeText(
                    this@AbsenActivity,
                    "Laporan Anda terkirim, tunggu info selanjutnya ya!", Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val fileName = "images/${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(fileName)

        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uriDownload ->
                val downloadUrl = uriDownload.toString()
                Log.d("FIREBASE_URL", "Download URL: $downloadUrl")

                strBase64Photo = downloadUrl // Gunakan variabel ini untuk dikirim ke server
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Upload gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }




    @Throws(IOException::class)
    fun createImageFile(): File {
        strTimeStamp =
            SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        strImageName = "IMG_"
        fileDirectoty =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "")
        imageFilename = File.createTempFile(strImageName, ".jpg", fileDirectoty)
        strFilePath = imageFilename.absolutePath
        return imageFilename
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == REQ_GALLERY) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                try {
                    val inputStream = contentResolver.openInputStream(selectedImageUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    val imageSelfie = findViewById<ImageView>(R.id.imageSelfie)
                    val resizeImage = (bitmap.height * (512.0 / bitmap.width)).toInt()
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 512, resizeImage, true)

                    Glide.with(this)
                        .load(scaledBitmap)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_photo_camera)
                        .into(imageSelfie)

                    val baos = ByteArrayOutputStream()
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val imageData = baos.toByteArray()

                    val storageRef = storageReference.reference
                    val imageRef = storageRef.child("absensi/${UUID.randomUUID()}.jpg")

                    val uploadTask = imageRef.putBytes(imageData)

                    uploadTask.addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            strBase64Photo = imageUrl // Ganti dengan URL dari Firebase
                            strFilePath = "uploaded_to_firebase"
                            Toast.makeText(this, "Upload foto berhasil", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(this, "Upload foto gagal", Toast.LENGTH_SHORT).show()
                    }


                    // Simpan path dummy agar lolos validasi
                    strFilePath = "from_gallery"

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Gagal memuat gambar dari galeri", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            Toast.makeText(this, "Izin dibutuhkan untuk mengakses galeri", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        val DATA_TITLE = "TITLE"
    }
}
