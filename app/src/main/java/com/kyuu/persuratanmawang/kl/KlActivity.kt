package com.kyuu.persuratanmawang.kl

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kyuu.persuratanmawang.auth.LoginActivity
import com.kyuu.persuratanmawang.data.PermohonanTes
import com.kyuu.persuratanmawang.databinding.ActivityKlBinding
import com.kyuu.persuratanmawang.rt.adapter.PermohonanRtAdapter

class KlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKlBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var permohonanAdapter: PermohonanRtAdapter
    private lateinit var permohonanList: MutableList<PermohonanTes>

    private var userLingkungan: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityKlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val klViewModel =
            ViewModelProvider(this).get(KlViewModel::class.java)

        val textView: TextView = binding.textHome
        klViewModel.text.observe(this) {
            textView.text = it
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("requests")
        permohonanList = mutableListOf()

        permohonanAdapter = PermohonanRtAdapter(permohonanList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = permohonanAdapter

        // Load user RT information first, then load permohonan data

        binding.buttonLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            Toast.makeText(this, "Success Logout", Toast.LENGTH_SHORT).show()
        }
        loadUserLingkungan()
    }

    private fun loadUserLingkungan() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userLingkungan= snapshot.child("lingkungan").value as? String
                if (userLingkungan != null) {
                    Log.d("RtActivity", "User RT: $userLingkungan")
                    loadPermohonanData(userLingkungan!!)
                } else {
                    Log.e("RtActivity", "User RT is not available")
                    Toast.makeText(this@KlActivity, "User Kepala Lingkungan is not available", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RtActivity", "Failed to load user RT", error.toException())
                //Toast.makeText(this@KlActivity, "Failed to load user RT", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadPermohonanData(lingkungan: String) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                permohonanList.clear()
                for (uidSnapshot in snapshot.children) {
                    Log.d("RtActivity", "UID Snapshot: ${uidSnapshot.key}")
                    for (permohonanSnapshot in uidSnapshot.children) {
                        try {
                            val permohonan = permohonanSnapshot.getValue(PermohonanTes::class.java)
                            if (permohonan?.lingkungan == lingkungan && permohonan?.status == "Menunggu Persetujuan Kepala Lingkungan") {
                                permohonanList.add(permohonan)
                                Log.d("RtActivity", "Permohonan added: $permohonan")
                            }
                        } catch (e: DatabaseException) {
                            Log.e("RtActivity", "Error converting snapshot to PermohonanTes", e)
                        }
                    }
                }
                permohonanAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RtActivity", "Failed to load data", error.toException())
            }
        })
    }
}