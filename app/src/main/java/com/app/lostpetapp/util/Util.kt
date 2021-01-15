package com.app.lostpetapp.util

import java.text.SimpleDateFormat


object Util {

    @JvmStatic
    fun stringFormatToString(date: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        return formatter.format(format.parse(date))
    }
}