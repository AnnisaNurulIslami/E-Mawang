package com.kyuu.persuratanmawang.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.storage.FirebaseStorage
import com.kyuu.persuratanmawang.data.PermohonanTes
import com.kyuu.persuratanmawang.databinding.FragmentDashboardBinding
import com.kyuu.persuratanmawang.ui.InfoActivity
import com.kyuu.persuratanmawang.adapter.FileAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private val fileUris = mutableListOf<Uri>()
    private lateinit var fileAdapter: FileAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
                val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val view = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()

        val religionValues = arrayOf("Islam", "Kristen (Protestan)", "Katolik", "Hindu", "Budha", "Khong Hu Ciu")
        val letterValues = arrayOf(
            "Kartu Tanda Penduduk (KTP)", "Kartu Keluarga (KK)", "Surat Keterangan Tidak Mampu",
            "Surat Keterangan Pindah", "Surat Keterangan Kelahiran", "Surat Keterangan Kematian",
            "Surat Keterangan Beasiswa", "Surat Keterangan Catatan Kepolisian (SKCK)", "Surat Keterangan Nikah (SKN)",
            "Surat Keterangan Usaha", "Surat Izin Tempat Usaha (SITU)")

        val religionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, religionValues)
        religionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerReligion.adapter = religionAdapter

        val letterAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, letterValues)
        letterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLetter.adapter = letterAdapter

        fileAdapter = FileAdapter(fileUris) { uri ->
            fileUris.remove(uri)
            fileAdapter.notifyDataSetChanged()
        }

        binding.recyclerViewFiles.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewFiles.adapter = fileAdapter
        binding.recyclerViewFiles.setHasFixedSize(true)

        binding.buttonPickFiles.setOnClickListener {
            pickFiles()
        }

        binding.btnSubmit.setOnClickListener {
            submitFiles()
        }

        binding.tvHere.setOnClickListener {
            val intent = Intent(requireContext(), InfoActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun pickFiles() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Select Files"), REQUEST_CODE_PICK_FILES)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_FILES && resultCode == Activity.RESULT_OK) {
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val fileUri = data.clipData!!.getItemAt(i).uri
                    fileUris.add(fileUri)
                }
            } else if (data?.data != null) {
                val fileUri = data.data!!
                fileUris.add(fileUri)
            }
            fileAdapter.notifyDataSetChanged()
        }
    }

    private fun submitFiles() {
        val name = binding.etName.text.toString()
        val nik = binding.etNik.text.toString()
        val ttl = binding.etTtl.text.toString()
        val religion = binding.spinnerReligion.selectedItem.toString()
        val job = binding.etJob.text.toString()
        val address = binding.etAddress.text.toString()
        val letter = binding.spinnerLetter.selectedItem.toString()

        var isValid = true

        if (name.isEmpty()) {
            binding.etName.error = "Nama tidak boleh kosong"
            isValid = false
        } else {
            binding.etName.error = null
        }

        if (nik.isEmpty()) {
            binding.etNik.error = "NIK tidak boleh kosong"
            isValid = false
        } else {
            binding.etNik.error = null
        }

        if (ttl.isEmpty()) {
            binding.etTtl.error = "Tempat/Tanggal Lahir tidak boleh kosong"
            isValid = false
        } else {
            binding.etTtl.error = null
        }

        if (job.isEmpty()) {
            binding.etJob.error = "Pekerjaan tidak boleh kosong"
            isValid = false
        } else {
            binding.etJob.error = null
        }

        if (address.isEmpty()) {
            binding.etAddress.error = "Alamat tidak boleh kosong"
            isValid = false
        } else {
            binding.etAddress.error = null
        }

        if (fileUris.isEmpty()) {
            Toast.makeText(context, "Harap mengunggah setidaknya satu file", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (!isValid) {
            return
        }

        val uid = auth.currentUser?.uid ?: return
        val userRef = database.child("users").child(uid)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val rt = snapshot.child("rt").value.toString()
                val rw = snapshot.child("rw").value.toString()
                val environment = snapshot.child("lingkungan").value.toString()

                val calendar = Calendar.getInstance()
                val uploadDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)

                val uniqueId = "${System.currentTimeMillis()}_${uid}"

                val permohonanRef = database.child("requests").child(uid).push() // Push a new permohonan node
                val permohonanId = permohonanRef.key ?: return@addOnSuccessListener

                val permohonan = PermohonanTes(uid, name, nik, ttl, religion, job, address, letter,
                    mutableListOf(),
                    "Menunggu Persetujuan RT",
                    rt, rw, environment, uploadDate,
                    null.toString(), uniqueId
                )

                // Save permohonan data
                permohonanRef.setValue(permohonan)
                    .addOnSuccessListener {
                        // If data is successfully saved, upload files
                        for (uri in fileUris) {
                            val fileName = uri.lastPathSegment ?: continue
                            uploadFile(permohonanId, fileName, uri) {
                                // Navigate back and show success toast after the last file upload
                                if (fileUris.last() == uri) {
                                    requireActivity().supportFragmentManager.popBackStack()
                                    Toast.makeText(context, "Berhasil mengunggah permohonan", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to submit", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "User information not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to retrieve user information", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadFile(permohonanId: String, fileName: String, fileUri: Uri, onComplete: () -> Unit) {
        val storageRef = storage.reference.child("files/$permohonanId/$fileName")
        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveFileUrl(permohonanId, downloadUri.toString(), onComplete)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to upload $fileName", Toast.LENGTH_SHORT).show()
                onComplete()
            }
    }

    private fun saveFileUrl(permohonanId: String, url: String, onComplete: () -> Unit) {
        val permohonanRef = database.child("requests").child(auth.currentUser?.uid ?: "").child(permohonanId)
        permohonanRef.child("fileUrls").get().addOnSuccessListener { snapshot ->
            val fileUrls = snapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()
            fileUrls.add(url)
            permohonanRef.child("fileUrls").setValue(fileUrls)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //Toast.makeText(context, "File URL saved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to save file URL", Toast.LENGTH_SHORT).show()
                    }
                    onComplete()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_CODE_PICK_FILES = 1001
    }
}
