package com.example.eventra1.view.absen

import android.Manifest
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Geocoder
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModelProvider
import com.example.eventra1.R
import com.example.eventra1.utils.BitmapManager.bitmapToBase64
import com.example.eventra1.viewmodel.AbsenViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.location.LocationServices
import com.karumi.dexter.BuildConfig
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AbsenActivity : AppCompatActivity() {
    var REQ_CAMERA = 101
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absen)

        setInitLayout()
        setCurrentLocation()
        // setUploadData() akan dipanggil di dalam setInitLayout() setelah view diinisialisasi
    }

    private fun setCurrentLocation() {
        progressDialog.show()

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                                val addressList = geocoder.getFromLocation(strCurrentLatitude, strCurrentLongitude, 1)
                                if (addressList != null && addressList.isNotEmpty()) {
                                    return@withTimeout addressList[0].getAddressLine(0)
                                } else {
                                    Toast.makeText(applicationContext, "Gagal mendapatkan Lokasi", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@AbsenActivity, "Gagal mendapatkan alamat (timeout).", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                progressDialog.dismiss()
                Toast.makeText(this@AbsenActivity,
                    "Ups, gagal mendapatkan lokasi. Silahkan periksa GPS atau koneksi internet Anda!",
                    Toast.LENGTH_SHORT).show()
                strLatitude = "0"
                strLongitude = "0"
            }
        }
    }

    private fun setInitLayout() {
        progressDialog = ProgressDialog(this)
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
            val date = OnDateSetListener { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
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
            Dexter.withContext(this@AbsenActivity)
                .withPermissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
                            try {
                                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                cameraIntent.putExtra(
                                    "com.google.assistant.extra.USE_FRONT_CAMERA",
                                    true
                                )
                                cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
                                cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
                                cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1)

                                // Samsung
                                cameraIntent.putExtra("camerafacing", "front")
                                cameraIntent.putExtra("previous_mode", "front")

                                // Huawei
                                cameraIntent.putExtra("default_camera", "1")
                                cameraIntent.putExtra(
                                    "default_mode",
                                    "com.huawei.camera2.mode.photo.PhotoMode"
                                )

                                cameraIntent.putExtra(
                                    MediaStore.EXTRA_OUTPUT,
                                    FileProvider.getUriForFile(
                                        this@AbsenActivity,
                                        BuildConfig.APPLICATION_ID + ".provider",
                                        createImageFile()
                                    )
                                )
                                startActivityForResult(cameraIntent, REQ_CAMERA)
                            } catch (ex: IOException) {
                                Toast.makeText(this@AbsenActivity,
                                    "Ups, gagal membuka kamera", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest>,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).check()
        }
    }
    private fun setUploadData() {
        btnAbsen.setOnClickListener {
            val strNama = inputNama.text.toString()
            val strTanggal = inputTanggal.text.toString()
            val strKeterangan = inputKeterangan.text.toString()

            if (strFilePath.isEmpty() || strNama.isEmpty() || strCurrentLocation.isEmpty()
                || strTanggal.isEmpty() || strKeterangan.isEmpty()
            ) {
                Toast.makeText(this@AbsenActivity,
                    "Data tidak boleh ada yang kosong!", Toast.LENGTH_SHORT).show()
            } else {
                absenViewModel.addDataAbsen(
                    strBase64Photo,
                    strNama,
                    strTanggal,
                    strCurrentLocation,
                    strKeterangan
                )
                Toast.makeText(this@AbsenActivity,
                    "Laporan Anda terkirim, tunggu info selanjutnya ya!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        strTimeStamp = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        strImageName = "IMG_"
        fileDirectoty = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "")
        imageFilename = File.createTempFile(strImageName, ".jpg", fileDirectoty)
        strFilePath = imageFilename.absolutePath
        return imageFilename
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        convertImage(strFilePath)
    }

    private fun convertImage(imageFilePath: String?) {
        val imageFile = File(imageFilePath)
        if (imageFile.exists()) {
            val options = BitmapFactory.Options()
            var bitmapImage = BitmapFactory.decodeFile(imageFile.absolutePath, options)

            try {
                exifInterface = ExifInterface(imageFile.absolutePath)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            val matrix = Matrix()
            if (orientation == 6) {
                matrix.postRotate(90f)
            } else if (orientation == 3) {
                matrix.postRotate(180f)
            } else if (orientation == 8) {
                matrix.postRotate(270f)
            }

            bitmapImage = Bitmap.createBitmap(
                bitmapImage,
                0,
                0,
                bitmapImage.width,
                bitmapImage.height,
                matrix,
                true
            )

            val imageSelfie = findViewById<ImageView>(R.id.imageSelfie)
            if (bitmapImage == null) {
                Toast.makeText(this@AbsenActivity,
                    "Ups, foto kamu belum ada!", Toast.LENGTH_LONG).show()
            } else {
                val resizeImage = (bitmapImage.height * (512.0 / bitmapImage.width)).toInt()
                val scaledBitmap = Bitmap.createScaledBitmap(bitmapImage, 512, resizeImage, true)
                Glide.with(this)
                    .load(scaledBitmap)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_photo_camera)
                    .into(imageSelfie)
                strBase64Photo = bitmapToBase64(scaledBitmap)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                val intent = intent
                finish()
                startActivity(intent)
            }
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
        const val DATA_TITLE = "TITLE"
    }
}