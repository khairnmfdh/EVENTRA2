data class Absensi(
    val status: String = "",
    val lokasi: String = "",
    val imageUrl: String = "",
    val timestamp: String = "",
    val uid: String = ""
) {
    companion object {
        fun toMap(modelAbsensi: Absensi): Map<String, Any> {
            return mapOf(
                "status" to modelAbsensi.status,
                "lokasi" to modelAbsensi.lokasi,
                "imageUrl" to modelAbsensi.imageUrl,
                "timestamp" to modelAbsensi.timestamp,
                "uid" to modelAbsensi.uid
            )
        }

        fun onDelete(absen: Any) {

        }
    }
}