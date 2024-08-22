package com.kyuu.persuratanmawang.admin.accept

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kyuu.persuratanmawang.R
import com.kyuu.persuratanmawang.admin.dashboard.DashboardAdminViewModel
import com.kyuu.persuratanmawang.data.PermohonanTes
import com.kyuu.persuratanmawang.databinding.FragmentAcceptAdminBinding
import com.kyuu.persuratanmawang.databinding.FragmentDashboardAdminBinding
import com.kyuu.persuratanmawang.rt.adapter.PermohonanRtAdapter

class AcceptAdminFragment : Fragment() {

    private var _binding: FragmentAcceptAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var permohonanAdapter: PermohonanRtAdapter
    private val permohonanList = mutableListOf<PermohonanTes>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(AcceptAdminViewModel::class.java)

        _binding = FragmentAcceptAdminBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        setupRecyclerView()
        //setupSearchView()
        fetchAndDisplayData()

        return root
    }

    private fun setupRecyclerView() {
        permohonanAdapter = PermohonanRtAdapter(permohonanList)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = permohonanAdapter
    }

    private fun fetchAndDisplayData() {
        val database = FirebaseDatabase.getInstance().getReference("requests")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                permohonanList.clear()
                for (uidSnapshot in snapshot.children) {
                    for (generateKeySnapshot in uidSnapshot.children) {
                        try {
                            val permohonan = generateKeySnapshot.getValue(PermohonanTes::class.java)
                            if (permohonan?.status == "Surat Sedang Dibuat") {
                                permohonanList.add(permohonan)
                            }
                        } catch (e: DatabaseException) {
                            Log.e("RtActivity", "Error converting snapshot to PermohonanTes", e)
                        }

                    }
                }
                Log.d("DashboardAdminFragment", "Data fetched: ${permohonanList.size} items")
                permohonanAdapter.notifyDataSetChanged()  // Notify adapter that data has changed
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DashboardAdminFragment", "Database error: ${error.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}