package com.utility.finmartcontact

import android.content.Context

open class BaseController() {


    fun errorStatus(saveError: String): String {
        when (saveError) {

            "400" -> {
                return "Bad request :The server cannot or will not process the request due to an apparent client error status"
            }
            "403" -> {
                return "Forbidden :Server is refusing action"
            }
            "404" -> {
                return "Not found :The requested resource could not be found "
            }
            "500" -> {
                return "Internal Server Error : Unexpected condition was encountered"
            }

            "502" -> {
                return "Bad Gateway : Invalid response from the upstream server"
            }

            "503" -> {
                return "Service Unavailable : The server is currently unavailable"
            }

            "504" -> {
                return "Gateway Timeout : The server is currently unavailable"
            }
        }
        return ""
    }


}