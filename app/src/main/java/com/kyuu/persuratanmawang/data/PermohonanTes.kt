package com.kyuu.persuratanmawang.data

data class PermohonanTes(
    val uid: String = "",
    val name: String,
    val nik: String,
    val ttl: String,
    val religion: String,
    val job: String,
    val address: String,
    val letter: String,
    val fileUrls: MutableList<String>,
    val status: String,
    val rt: String,
    val rw: String,
    val lingkungan: String,
    val uploadDate: String,
    val alasanPenolakan: String,
    val uniqueId: String

) {
    constructor() : this("","", "", "", "", "", "", "", mutableListOf(), "","", "", "", "", "", "")
    // firebase membutuhkan costructor agar tidak error
}

