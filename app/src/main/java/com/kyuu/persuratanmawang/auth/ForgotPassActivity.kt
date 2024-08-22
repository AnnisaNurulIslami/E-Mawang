package com.kyuu.persuratanmawang.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.kyuu.persuratanmawang.databinding.ActivityForgotPassBinding

class ForgotPassActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPassBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgotPassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnReset.setOnClickListener {
            sendPasswordResetEmail()
        }

        // Load image using Glide
        Glide.with(this)
            .load(com.kyuu.persuratanmawang.R.drawable.forgotcuate)
            .override(800, 600)
            .into(binding.logoImage)

        binding.tvHere.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun sendPasswordResetEmail() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                // Navigate to login screen or close this activity
                finish()
            } else {
                Toast.makeText(this, "Failed to send password reset email", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
