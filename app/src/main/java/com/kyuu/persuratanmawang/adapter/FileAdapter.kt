package com.kyuu.persuratanmawang.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kyuu.persuratanmawang.databinding.ItemFileBinding

class FileAdapter(
    private val fileUris: List<Uri>,
    private val removeFileCallback: (Uri) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(fileUris[position])
    }

    override fun getItemCount(): Int = fileUris.size

    inner class FileViewHolder(private val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri) {
            binding.fileName.text = uri.lastPathSegment
            binding.removeFileButton.setOnClickListener {
                removeFileCallback(uri)
            }
        }
    }
}