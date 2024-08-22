package com.kyuu.persuratanmawang.admin.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardAdminViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Verifikasi Permohonan"
    }
    val text: LiveData<String> = _text

    private val _textVerif = MutableLiveData<String>().apply {
        value = "Selesaikan Permohonan"
    }
    val textVerif: LiveData<String> = _textVerif
}