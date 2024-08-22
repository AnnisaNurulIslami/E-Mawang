package com.kyuu.persuratanmawang.ui

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.internal.StatusCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.TextAlignment
import com.kyuu.persuratanmawang.AdminMainActivity
import com.kyuu.persuratanmawang.adapter.FileAdapterDetail
import com.kyuu.persuratanmawang.data.PermohonanTes
import com.kyuu.persuratanmawang.databinding.ActivityDetailFormBinding
import com.kyuu.persuratanmawang.kl.KlActivity
import com.kyuu.persuratanmawang.rt.RtActivity
import com.kyuu.persuratanmawang.rw.RwActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DetailFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailFormBinding
    private lateinit var role: String
    private lateinit var database: DatabaseReference
    private val fileUrls = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(this)
            .load(com.kyuu.persuratanmawang.R.drawable.documentcuate)
            .override(800, 600)
            .into(binding.logoImage)

        val nameWarga = intent.getStringExtra(NAME)
        val nikWarga = intent.getStringExtra(NIK)
        val ttlWarga = intent.getStringExtra(TTL)
        val religionWarga = intent.getStringExtra(RELIGION)
        val jobWarga = intent.getStringExtra(JOB)
        val addressWarga = intent.getStringExtra(ADDRESS)
        val letterWarga = intent.getStringExtra(LETTER)
        val fileUrlWarga = intent.getStringArrayListExtra(FILEURLS) ?: arrayListOf()
        val rtWarga = intent.getStringExtra(RT)
        val rwWarga = intent.getStringExtra(RW)
        val lingkunganWarga = intent.getStringExtra(LINGKUNGAN)

        binding.apply {
            tampilanNama.text = nameWarga
            tampilanNIK.text = nikWarga
            tampilanRt.text = rtWarga
            tampilanRw.text = rwWarga
            tampilanJob.text = jobWarga
            tampilanAddress.text = addressWarga
            tampilanLingkungan.text = lingkunganWarga
            tampilanReligion.text = religionWarga
            tampilanTTL.text = ttlWarga
            tampilanLetter.text = letterWarga

            val fileUrls = intent.getStringArrayListExtra(FILEURLS)?.map { Uri.parse(it) } ?: emptyList()
            recyclerViewFiles.layoutManager = LinearLayoutManager(this@DetailFormActivity)
            recyclerViewFiles.adapter = FileAdapterDetail(fileUrls, this@DetailFormActivity)
        }

        database = FirebaseDatabase.getInstance().reference

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            val userId = it.uid
            database.child("users").child(userId).child("role")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        role = snapshot.getValue(String::class.java) ?: ""

                        val currentStatus = intent.getStringExtra(STATUS) ?: ""
                        val statusSurat = intent.getStringExtra(STATUS) ?: ""
                        if (role == "admin") {
                            when (currentStatus) {
                                "Menunggu Verifikasi Admin" -> {
                                    binding.btnDone.visibility = View.GONE
                                    binding.btnAccept.setOnClickListener {
                                        generatePdfAndUpload()
                                        handleSetujuClick()
                                    }
                                }
                                "Surat Sedang Dibuat" -> {
                                    binding.tvAcceptReject.text =
                                        "Klik 'Selesai' untuk memberitahu warga bahwa surat yang dimohonkan telah selesai dan dapat diambil di kantor kelurahan."
                                    binding.notes.visibility = View.GONE
                                    binding.btnDone.visibility = View.VISIBLE
                                    binding.btnAcceptReject.visibility =View.GONE
                                    binding.btnDone.setOnClickListener {
                                        handleSetujuClick()
                                    }
                                } else -> return
                            }
                        }
                        else if (role == "rt" || role == "rw" || role == "lingkungan") {
                            binding.btnDone.visibility = View.GONE
                            binding.btnAccept.setOnClickListener {
                                handleSetujuClick()
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@DetailFormActivity, "Gagal mengambil peran pengguna", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        binding.btnReject.setOnClickListener {
            handleTolakClick()
        }

        checkWriteExternalStoragePermission()
    }

    private fun handleSetujuClick() {
        val uniqueId = intent.getStringExtra(UNIQUE_ID) ?: return
        Log.d("HANDLE_SETUJU", "NIK: $uniqueId, Role: $role")
        if (uniqueId.isEmpty()) {
            showToast("NIK tidak tersedia.")
            return
        }

        getCurrentStatus(uniqueId, object : StatusCallback {
            override fun onStatusReceived(currentStatus: String?) {
                var statusBaru: String? = null

                when (role) {
                    "rt" -> {
                        statusBaru = "Menunggu Persetujuan RW"
                        val intent = Intent(this@DetailFormActivity, RtActivity::class.java)
                        startActivity(intent)
                    }
                    "rw" -> {
                        statusBaru = "Menunggu Persetujuan Kepala Lingkungan"
                        val intent = Intent(this@DetailFormActivity, RwActivity::class.java)
                        startActivity(intent)
                    }
                    "lingkungan" -> {
                        statusBaru = "Menunggu Verifikasi Admin"
                        val intent = Intent(this@DetailFormActivity, KlActivity::class.java)
                        startActivity(intent)
                    }
                    "admin" -> {
                        statusBaru = when (currentStatus) {
                            "Menunggu Verifikasi Admin" -> "Surat Sedang Dibuat"
                            "Surat Sedang Dibuat" -> "Surat Siap Diambil"
                            else -> {
                                showToast("Status tidak dikenali atau tidak bisa diubah.")
                                return
                            }
                        }
                        val intent = Intent(this@DetailFormActivity, AdminMainActivity::class.java)
                        intent.putExtra("openFragment", "HomeAdminFragment")
                        startActivity(intent)
                        finish()
                    }
                    else -> {
                        showToast("Role tidak dikenali atau tidak memiliki izin untuk menyetujui.")
                        return
                    }
                }

                if (statusBaru != null) {
                    Log.d("HANDLE_SETUJU", "Status baru: $statusBaru")
                    updateStatus(uniqueId, statusBaru)
                }
            }

            override fun onError(error: DatabaseError) {
                Log.e("DATABASE_ERROR", "Error accessing database: ${error.message}")
                showToast("Gagal mengambil status permohonan: ${error.message}")
            }
        })
    }

    private fun handleTolakClick() {
        val uniqueId = intent.getStringExtra(UNIQUE_ID)
        if (uniqueId == null) {
            showToast("NIK tidak tersedia.")
            Log.e("HANDLE_TOLAK", "uniqueId is null")
            return
        }
        Log.d("NIK WARGA", "NIK Received: $uniqueId")

        val alasanPenolakan = binding.notes.text.toString().trim()
        if (alasanPenolakan.isEmpty()) {
            showToast("Harap berikan alasan penolakan.")
            return
        }

        updateStatus(uniqueId, "Permohonan Ditolak", alasanPenolakan)

        when (role) {
            "rt" -> {
                val intent = Intent(this@DetailFormActivity, RtActivity::class.java)
                startActivity(intent)
            }
            "rw" -> {
                val intent = Intent(this@DetailFormActivity, RwActivity::class.java)
                startActivity(intent)
            }
            "lingkungan" -> {
                val intent = Intent(this@DetailFormActivity, KlActivity::class.java)
                startActivity(intent)
            }
            "admin" -> {
                val intent = Intent(this@DetailFormActivity, AdminMainActivity::class.java)
                intent.putExtra("openFragment", "HomeAdminFragment")
                startActivity(intent)
                finish()
            }
            else -> {
                showToast("Role tidak dikenali atau tidak memiliki izin untuk menyetujui.")
                return
            }
        }
    }

    private fun getCurrentStatus(uniqueId: String, callback: StatusCallback) {
        database.child("requests").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var currentStatus: String? = null
                for (uidSnapshot in snapshot.children) {
                    for (requestsSnapshot in uidSnapshot.children) {
                        val requests = requestsSnapshot.getValue(PermohonanTes::class.java)
                        if (requests?.uniqueId == uniqueId) {
                            currentStatus = requests.status
                            break
                        }
                    }
                    if (currentStatus != null) break
                }
                callback.onStatusReceived(currentStatus)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error)
            }
        })
    }

    interface StatusCallback {
        fun onStatusReceived(status: String?)
        fun onError(error: DatabaseError)
    }

    private fun updateStatus(uniqueId: String, status: String, alasanPenolakan: String? = null) {
        Log.d("UPDATE_STATUS", "Updating status for NIK: $uniqueId to $status with alasan: $alasanPenolakan")
        database.child("requests").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isUpdated = false

                for (uidSnapshot in snapshot.children) {
                    for (requestsSnapshot in uidSnapshot.children) {
                        try {
                            val requests = requestsSnapshot.getValue(PermohonanTes::class.java)
                            if (requests?.uniqueId == uniqueId) {
                                val uid = requestsSnapshot.key
                                if (uid == null) {
                                    Log.e("PERMOHONAN_FOUND", "UID is null for uniqueId: $uniqueId")
                                    continue
                                }
                                Log.d("PERMOHONAN_FOUND", "Permohonan found with UID: $uid")
                                val updates = mutableMapOf<String, Any>(
                                    "status" to status
                                )

                                if (status == "Permohonan Ditolak") {
                                    updates["alasanPenolakan"] = alasanPenolakan ?: ""
                                }

                                Log.d("UPDATE_STATUS", "Updates Map: $updates")

                                try {
                                    database.child("requests").child(uidSnapshot.key!!).child(uid).updateChildren(updates)
                                        .addOnSuccessListener {
                                            Log.d("UPDATE_SUCCESS", "Status updated successfully.")
                                            showToast("Status permohonan berhasil diperbarui.")
                                            // Hanya finish() jika admin
                                            if (role == "admin") {
                                                finish()
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e("UPDATE_FAILURE", "Failed to update status: ${exception.message}")
                                            showToast("Gagal memperbarui status permohonan: ${exception.message}")
                                        }
                                } catch (e: Exception) {
                                    Log.e("UPDATE_STATUS_ERROR", "Exception during updateChildren: ${e.message}")
                                    showToast("Terjadi kesalahan saat memperbarui status.")
                                }
                                isUpdated = true
                                break
                            }
                        } catch (e: DatabaseException) {
                            Log.e("PERMOHONAN_ERROR", "Error converting snapshot to PermohonanTes", e)
                        }
                    }
                    if (isUpdated) break
                }

                if (!isUpdated) {
                    showToast("Permohonan dengan NIK tersebut tidak ditemukan.")
                    Log.e("PERMOHONAN_NOT_FOUND", "Permohonan with NIK $uniqueId not found.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Gagal mencari permohonan: ${error.message}")
                Log.e("DATABASE_ERROR", "Error accessing database: ${error.message}")
            }
        })

        Log.d("UPDATE_STATUS_FINISHED", "updateStatus method executed for role: $role")
    }

    private fun generatePdfAndUpload() {
        val nameWarga = intent.getStringExtra(NAME) ?: "Nama Tidak Tersedia"
        val nikWarga = intent.getStringExtra(NIK) ?: "NIK Tidak Tersedia"
        val ttlWarga = intent.getStringExtra(TTL) ?: "TTL Tidak Tersedia"
        val religionWarga = intent.getStringExtra(RELIGION) ?: "Agama Tidak Tersedia"
        val jobWarga = intent.getStringExtra(JOB) ?: "Pekerjaan Tidak Tersedia"
        val addressWarga = intent.getStringExtra(ADDRESS) ?: "Alamat Tidak Tersedia"
        val letterWarga = intent.getStringExtra(LETTER) ?: "Surat Tidak Tersedia"
        val rtWarga = intent.getStringExtra(RT) ?: "Surat Tidak Tersedia"
        val rwWarga = intent.getStringExtra(RW) ?: "Surat Tidak Tersedia"
        val lingkunganWarga = intent.getStringExtra(LINGKUNGAN) ?: "Lingkungan Tidak Tersedia"

        val currentDate = SimpleDateFormat("yyyy_MM_dd_mm_ss", Locale.getDefault()).format(Date())
        val sanitizedNameWarga = nameWarga.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val pdfPath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath + "/surat_${currentDate}_${sanitizedNameWarga}$.pdf"

        //val pdfPath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath + "/surat_$nikWarga.pdf"

        try {
            val placeholderQrCodeImage = generateQRCode("https://placeholder.url")

            createPdf(pdfPath, nameWarga, nikWarga, ttlWarga, religionWarga, jobWarga, addressWarga, letterWarga, rtWarga ,rwWarga ,lingkunganWarga, placeholderQrCodeImage)

            val storageReference = FirebaseStorage.getInstance().reference
            val pdfFile = File(pdfPath)
            val pdfUri = Uri.fromFile(pdfFile)
            val pdfRef = storageReference.child("surat/${pdfFile.name}")

            pdfRef.putFile(pdfUri)
                .addOnSuccessListener {
                    pdfRef.downloadUrl.addOnSuccessListener { uri ->
                        val actualQrCodeImage = generateQRCode(uri.toString())

                        createPdf(pdfPath, nameWarga, nikWarga, ttlWarga, religionWarga, jobWarga, addressWarga, letterWarga, rtWarga, rwWarga, lingkunganWarga, actualQrCodeImage)

                        savePdfToDownloads(pdfPath)

                        pdfRef.putFile(pdfUri)
                            .addOnSuccessListener {
                                //showToast("PDF berhasil dicetak")
                                Log.d("PDF_UPLOAD", "PDF successfully uploaded to Firebase Storage.")
                            }
                            .addOnFailureListener { e ->
                                showToast("Gagal mengunggah PDF: ${e.message}")
                                Log.e("PDF_UPLOAD_ERROR", "Failed to upload PDF: ${e.message}")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    showToast("Gagal mengunggah PDF: ${e.message}")
                    Log.e("PDF_UPLOAD_ERROR", "Failed to upload PDF: ${e.message}")
                }
        } catch (e: Exception) {
            showToast("Gagal membuat PDF: ${e.message}")
            Log.e("PDF_CREATION_ERROR", "Failed to create PDF: ${e.message}")
        }
    }

    private fun createPdf(
        pdfPath: String,
        nameWarga: String,
        nikWarga: String,
        ttlWarga: String,
        religionWarga: String,
        jobWarga: String,
        addressWarga: String,
        letterWarga: String,
        rt: String,
        rw: String,
        lingkungan: String,
        qrCodeImage: Image
    ) {
        val writer = PdfWriter(pdfPath)
        val pdf = PdfDocument(writer)
        val document = Document(pdf, PageSize.A4)

        val title = Paragraph("SURAT PENGANTAR")
            .setFont(PdfFontFactory.createFont())
            .setFontSize(18f)
            .setTextAlignment(TextAlignment.CENTER)
        document.add(title)

        document.add(Paragraph("ORGANISASI RUKUN TETANGGA (ORT $rt)")
            .setTextAlignment(TextAlignment.CENTER))
        document.add(Paragraph("RUKUN WARGA (RW $rw) LINGKUNGAN BUTTADIDI")
            .setTextAlignment(TextAlignment.CENTER))
        document.add(Paragraph("KELURAHAN MAWANG, KECAMATAN SOMBA OPU, KABUPATEN GOWA")
            .setTextAlignment(TextAlignment.CENTER))

        val nomorSurat = generateNomorSurat(this, rt, rw)
        document.add(Paragraph(nomorSurat).setMarginTop(20f))

        document.add(Paragraph("Yang bertanda tangan di bawah ini Ketua ORT $rt ORW $rw menerangkan bahwa :")
            .setMarginTop(10f))

        document.add(Paragraph("Nama: $nameWarga"))
        document.add(Paragraph("NIK: $nikWarga"))
        document.add(Paragraph("Tempat/Tgl Lahir: $ttlWarga"))
        document.add(Paragraph("Agama: $religionWarga"))
        document.add(Paragraph("Pekerjaan: $jobWarga"))
        document.add(Paragraph("Alamat sekarang: $addressWarga"))

        // Add purpose paragraph
        document.add(Paragraph("Benar nama tersebut di atas adalah penduduk dalam wilayah ORT $rt ORW $rw Lingkungan Buttadidi Kelurahan Mawang Kecamatan Somba Opu bermohon surat pengantar untuk mengurus surat di Kantor Lurah berupa $letterWarga.")
            .setMarginTop(10f))

        // Add closing paragraph
        document.add(Paragraph("Demikian Surat Pengantar ini diberikan untuk mendapatkan pelayanan dengan sebaik-baiknya dan untuk dipergunakan sebagaimana mestinya.")
            .setMarginTop(20f))

        // Add date
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
        val currentDate = dateFormat.format(Date())
        document.add(Paragraph("Mawang, $currentDate").setMarginTop(30f)).setFixedPosition(450f, 0f, 350f)

        // Add Ketua RT and Ketua RW at specified positions
        document.add(Paragraph("Ketua RT $rt").setFixedPosition(200f, 200f, 200f))
        document.add(Paragraph("Ketua RW $rw").setFixedPosition(450f, 200f, 200f))

        // Add Mengetahui and Kepala Lingkungan
        //document.add(Paragraph("Mengetahui").setFixedPosition(335f, 110f, 200f))
        document.add(Paragraph("Kepala Lingkungan $lingkungan").setFixedPosition(280f, 90f, 300f))

        // Adding QR code at specific coordinates and resizing it
        qrCodeImage.setFixedPosition(20f, 150f)
        qrCodeImage.setHeight(100f) // Set height
        qrCodeImage.setWidth(100f) // Set width
        document.add(qrCodeImage)

        document.close()
    }

    private fun generateQRCode(content: String): Image {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val imageData = ImageDataFactory.create(byteArrayOutputStream.toByteArray())
        return Image(imageData)
    }

    private fun generateNomorSurat(context: Context, rt: String, rw: String): String {
        val dateFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val sharedPref = context.getSharedPreferences("NomorSuratPrefs", Context.MODE_PRIVATE)
        val lastNumber = sharedPref.getInt("lastNumber", 0)
        val newNumber = lastNumber + 1

        with(sharedPref.edit()) {
            putInt("lastNumber", newNumber)
            apply()
        }

        return "NO  :  ${String.format("%03d", newNumber)}/ORT $rt/ORW $rw/KM/$currentDate"
    }

    private fun savePdfToDownloads(pdfPath: String) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val pdfFile = File(pdfPath)
            val destinationFile = File(downloadsDir, pdfFile.name)

            Files.copy(pdfFile.toPath(), destinationFile.toPath())

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "pdf_download_channel"
            val channelName = "PDF Downloads"
            val importance = NotificationManager.IMPORTANCE_HIGH

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, channelName, importance)
                notificationManager.createNotificationChannel(channel)
            }

            val pdfUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", destinationFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(pdfUri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Unduhan Berhasil")
                .setContentText("File ${pdfFile.name} telah berhasil diunduh")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            notificationManager.notify(1, notificationBuilder.build())

            Log.d("PDF_DOWNLOAD", "PDF successfully saved to Downloads folder and notification created.")
        } catch (e: Exception) {
            showToast("Gagal menyimpan PDF: ${e.message}")
            Log.e("PDF_DOWNLOAD_ERROR", "Failed to save PDF to Downloads: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Izin penyimpanan diberikan.")
            } else {
                //showToast("Izin penyimpanan ditolak.")
            }
        }
    }

    companion object {
        const val UID = "UID"
        const val NAME = "NAME"
        const val NIK = "NIK"
        const val TTL = "TTL"
        const val RELIGION = "RELIGION"
        const val JOB = "JOB"
        const val ADDRESS = "ADDRESS"
        const val LETTER = "LETTER"
        const val FILEURLS = "FILEURLS"
        const val RT = "RT"
        const val RW = "RW"
        const val LINGKUNGAN = "LINGKUNGAN"
        const val UNIQUE_ID = "UNIQUE_ID"
        const val STATUS = "STATUS"
        const val REQUEST_WRITE_STORAGE = 1
    }
}
