package com.kyuu.persuratanmawang.admin.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationsAdminViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Kelola Akun"
    }
    val text: LiveData<String> = _text
}