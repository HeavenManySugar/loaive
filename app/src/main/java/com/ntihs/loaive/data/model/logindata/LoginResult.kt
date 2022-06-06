package com.ntihs.loaive.data.model.logindata

import com.google.gson.annotations.SerializedName


data class LoginResult (

    @SerializedName("status"     ) var status     : String? = null,
    @SerializedName("err"        ) var err        : String? = null,
    @SerializedName("loginMember") var loginMember: String? = null,
    @SerializedName("registerMember" ) var registerMember : LoginMember? = LoginMember()

)
