package com.kyuu.persuratanmawang.rt

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kyuu.persuratanmawang.R
import com.kyuu.persuratanmawang.auth.LoginActivity
import com.kyuu.persuratanmawang.data.PermohonanTes
import com.kyuu.persuratanmawang.databinding.ActivityRtBinding
import com.kyuu.persuratanmawang.rt.adapter.PermohonanRtAdapter
import com.kyuu.persuratanmawang.utils.NotificationListener

class RtActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRtBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var permohonanAdapter: PermohonanRtAdapter
    private lateinit var permohonanList: MutableList<PermohonanTes>
    private lateinit var permohonanListener: ValueEventListener

    private var userRt: String? = null
    private var userRw: String? = null
    private var userLingkungan: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRtBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rtViewModel = ViewModelProvider(this).get(RtViewModel::class.java)

        val textView: TextView = binding.textHome
        rtViewModel.text.observe(this) {
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

        loadUserRt()
        //startNotificationService() // Start the notification service
    }

    private fun loadUserRt() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userRt = snapshot.child("rt").value as? String
                userRw = snapshot.child("rw").value as? String
                userLingkungan = snapshot.child("lingkungan").value as? String

                if (userRt != null) {
                    Log.d("RtActivity", "User RT: $userRt")
                    loadPermohonanData(userRt!!, userRw!!, userLingkungan!!)
                } else {
                    Log.e("RtActivity", "User RT is not available")
                    Toast.makeText(this@RtActivity, "User RT is not available", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RtActivity", "Failed to load user RT", error.toException())
                Toast.makeText(this@RtActivity, "Failed to load user RT", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadPermohonanData(rt: String, rw: String, lingkungan: String) {
        if (auth.currentUser == null) {
            Log.e("RtActivity", "User is not authenticated")
            return
        }

        permohonanListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                permohonanList.clear()
                for (uidSnapshot in snapshot.children) {
                    Log.d("RtActivity", "UID Snapshot: ${uidSnapshot.key}")
                    for (permohonanSnapshot in uidSnapshot.children) {
                        try {
                            val permohonan = permohonanSnapshot.getValue(PermohonanTes::class.java)
                            if (permohonan?.rt == rt && permohonan?.rw == rw && permohonan.lingkungan == lingkungan && permohonan?.status == "Menunggu Persetujuan RT") {
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
        }
        database.addValueEventListener(permohonanListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the listener to avoid leaks or errors
        database.removeEventListener(permohonanListener)
    }

    private fun startNotificationService() {
        val intent = Intent(this, NotificationListener::class.java)
        startService(intent)
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "notification_channel_id"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Channel name", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_notifications_active_24) // Ganti dengan ikon notifikasi yang sesuai
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(1, notificationBuilder.build())
    }
}
