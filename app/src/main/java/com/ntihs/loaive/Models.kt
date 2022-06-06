package com.ntihs.loaive

data class Message (val userName : String, val messageContent : String, val roomName: String,var viewType : Int)
data class InitialData (val userName : String, val roomName : String, val token: String)
data class SendMessage(val userName : String, val messageContent: String, val roomName: String)