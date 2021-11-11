package com.utility.finmartcontact.core.controller.facade

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.utility.finmartcontact.core.response.LoginResponse
import com.utility.finmartcontact.core.response.LoginResponseEntity
import com.utility.finmartcontact.utility.Utility

/**
 * Created by Rajeev Ranjan on 29/03/2019.
 */
open class ApplicationPersistance (context: Context) : IApplicationPersistance {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    val USER_DATA = "login_entity"

    init {
        sharedPreferences = context.getSharedPreferences(Utility.SHARED_PREF, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }
    override fun saveUser(loginResponse: LoginResponse): Boolean {
        return editor.putString(USER_DATA, Gson().toJson(loginResponse)).commit()
    }

    override fun getUser(): LoginResponseEntity? {
        val user = sharedPreferences.getString(USER_DATA, "")
        val gson = Gson()
        if (user.length > 0) {
            val loginEntity = gson.fromJson<LoginResponse>(user, LoginResponse::class.java)
            return loginEntity.MasterData
        } else {
            return null
        }
    }




    override fun getFBAID(): Int {
        if (getUser() != null) return getUser()!!.FBAId.toInt() else return 0
    }
}