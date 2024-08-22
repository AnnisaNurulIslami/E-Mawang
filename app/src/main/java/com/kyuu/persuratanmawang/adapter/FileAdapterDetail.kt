package com.kyuu.persuratanmawang.adapter

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kyuu.persuratanmawang.databinding.ItemDownloadBinding
import com.kyuu.persuratanmawang.databinding.ItemFileBinding
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FileAdapterDetail(
    private val fileUris: List<Uri>,
    private val context: Context
) : RecyclerView.Adapter<FileAdapterDetail.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(fileUris[position])
    }

    override fun getItemCount(): Int = fileUris.size

    inner class FileViewHolder(private val binding: ItemDownloadBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri) {
            binding.fileName.text = uri.lastPathSegment
            binding.removeFileButton.setOnClickListener {
                downloadFile(uri)
            }
        }

        private fun downloadFile(uri: Uri) {
            val request = DownloadManager.Request(uri)
                .setTitle(uri.lastPathSegment)
                .setDescription("Downloading file...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uri.lastPathSegment)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        }
    }
}