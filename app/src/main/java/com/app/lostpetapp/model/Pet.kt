package com.app.lostpetapp.model

import android.os.Parcelable
import com.app.lostpetapp.util.Util
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Pet(
    var photo: String = "",
    var description: String = "",
    var latitude: Double = 0.000000,
    var longitude: Double = 0.000000,
    var isFound: Boolean = false,
    var city: String = "",
    var date: String = ""
) : Parcelable {
    val _date: String
        get() = Util.stringFormatToString(date)
}

