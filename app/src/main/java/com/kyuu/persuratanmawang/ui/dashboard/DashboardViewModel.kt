package com.kyuu.persuratanmawang.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "FORMULIR PERMOHONAN"
    }
    val text: LiveData<String> = _text

    fun updateText(newText: String) {
        _text.value = newText
    }
}