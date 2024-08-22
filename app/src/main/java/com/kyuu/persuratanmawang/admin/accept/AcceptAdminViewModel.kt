package com.kyuu.persuratanmawang.admin.accept

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AcceptAdminViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Daftar Permohonan"
    }
    val text: LiveData<String> = _text
}