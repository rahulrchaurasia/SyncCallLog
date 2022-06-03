package com.utility.finmartcontact.core.response

data class LoginResponseEntity(

    val FBAId: Int,
    val ssid: String,
    val parentid: String,
    val SuccessStatus: String?

)