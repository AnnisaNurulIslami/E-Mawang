package com.kyuu.persuratanmawang.rt.adapter

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kyuu.persuratanmawang.R
import com.kyuu.persuratanmawang.data.PermohonanTes
import com.kyuu.persuratanmawang.databinding.ItemPermohonanBinding
import com.kyuu.persuratanmawang.ui.DetailFormActivity

class PermohonanRtAdapter(private var permohonanList: List<PermohonanTes>, ) :
    RecyclerView.Adapter<PermohonanRtAdapter.PermohonanViewHolder>() {

    private var filteredList: MutableList<PermohonanTes> = ArrayList(permohonanList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermohonanViewHolder {
        val binding = ItemPermohonanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PermohonanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PermohonanViewHolder, position: Int) {
        val permohonan = permohonanList[position]
        holder.bind(permohonan)
    }

    override fun getItemCount() = permohonanList.size

    fun updateList(newList: MutableList<PermohonanTes>) {
        permohonanList = newList
        filteredList = ArrayList(newList)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            ArrayList(permohonanList)
        } else {
            val resultList = permohonanList.filter {
                it.name.contains(query, ignoreCase = true)
            }
            ArrayList(resultList)
        }
        notifyDataSetChanged()
    }

    class PermohonanViewHolder(private val binding: ItemPermohonanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(permohonan: PermohonanTes) {
            binding.tvLetterType.text = permohonan.letter
            binding.tvApplicantName.text = permohonan.name
            binding.tvUploadDate.text = permohonan.uploadDate
            //binding.tvStatus.text = "Menunggu Persetujuan RT"
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
                val intent = Intent(itemView.context, DetailFormActivity::class.java).apply {
                    putExtra(DetailFormActivity.UID, permohonan.uid)
                    putExtra(DetailFormActivity.NAME, permohonan.name)
                    putExtra(DetailFormActivity.NIK, permohonan.nik)
                    putExtra(DetailFormActivity.TTL, permohonan.ttl)
                    putExtra(DetailFormActivity.RELIGION, permohonan.religion)
                    putExtra(DetailFormActivity.JOB, permohonan.job)
                    putExtra(DetailFormActivity.STATUS, permohonan.status)
                    putExtra(DetailFormActivity.ADDRESS, permohonan.address)
                    putExtra(DetailFormActivity.LETTER, permohonan.letter)
                    putStringArrayListExtra(DetailFormActivity.FILEURLS, ArrayList(permohonan.fileUrls))
                    putExtra(DetailFormActivity.RT, permohonan.rt)
                    putExtra(DetailFormActivity.RW, permohonan.rw)
                    putExtra(DetailFormActivity.LINGKUNGAN, permohonan.lingkungan)
                    putExtra(DetailFormActivity.UNIQUE_ID, permohonan.uniqueId)
                }
                itemView.context.startActivity(intent)
            }
        }
    }
}