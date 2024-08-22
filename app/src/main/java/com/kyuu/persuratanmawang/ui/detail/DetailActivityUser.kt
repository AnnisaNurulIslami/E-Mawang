package com.kyuu.persuratanmawang.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kyuu.persuratanmawang.MainActivity
import com.kyuu.persuratanmawang.adapter.FileAdapterDetail
import com.kyuu.persuratanmawang.databinding.ActivityDetailUserBinding

class DetailActivityUser : AppCompatActivity() {

    private lateinit var binding: ActivityDetailUserBinding
    private val fileUrls = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailUserBinding.inflate(layoutInflater)

        setContentView(binding.root)

        Glide.with(this)
            .load(com.kyuu.persuratanmawang.R.drawable.documentcuate)
            .override(800, 600)
            .into(binding.logoImage);

        val nameWarga = intent.getStringExtra(NAME)
        val nikWarga = intent.getStringExtra(NIK)
        val ttlWarga = intent.getStringExtra(TTL)
        val religionWarga = intent.getStringExtra(RELIGION)
        val jobWarga = intent.getStringExtra(JOB)
        val addressWarga = intent.getStringExtra(ADDRESS)
        val letterWarga = intent.getStringExtra(LETTER)
        val fileUrlWarga = intent.getStringArrayListExtra(FILEURLS) ?: arrayListOf()
        Log.d("DOKUMEN WARGA", "$fileUrlWarga")
        val rtWarga = intent.getStringExtra(RT)
        val rwWarga = intent.getStringExtra(RW)
        val lingkunganWarga = intent.getStringExtra(LINGKUNGAN)
        val alasanPenolakan = intent.getStringExtra(ALASANPENOLAKAN)
        val statusSurat = intent.getStringExtra(STATUS)

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
            notes.text = alasanPenolakan

            if (statusSurat == "Permohonan Ditolak") {
                notes.text = alasanPenolakan
                notes.visibility = View.VISIBLE
                penolakanLabel.visibility = View.VISIBLE
                textNotes.visibility = View.VISIBLE
                btnCancel.text = "Hapus Permohonan"
            } else {
                notes.visibility = View.GONE
                penolakanLabel.visibility = View.GONE
                textNotes.visibility = View.GONE
                btnCancel.text = "Batalkan Permohonan"
            }

            fileUrlWarga.let {
                fileUrls
            }

            val fileUrls = intent.getStringArrayListExtra(FILEURLS)?.map { Uri.parse(it) } ?: emptyList()
            recyclerViewFiles.layoutManager = LinearLayoutManager(this@DetailActivityUser)
            recyclerViewFiles.adapter = FileAdapterDetail(fileUrls, this@DetailActivityUser)
        }

        binding.btnCancel.setOnClickListener {
            deleteRequest()
        }
    }

    private fun deleteRequest() {
        val uniqueId = intent.getStringExtra(UNIQUE_ID)?.trim()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser?.uid?.trim()

        val database = FirebaseDatabase.getInstance().reference
            .child("requests")
            .child(currentUser!!)

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (permohonanSnapshot in snapshot.children) {
                    val uniqueIdPermohonan = permohonanSnapshot.child("uniqueId").getValue(String::class.java)
                    val currentUniqueId = uniqueId
                    if (uniqueIdPermohonan != null && currentUniqueId != null && uniqueIdPermohonan in currentUniqueId) {
                        permohonanSnapshot.ref.removeValue()
                            .addOnSuccessListener {
                                // Data berhasil dihapus
                                Log.d("Firebase", "Permohonan dengan uniqueId '$uniqueIdPermohonan' berhasil dihapus.")
                                Toast.makeText(this@DetailActivityUser, "Permohonan Dibatalkan", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@DetailActivityUser, MainActivity::class.java)
                                intent.putExtra("openFragment", "HomeFragment")
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { exception ->
                                // Gagal menghapus data
                                Log.e("Firebase", "Gagal menghapus permohonan: ${exception.message}")
                            }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Gagal mendapatkan data
                Log.e("Firebase", "Gagal mendapatkan data: ${error.message}")
            }
        })
    }

    companion object {
        const val NAME = "NAME"
        const val NIK = "NIK"
        const val TTL = "TTL"
        const val RELIGION = "RELIGION"
        const val JOB = "JOB"
        const val ADDRESS = "ADDRESS"
        const val LETTER = "LETTER"
        const val FILEURLS = "FILEURLS"
        const val STATUS = "STATUS"
        const val RT = "RT"
        const val RW = "RW"
        const val LINGKUNGAN = "LINGKUNGAN"
        const val ALASANPENOLAKAN = "ALASANPENOLAKAN"
        const val UNIQUE_ID = "uniqueId"
    }
}