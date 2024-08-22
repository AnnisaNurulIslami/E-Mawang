package com.kyuu.persuratanmawang.data

data class User(
    val name: String = "",
    val email: String = "",
    val rt: String = "",
    val rw: String = "",
    val lingkungan: String = "",
    val password: String = "",
    val role: String = "",
)

data class UserRole(
    val name: String = "",
    val email: String = "",
    val rt: String = "",
    val rw: String = "",
    val lingkungan: String = "",
    val password: String = "",
    val role: String = "",
    var uid: String
) {
    constructor() : this("", "", "", "", "", "", "", "")
}

data class UserNotification(
    val name: String = "",
    val email: String = "",
    val rt: String = "",
    val rw: String = "",
    val lingkungan: String = ""
)
