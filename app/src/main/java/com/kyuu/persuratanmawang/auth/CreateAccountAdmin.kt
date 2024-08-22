package com.kyuu.persuratanmawang.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kyuu.persuratanmawang.AdminMainActivity
import com.kyuu.persuratanmawang.R
import com.kyuu.persuratanmawang.data.User
import com.kyuu.persuratanmawang.databinding.ActivityCreateAccountAdminBinding

class CreateAccountAdmin : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAccountAdminBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateAccountAdminBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val roleDisplayValues = arrayOf("RT", "RW", "Kepala Lingkungan")
        val roleSaveValues = arrayOf("rt", "rw", "lingkungan")
        val rtValues = arrayOf("01", "02", "03", "04", "05", "06", "07", "08", "09", "10")
        val rwValues = arrayOf("01", "02", "03", "04")
        val lingkunganValues = arrayOf("Buttadidi", "Biring Balang")

        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roleDisplayValues)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = roleAdapter

        val rtAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, rtValues)
        rtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRt.adapter = rtAdapter

        val rwAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, rwValues)
        rwAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRw.adapter = rwAdapter

        val lingkunganAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lingkunganValues)
        lingkunganAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLingkungan.adapter = lingkunganAdapter

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
            val selectedRoleDisplay = binding.spinnerRole.selectedItem.toString()
            val selectedIndex = roleDisplayValues.indexOf(selectedRoleDisplay)
            val roleToSave = roleSaveValues[selectedIndex]

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() &&
                roleToSave.isNotEmpty() && rt.isNotEmpty() && rw.isNotEmpty() && lingkungan.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                val user = User(name, email, rt, rw, lingkungan, password, roleToSave)
                                database.reference.child("users").child(userId).setValue(user)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(this, "Registration successful.", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this@CreateAccountAdmin, AdminMainActivity::class.java)
                                            intent.putExtra("openFragment", "NotificationsAdminFragment")
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

        setContentView(binding.root)

        binding.tvHere.setOnClickListener {
            finish()
        }
    }
}