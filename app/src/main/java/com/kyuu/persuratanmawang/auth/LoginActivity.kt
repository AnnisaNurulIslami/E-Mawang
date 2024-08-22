package com.kyuu.persuratanmawang.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kyuu.persuratanmawang.AdminMainActivity
import com.kyuu.persuratanmawang.MainActivity
import com.kyuu.persuratanmawang.R
import com.kyuu.persuratanmawang.data.User
import com.kyuu.persuratanmawang.databinding.ActivityLoginBinding
import com.kyuu.persuratanmawang.kl.KlActivity
import com.kyuu.persuratanmawang.rt.RtActivity
import com.kyuu.persuratanmawang.rw.RwActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        ref = FirebaseDatabase.getInstance().reference

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginFirebase(email, password)
            } else {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvHere.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        // Memuat gambar menggunakan Glide
        Glide.with(this)
            .load(R.drawable.logincuate)  // Ganti dengan URL atau ID resource gambar Anda
            .override(800, 600)            // Ukuran yang diinginkan
            .into(binding.logoImage);

        binding.tvForgetPassword.setOnClickListener {
            val intent = Intent(this, ForgotPassActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: ""
                val userRef = database.getReference("users").child(userId)

                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val user = dataSnapshot.getValue(User::class.java)
                        if (user != null) {
                            navigateToRoleSpecificActivity(user.role)
                        } else {
                            Toast.makeText(this@LoginActivity, "User data not found", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@LoginActivity, "Database error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this@LoginActivity, "Failed to login: ${exception.message}", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Unknown role", Toast.LENGTH_SHORT).show()
                return
            }
        }
        startActivity(intent)
        Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            auth.signOut() // Sign out the user to ensure they re-enter credentials
        }
    }
}
