package com.kyuu.persuratanmawang.admin.dashboard

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kyuu.persuratanmawang.data.PermohonanTes
import com.kyuu.persuratanmawang.databinding.FragmentDashboardAdminBinding
import com.kyuu.persuratanmawang.rt.adapter.PermohonanRtAdapter

class DashboardAdminFragment : Fragment() {
    private var _binding: FragmentDashboardAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var permohonanAdapter: PermohonanRtAdapter
    private lateinit var permohonanAcceptAdapter: PermohonanRtAdapter
    private val permohonanList = mutableListOf<PermohonanTes>()
    private val permohonanAcceptList = mutableListOf<PermohonanTes>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardAdminViewModel::class.java)

        _binding = FragmentDashboardAdminBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val textViewVerif: TextView = binding.textVerif
        dashboardViewModel.textVerif.observe(viewLifecycleOwner) {
            textViewVerif.text = it
        }

        setupRecyclerView()
        setupRecyclerView2()


        fetchAndDisplayData()
        fetchAndDisplayDataAccept()

        return root
    }

    private fun setupRecyclerView() {
        permohonanAdapter = PermohonanRtAdapter(permohonanList)
        binding.recyclerView1.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView1.adapter = permohonanAdapter
    }

    private fun setupRecyclerView2() {
        permohonanAcceptAdapter = PermohonanRtAdapter(permohonanAcceptList)
        binding.recyclerView2.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView2.adapter = permohonanAcceptAdapter
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
                            if (permohonan?.status == "Menunggu Verifikasi Admin") {
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

    private fun fetchAndDisplayDataAccept() {
        val database = FirebaseDatabase.getInstance().getReference("requests")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                permohonanAcceptList.clear()
                for (uidSnapshot in snapshot.children) {
                    for (generateKeySnapshot in uidSnapshot.children) {
                        try {
                            val permohonan = generateKeySnapshot.getValue(PermohonanTes::class.java)
                            if (permohonan?.status == "Surat Sedang Dibuat") {
                                permohonanAcceptList.add(permohonan)
                            }
                        } catch (e: DatabaseException) {
                            Log.e("RtActivity", "Error converting snapshot to PermohonanTes", e)
                        }

                    }
                }
                Log.d("DashboardAdminFragment", "Data fetched: ${permohonanList.size} items")
                permohonanAcceptAdapter.notifyDataSetChanged()  // Notify adapter that data has changed
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
