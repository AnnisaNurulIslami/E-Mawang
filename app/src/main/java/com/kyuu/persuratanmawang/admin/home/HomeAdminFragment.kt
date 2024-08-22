package com.kyuu.persuratanmawang.admin.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kyuu.persuratanmawang.R
import com.kyuu.persuratanmawang.data.PermohonanTes
import com.kyuu.persuratanmawang.databinding.FragmentHomeAdminBinding

class HomeAdminFragment : Fragment() {
    private var _binding: FragmentHomeAdminBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeAdminViewModel::class.java)

        _binding = FragmentHomeAdminBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        Glide.with(this)
            .load(R.drawable.applogo)
            .override(200, 200)
            .into(binding.logoImage)

        fetchAndDisplayData()

        return root
    }

    private fun fetchAndDisplayData() {
        val database = FirebaseDatabase.getInstance().getReference("requests")

        val statuses = listOf(
            "Menunggu Persetujuan RT",
            "Menunggu Persetujuan RW",
            "Menunggu Persetujuan Kepala Lingkungan",
            "Menunggu Verifikasi Admin",
            "Permohonan Ditolak",
            "Surat Sedang Dibuat",
            "Surat Siap Diambil"
        )

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Pastikan binding tidak null
                if (_binding == null) {
                    Log.e("HomeAdminFragment", "Binding is null in onDataChange")
                    return
                }

                val statusCountMap = mutableMapOf<String, Int>()
                for (status in statuses) {
                    statusCountMap[status] = 0
                }
                var totalPermohonan = 0

                for (uidSnapshot in snapshot.children) {
                    for (generateKeySnapshot in uidSnapshot.children) {
                        val permohonan = generateKeySnapshot.getValue(PermohonanTes::class.java)
                        permohonan?.let {
                            val status = it.status
                            if (statusCountMap.containsKey(status)) {
                                statusCountMap[status] = statusCountMap[status]!! + 1
                            }
                            totalPermohonan += 1
                        }
                    }
                }

                // Update UI with counts
                binding.gridLayout.removeAllViews()
                for ((status, count) in statusCountMap) {
                    updateUI(status, count)
                }
                updateUI("Total Permohonan", totalPermohonan)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DashboardAdminFragment", "Database error: ${error.message}")
            }
        })
    }

    private fun updateUI(status: String, count: Int) {
        // Pastikan binding tidak null
        if (_binding == null) {
            Log.e("HomeAdminFragment", "Binding is null in updateUI")
            return
        }

        val gridLayout = binding.gridLayout

        val cardView = createCard(count.toString(), status)
        val layoutParams = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(16, 16, 16, 16) // Mengatur margin
        }
        cardView.layoutParams = layoutParams
        gridLayout.addView(cardView)
    }

    private fun createCard(count: String, status: String): View {
        val inflater = LayoutInflater.from(context)
        val cardView = inflater.inflate(R.layout.item_card, null) as CardView

        val numberTextView = cardView.findViewById<TextView>(R.id.numberTextView)
        val statusTextView = cardView.findViewById<TextView>(R.id.statusTextView)

        numberTextView.text = count
        statusTextView.text = status

        val color = when (status) {
            "Menunggu Persetujuan RT" -> ContextCompat.getColor(requireContext(), R.color.dark_blue)
            "Menunggu Persetujuan RW" -> ContextCompat.getColor(requireContext(), R.color.dark_blue)
            "Menunggu Persetujuan Kepala Lingkungan" -> ContextCompat.getColor(requireContext(), R.color.dark_blue)
            "Menunggu Verifikasi Admin" -> ContextCompat.getColor(requireContext(), R.color.dark_blue)
            "Surat Sedang Dibuat" -> ContextCompat.getColor(requireContext(), R.color.dark_blue)
            "Permohonan Ditolak" -> ContextCompat.getColor(requireContext(), R.color.dark_red)
            "Surat Siap Diambil" -> ContextCompat.getColor(requireContext(), R.color.dark_green)
            "Total Permohonan" -> ContextCompat.getColor(requireContext(), R.color.black)
            else -> Color.BLUE // Default color if none match
        }
        numberTextView.setTextColor(color)
        //statusTextView.setTextColor(color)

        val backgroundColor = when (status) {
            "Menunggu Persetujuan RT" -> ContextCompat.getColor(requireContext(), R.color.white)
            "Menunggu Persetujuan RW" -> ContextCompat.getColor(requireContext(), R.color.white)
            "Menunggu Persetujuan Kepala Lingkungan" -> ContextCompat.getColor(requireContext(), R.color.white)
            "Menunggu Verifikasi Admin" -> ContextCompat.getColor(requireContext(), R.color.white)
            "Surat Sedang Dibuat" -> ContextCompat.getColor(requireContext(), R.color.white)
            "Permohonan Ditolak" -> ContextCompat.getColor(requireContext(), R.color.white)
            "Surat Siap Diambil" -> ContextCompat.getColor(requireContext(), R.color.white)
            "Total Permohonan" -> ContextCompat.getColor(requireContext(), R.color.white)
            else -> Color.LTGRAY // Default background color if none match
        }
        cardView.setCardBackgroundColor(backgroundColor)
        // Set padding for cardView
        cardView.setPadding(16, 22, 16, 22)

        return cardView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
