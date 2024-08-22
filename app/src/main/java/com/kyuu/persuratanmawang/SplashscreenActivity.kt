package com.kyuu.persuratanmawang

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kyuu.persuratanmawang.auth.LoginActivity
import com.kyuu.persuratanmawang.data.User
import com.kyuu.persuratanmawang.databinding.ActivitySplashscreenBinding
import com.kyuu.persuratanmawang.kl.KlActivity
import com.kyuu.persuratanmawang.rt.RtActivity
import com.kyuu.persuratanmawang.rw.RwActivity

class SplashscreenActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 3000 // Durasi splash screen dalam milidetik (misalnya, 3000 = 3 detik)
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: ActivitySplashscreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Tambahkan animasi sederhana
        Log.d("SplashscreenActivity", "Animasi dimulai")
        binding.imageView.alpha = 0f
        binding.imageView.animate().alpha(1f).setDuration(1000).withEndAction {
            Log.d("SplashscreenActivity", "Animasi Selesai")
        }.start()

        // Handler untuk menunda navigasi ke LoginActivity
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserStatus()
        }, SPLASH_TIME_OUT)
    }

    private fun checkUserStatus() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = database.getReference("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        navigateToRoleSpecificActivity(user.role)
                    } else {
                        // Data user tidak ditemukan, arahkan ke halaman login
                        startActivity(Intent(this@SplashscreenActivity, LoginActivity::class.java))
                        finish()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Error dalam membaca data dari database, arahkan ke halaman login
                    startActivity(Intent(this@SplashscreenActivity, LoginActivity::class.java))
                    finish()
                }
            })
        } else {
            // Tidak ada pengguna yang terautentikasi, arahkan ke halaman login
            startActivity(Intent(this@SplashscreenActivity, LoginActivity::class.java))
            finish()
        }
    }

    private fun navigateToRoleSpecificActivity(role: String) {
        val intent = when (role) {
            "user" -> Intent(this, MainActivity::class.java)
            "admin" -> Intent(this, AdminMainActivity::class.java)
            "rt" -> Intent(this, RtActivity::class.java)
            "rw" -> Intent(this, RwActivity::class.java)
            "lingkungan" -> Intent(this, KlActivity::class.java)
            else -> {
                startActivity(Intent(this, LoginActivity::class.java))
                return
            }
        }
        startActivity(intent)
        finish()
    }
}