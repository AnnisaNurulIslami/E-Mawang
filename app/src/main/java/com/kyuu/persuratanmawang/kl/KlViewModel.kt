package com.kyuu.persuratanmawang.kl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class KlViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Layanan Persuratan Kelurahan Mawang"
    }
    val text: LiveData<String> = _text
}