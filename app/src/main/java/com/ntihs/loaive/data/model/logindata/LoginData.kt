package com.ntihs.loaive.data.model.logindata

import com.google.gson.annotations.SerializedName

data class LoginData (

    @SerializedName("result" ) var result : LoginResult? = LoginResult()

)