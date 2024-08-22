package com.kyuu.persuratanmawang.adapter

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kyuu.persuratanmawang.R
import com.kyuu.persuratanmawang.data.PermohonanTes
import com.kyuu.persuratanmawang.databinding.ItemPermohonanBinding
import com.kyuu.persuratanmawang.ui.detail.DetailActivityUser

class PermohonanAdapter(private val permohonanList: List<PermohonanTes>) :
    RecyclerView.Adapter<PermohonanAdapter.PermohonanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermohonanViewHolder {
        val binding = ItemPermohonanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PermohonanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PermohonanViewHolder, position: Int) {
        val permohonan = permohonanList[position]
        holder.bind(permohonan)
    }

    override fun getItemCount() = permohonanList.size

    class PermohonanViewHolder(private val binding: ItemPermohonanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(permohonan: PermohonanTes) {
            binding.tvLetterType.text = permohonan.letter
            binding.tvApplicantName.text = permohonan.name
            binding.tvUploadDate.text = permohonan.uploadDate
            binding.tvStatus.text = permohonan.status
            when (permohonan.status) {
                "Menunggu Persetujuan RT" -> {
                    binding.tvStatus.setTextColor(Color.WHITE)
                    binding.tvStatus.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.galaticWonder))
                }
                "Menunggu Persetujuan RW" -> {
                    binding.tvStatus.setTextColor(Color.WHITE)
                    binding.tvStatus.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.prestigeMauve))
                }
                "Menunggu Persetujuan Kepala Lingkungan" -> {
                    binding.tvStatus.setTextColor(Color.WHITE)
                    binding.tvStatus.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.cafeNoir))
                }
                "Surat Siap Diambil" -> {
                    binding.tvStatus.setTextColor(Color.WHITE)
                    binding.tvStatus.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.chestnutGreen))
                }
                "Permohonan Ditolak" -> {
                    binding.tvStatus.setTextColor(Color.WHITE)
                    binding.tvStatus.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.armyGreen))
                }
                "Surat Sedang Dibuat" -> {
                    binding.tvStatus.setTextColor(Color.WHITE)
                    binding.tvStatus.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.lees))
                }
                "Menunggu Verifikasi Admin" -> {
                    binding.tvStatus.setTextColor(Color.WHITE)
                    binding.tvStatus.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.rainStorm))
                }
                else -> {
                    binding.tvStatus.setTextColor(Color.BLACK) // Warna default, misalnya hitam
                }
            }
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailActivityUser::class.java).apply {
                    putExtra(DetailActivityUser.NAME, permohonan.name)
                    putExtra(DetailActivityUser.NIK, permohonan.nik)
                    putExtra(DetailActivityUser.TTL, permohonan.ttl)
                    putExtra(DetailActivityUser.RELIGION, permohonan.religion)
                    putExtra(DetailActivityUser.JOB, permohonan.job)
                    putExtra(DetailActivityUser.ADDRESS, permohonan.address)
                    putExtra(DetailActivityUser.LETTER, permohonan.letter)
                    putStringArrayListExtra(DetailActivityUser.FILEURLS, ArrayList(permohonan.fileUrls))
                    putExtra(DetailActivityUser.STATUS, permohonan.status)
                    putExtra(DetailActivityUser.RT, permohonan.rt)
                    putExtra(DetailActivityUser.RW, permohonan.rw)
                    putExtra(DetailActivityUser.LINGKUNGAN, permohonan.lingkungan)
                    putExtra(DetailActivityUser.ALASANPENOLAKAN, permohonan.alasanPenolakan)
                    putExtra(DetailActivityUser.UNIQUE_ID, permohonan.uniqueId)
                }
                itemView.context.startActivity(intent)
            }
        }
    }
}