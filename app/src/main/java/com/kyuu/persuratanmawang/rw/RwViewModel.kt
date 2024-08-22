package com.kyuu.persuratanmawang.rw

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RwViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Layanan Persuratan Kelurahan Mawang"
    }
    val text: LiveData<String> = _text
}