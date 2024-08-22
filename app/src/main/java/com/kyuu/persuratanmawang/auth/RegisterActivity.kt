package com.kyuu.persuratanmawang.auth

import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kyuu.persuratanmawang.data.User
import com.kyuu.persuratanmawang.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var rolesRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        rolesRef = database.getReference("roles")

        val rtValues = arrayOf("01", "02", "03", "04", "05", "06", "07", "08", "09", "10")
        val rwValues = arrayOf("01", "02", "03", "04")
        val lingkunganValues = arrayOf("Buttadidi", "Biring Balang")

        val rtAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, rtValues)
        rtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRt.adapter = rtAdapter

        val rwAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, rwValues)
        rwAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRw.adapter = rwAdapter

        val lingkunganAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, lingkunganValues)
        lingkunganAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLingkungan.adapter = lingkunganAdapter

        // Memuat gambar menggunakan Glide
        Glide.with(this)
            .load(com.kyuu.persuratanmawang.R.drawable.registercuate)  // Ganti dengan URL atau ID resource gambar Anda
            .override(800, 600)            // Ukuran yang diinginkan
            .into(binding.logoImage);

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val rt = binding.spinnerRt.selectedItem.toString()
            val rw = binding.spinnerRw.selectedItem.toString()
            val lingkungan = binding.spinnerLingkungan.selectedItem.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() &&
                rt.isNotEmpty() && rw.isNotEmpty() && lingkungan.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                val user = User(name, email, rt, rw, lingkungan, password, "user")
                                database.reference.child("users").child(userId).setValue(user)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(this, "Registration successful.", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Toast.makeText(this, "Failed to register user data.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Registration failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvHere.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}