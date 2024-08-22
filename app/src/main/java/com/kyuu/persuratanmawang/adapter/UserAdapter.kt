package com.kyuu.persuratanmawang.adapter

import android.R
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kyuu.persuratanmawang.data.UserRole
import com.kyuu.persuratanmawang.databinding.DialogUserDetailBinding
import com.kyuu.persuratanmawang.databinding.ItemRoleBinding

class UserAdapter(
    private val context: Context,
    private val userList: MutableList<UserRole>,
    private val onUpdate: (UserRole, String, String, String, String, String) -> Unit,
    private val onDelete: (UserRole) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemRoleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserRole) {
            binding.userName.text = user.name
            binding.editButton.setOnClickListener { showDetailDialog(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemRoleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size

    private fun showDetailDialog(user: UserRole) {
        val dialogBinding = DialogUserDetailBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .create()

        val rtValues = arrayOf("01", "02", "03", "04", "05", "06", "07", "08", "09", "10")
        val rwValues = arrayOf("01", "02", "03", "04")
        val lingkunganValues = arrayOf("Buttadidi", "Biring Balang")

        val rtAdapter = ArrayAdapter(context, R.layout.simple_spinner_item, rtValues)
        rtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerUserRtDetail.adapter = rtAdapter

        val rwAdapter = ArrayAdapter(context, R.layout.simple_spinner_item, rwValues)
        rwAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerUserRwDetail.adapter = rwAdapter

        val lingkunganAdapter = ArrayAdapter(context, R.layout.simple_spinner_item, lingkunganValues)
        lingkunganAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerUserLingkunganDetail.adapter = lingkunganAdapter

        dialogBinding.editUserNameDetail.setText(user.name)
        dialogBinding.editUserEmailDetail.setText(user.email)
        dialogBinding.spinnerUserRtDetail.setSelection(rtValues.indexOf(user.rt))
        dialogBinding.spinnerUserRwDetail.setSelection(rwValues.indexOf(user.rw))
        dialogBinding.spinnerUserLingkunganDetail.setSelection(lingkunganValues.indexOf(user.lingkungan))

        dialogBinding.buttonUpdate.setOnClickListener {
            val updatedName = dialogBinding.editUserNameDetail.text.toString()
            val updatedEmail = dialogBinding.editUserEmailDetail.text.toString()
            val updatedRt = dialogBinding.spinnerUserRtDetail.selectedItem.toString()
            val updatedRw = dialogBinding.spinnerUserRwDetail.selectedItem.toString()
            val updatedLingkungan = dialogBinding.spinnerUserLingkunganDetail.selectedItem.toString()

            if (updatedName.isNotEmpty() && updatedEmail.isNotEmpty() && updatedRt.isNotEmpty() && updatedRw.isNotEmpty() && updatedLingkungan.isNotEmpty()) {
                onUpdate(user, updatedName, updatedEmail, updatedRt, updatedRw, updatedLingkungan)
                dialog.dismiss()
            }
        }

        dialogBinding.buttonDelete.setOnClickListener {
            onDelete(user)
            dialog.dismiss()
        }

        dialog.show()
    }
}
