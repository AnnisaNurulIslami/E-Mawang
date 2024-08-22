package com.kyuu.persuratanmawang.rw

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
import com.kyuu.persuratanmawang.databinding.ActivityRwBinding
import com.kyuu.persuratanmawang.rt.adapter.PermohonanRtAdapter

class RwActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRwBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var permohonanAdapter: PermohonanRtAdapter
    private lateinit var permohonanList: MutableList<PermohonanTes>
    private lateinit var permohonanListener: ValueEventListener

    private var userRw: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRwBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rwViewModel = ViewModelProvider(this).get(RwViewModel::class.java)

        val textView: TextView = binding.textHome
        rwViewModel.text.observe(this) {
            textView.text = it
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("requests")
        permohonanList = mutableListOf()

        permohonanAdapter = PermohonanRtAdapter(permohonanList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = permohonanAdapter

        binding.buttonLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            Toast.makeText(this, "Success Logout", Toast.LENGTH_SHORT).show()
        }

        loadUserRw()
    }

    private fun loadUserRw() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userRw = snapshot.child("rw").value as? String
                if (userRw != null) {
                    Log.d("RwActivity", "User RW: $userRw")
                    loadPermohonanData(userRw!!)
                } else {
                    Log.e("RwActivity", "User RW is not available")
                    Toast.makeText(this@RwActivity, "User RW is not available", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RwActivity", "Failed to load user RW", error.toException())
                Toast.makeText(this@RwActivity, "Failed to load user RW", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadPermohonanData(rw: String) {
        if (auth.currentUser == null) {
            Log.e("RwActivity", "User is not authenticated")
            return
        }

        permohonanListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                permohonanList.clear()
                for (uidSnapshot in snapshot.children) {
                    Log.d("RwActivity", "UID Snapshot: ${uidSnapshot.key}")
                    for (permohonanSnapshot in uidSnapshot.children) {
                        try {
                            val permohonan = permohonanSnapshot.getValue(PermohonanTes::class.java)
                            if (permohonan?.rw == rw && permohonan?.status == "Menunggu Persetujuan RW") {
                                permohonanList.add(permohonan)
                                Log.d("RwActivity", "Permohonan added: $permohonan")
                            }
                        } catch (e: DatabaseException) {
                            Log.e("RwActivity", "Error converting snapshot to PermohonanTes", e)
                        }
                    }
                }
                permohonanAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RwActivity", "Failed to load data", error.toException())
            }
        }
        database.addValueEventListener(permohonanListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the listener to avoid leaks or errors
        database.removeEventListener(permohonanListener)
    }
}
