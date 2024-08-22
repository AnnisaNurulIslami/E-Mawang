package com.kyuu.persuratanmawang.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kyuu.persuratanmawang.R
import com.kyuu.persuratanmawang.data.PermohonanTes

class NotificationListener: Service() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var userRt: String? = null
    private var lastNotified: String? = null

    override fun onCreate() {
        super.onCreate()
        database = FirebaseDatabase.getInstance().reference.child("request")
        auth = FirebaseAuth.getInstance()
        loadUserRt()

    }

    private fun loadUserRt() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userRt = snapshot.child("rt").value as? String
                if (userRt != null) {
                    Log.d("NotificationService", "User RT: $userRt")
                    setupDatabaseListener(userRt!!)
                } else {
                    Log.e("NotificationService", "User RT is not available")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationService", "Failed to load user RT", error.toException())
            }
        })
    }

    private fun setupDatabaseListener(rt: String) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (uidSnapshot in snapshot.children) {
                    for (permohonanSnapshot in uidSnapshot.children) {
                        try {
                            val permohonan = permohonanSnapshot.getValue(PermohonanTes::class.java)
                            if (permohonan?.rt == rt && permohonan?.status == "Menunggu Persetujuan RT") {
                                Log.d("NotificationService", "Permohonan added: $permohonan")
//                                if (permohonan.notificationStatus == "Ada surat yang memerlukan persetujuan RT") {
//                                    showNotification("Ada Permohonan Baru", "Ada surat yang memerlukan persetujuan RT")
//                                }
                                if (lastNotified != permohonanSnapshot.key) {
                                    showNotification("Ada Permohonan Baru", "Ada surat yang memerlukan persetujuan RT")
                                    lastNotified = permohonanSnapshot.key
                                }
                            }
                        } catch (e: DatabaseException) {
                            Log.e("NotificationService", "Error converting snapshot to PermohonanTes", e)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationService", "Failed to load data", error.toException())
            }
        })
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "notification_channel_id"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

