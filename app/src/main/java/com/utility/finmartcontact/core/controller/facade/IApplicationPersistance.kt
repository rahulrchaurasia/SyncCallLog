package com.utility.finmartcontact.core.controller.facade

import com.utility.finmartcontact.core.response.LoginResponse
import com.utility.finmartcontact.core.response.LoginResponseEntity

/**
 * Created by Rajeev Ranjan on 29/03/2019.
 */
interface IApplicationPersistance {



    fun saveUser(loginResponse: LoginResponse)

    fun getUser(): LoginResponseEntity?

    fun getFBAID(): Int

    fun getSSID(): String


    fun setFBAAndSSID(fbaId : String,ssId : String,parentID : String)

}

