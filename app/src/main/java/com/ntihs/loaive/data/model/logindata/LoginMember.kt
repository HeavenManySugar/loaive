package com.ntihs.loaive.data.model.logindata

import com.google.gson.annotations.SerializedName


data class LoginMember (

    @SerializedName("name"     ) var name     : String? = null,
    @SerializedName("email"        ) var email        : String? = null,
    @SerializedName("password") var password: String? = null

)
