package com.kyuu.persuratanmawang.admin.notifications

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kyuu.persuratanmawang.adapter.UserAdapter
import com.kyuu.persuratanmawang.auth.CreateAccountAdmin
import com.kyuu.persuratanmawang.auth.LoginActivity
import com.kyuu.persuratanmawang.data.UserRole
import com.kyuu.persuratanmawang.databinding.FragmentNotificationsAdminBinding

class NotificationsAdminFragment : Fragment() {
    private var _binding: FragmentNotificationsAdminBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var rtAdapter: UserAdapter
    private lateinit var rwAdapter: UserAdapter
    private lateinit var klAdapter: UserAdapter

    private val rtUsers = mutableListOf<UserRole>()
    private val rwUsers = mutableListOf<UserRole>()
    private val klUsers = mutableListOf<UserRole>()

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsAdminViewModel::class.java)

        _binding = FragmentNotificationsAdminBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNotifications
        notificationsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        database = FirebaseDatabase.getInstance().getReference("users")
        auth = FirebaseAuth.getInstance()

        binding.buttonLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            Toast.makeText(requireContext(), "Success Logout", Toast.LENGTH_SHORT).show()
        }

        binding.buttonNewAccount.setOnClickListener {
            val intent = Intent(requireContext(), CreateAccountAdmin::class.java)
            startActivity(intent)
        }

        setupRecyclerViews()
        fetchUsers()

        return root
    }

    private fun setupRecyclerViews() {
        rtAdapter = UserAdapter(requireContext(), rtUsers, ::confirmUpdateUser, ::confirmDeleteUser)
        binding.rtRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.rtRecyclerView.adapter = rtAdapter

        rwAdapter = UserAdapter(requireContext(), rwUsers, ::confirmUpdateUser, ::confirmDeleteUser)
        binding.rwRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.rwRecyclerView.adapter = rwAdapter

        klAdapter = UserAdapter(requireContext(), klUsers, ::confirmUpdateUser, ::confirmDeleteUser)
        binding.klRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.klRecyclerView.adapter = klAdapter
    }

    private fun fetchUsers() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                rtUsers.clear()
                rwUsers.clear()
                klUsers.clear()

                for (userSnapshot in dataSnapshot.children) {
                    val user = userSnapshot.getValue(UserRole::class.java)
                    if (user != null) {
                        user.uid = userSnapshot.key!!
                        Log.d("FirebaseData", "User: $user")
                        when (user.role) {
                            "rt" -> rtUsers.add(user)
                            "rw" -> rwUsers.add(user)
                            "lingkungan" -> klUsers.add(user)
                        }
                    }
                }

                Log.d("UserData", "RT Users: $rtUsers")
                Log.d("UserData", "RW Users: $rwUsers")
                Log.d("UserData", "KL Users: $klUsers")

                rtAdapter.notifyDataSetChanged()
                rwAdapter.notifyDataSetChanged()
                klAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun confirmUpdateUser(user: UserRole, name: String, email: String, rt: String, rw: String, lingkungan: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Update User")
            .setMessage("Are you sure you want to update this user?")
            .setPositiveButton("Yes") { dialog, _ ->
                updateUser(user, name, email, rt, rw, lingkungan)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun updateUser(user: UserRole, name: String, email: String, rt: String, rw: String, lingkungan: String) {
        val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
        val userUpdates = mapOf<String, Any>(
            "email" to email,
            "lingkungan" to lingkungan,
            "name" to name,
            "rt" to rt,
            "rw" to rw,
        )
        Log.d("UpdateUser", "Updating user with uid: ${user.uid}")
        database.child(user.uid).updateChildren(userUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "User updated successfully", Toast.LENGTH_SHORT).show()
                fetchUsers()
            } else {
                Toast.makeText(requireContext(), "Failed to update user", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDeleteUser(user: UserRole) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete this user?")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteUser(user)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteUser(user: UserRole) {
        val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
        Log.d("DeleteUser", "Deleting user with uid: ${user.uid}")
        database.child(user.uid).removeValue().addOnSuccessListener {
            Toast.makeText(requireContext(), "User deleted successfully", Toast.LENGTH_SHORT).show()
            fetchUsers()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to delete user", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}