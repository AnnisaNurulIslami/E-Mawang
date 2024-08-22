package com.kyuu.persuratanmawang.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kyuu.persuratanmawang.data.PermohonanTes
import com.kyuu.persuratanmawang.databinding.FragmentHomeBinding
import com.kyuu.persuratanmawang.adapter.PermohonanAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var permohonanAdapter: PermohonanAdapter
    private lateinit var permohonanList: MutableList<PermohonanTes>

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            database = FirebaseDatabase.getInstance().reference.child("requests").child(uid)
        } else {
            // Handle case where user is not authenticated
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
        }

        permohonanList = mutableListOf()
        permohonanAdapter = PermohonanAdapter(permohonanList)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = permohonanAdapter

        loadPermohonanData()

        return root
    }

    private fun loadPermohonanData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                permohonanList.clear()
                for (dataSnapshot in snapshot.children) {
                    val permohonan = dataSnapshot.getValue(PermohonanTes::class.java)
                    permohonan?.let { permohonanList.add(it) }
                }
                permohonanAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                //Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

