package com.kyuu.persuratanmawang.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.kyuu.persuratanmawang.databinding.ActivityInfoBinding

class InfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInfoBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Memuat gambar menggunakan Glide
        Glide.with(this)
            .load(com.kyuu.persuratanmawang.R.drawable.documentcuate)
            .override(800, 600)
            .into(binding.logoImage);

        binding.btnMengerti.setOnClickListener {
            finish()
        }
    }
}