package com.kyuu.persuratanmawang.admin.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeAdminViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Layanan Persuratan Kelurahan Mawang"
    }
    val text: LiveData<String> = _text
}