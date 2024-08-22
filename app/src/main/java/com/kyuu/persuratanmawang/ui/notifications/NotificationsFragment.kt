package com.kyuu.persuratanmawang.ui.notifications

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kyuu.persuratanmawang.auth.ForgotPassActivity
import com.kyuu.persuratanmawang.auth.LoginActivity
import com.kyuu.persuratanmawang.data.UserNotification
import com.kyuu.persuratanmawang.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNotifications
        notificationsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.buttonChangePassword.setOnClickListener {
            val intent = Intent(requireContext(), ForgotPassActivity::class.java)
            startActivity(intent)
        }

        binding.buttonDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }

        loadUserData()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.buttonLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            Toast.makeText(requireContext(), "Success Logout", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteAccountConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Hapus Akun")
        builder.setMessage("Apakah Anda yakin ingin menghapus akun ini? Tindakan ini tidak dapat dibatalkan.")
        builder.setPositiveButton("Ya") { dialog, _ ->
            deleteAccount()
            dialog.dismiss()
        }
        builder.setNegativeButton("Tidak") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun deleteAccount() {
        val user = auth.currentUser
        user?.let {
            val uid = it.uid

            // Hapus data pengguna dari Realtime Database
            database.child("users").child(uid).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Jika data berhasil dihapus, hapus akun pengguna dari Firebase Authentication
                        it.delete()
                            .addOnCompleteListener { deleteTask ->
                                if (deleteTask.isSuccessful) {
                                    Toast.makeText(context, "Akun berhasil dihapus", Toast.LENGTH_SHORT).show()
                                    // Alihkan pengguna ke layar login atau tindakan lain yang diperlukan
                                    val intent = Intent(context, LoginActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(context, "Gagal menghapus akun: ${deleteTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Gagal menghapus data pengguna: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } ?: run {
            Toast.makeText(context, "Pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = database.child("users").child(uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserNotification::class.java)
                user?.let {
                    binding.tampilanNama.text = it.name
                    binding.tampilanEmail.text = it.email
                    binding.tampilanRt.text = it.rt
                    binding.tampilanRw.text = it.rw
                    binding.tampilanLingkungan.text = it.lingkungan
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}