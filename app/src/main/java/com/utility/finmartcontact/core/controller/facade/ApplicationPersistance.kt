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
    val IS_LOGIN_USER = "IS_LOGIN_USER"
    val FBAID_DATA = "FBAID_entity"
    val SSID_DATA = "SSID_entity"
    val PARENRT_DATA = "PARENRT_entity"

    init {
        sharedPreferences = context.getSharedPreferences(Utility.SHARED_PREF, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }
    override fun saveUser(loginResponse: LoginResponse) {
              editor.putString(IS_LOGIN_USER,"Y")
               editor.putString(USER_DATA, Gson().toJson(loginResponse))
               editor.commit()
    }

    override fun getUser(): LoginResponseEntity? {
        val user = sharedPreferences.getString(USER_DATA, "")
        val gson = Gson()
        if (user!!.length > 0) {
            val loginEntity = gson.fromJson<LoginResponse>(user, LoginResponse::class.java)
            return loginEntity.MasterData
        } else {
            return null
        }
    }


    fun isLoginUser() : String
    {
      return sharedPreferences.getString(IS_LOGIN_USER,"N").toString()
    }

    override fun getFBAID(): Int {
        when (isLoginUser()){

            "Y" ->{
                if (getUser() != null) return getUser()!!.FBAId.toInt() else return 0
             }
            "N" ->{

            return sharedPreferences.getString(FBAID_DATA,"0")!!.toInt()
            }
        }

        return 0

    }


    override fun getSSID(): String {


        when (isLoginUser()){

            "Y" ->{
                if (getUser() != null) return getUser()!!.ssid else return "0"
            }
            "N" ->{

                return sharedPreferences.getString(SSID_DATA,"0")!!.toString()
            }
        }

        return "0"
    }


    fun getParentID(): String {


        when (isLoginUser()){

            "Y" ->{
                if (getUser() != null) return getUser()?.parentid ?: "" else return ""
            }
            "N" ->{

                return sharedPreferences.getString(SSID_DATA,"0")!!.toString()
            }
        }

        return "0"
    }

    override fun setFBAAndSSID(fbaId: String,ssId: String,parentID: String ) {
        editor.putString(IS_LOGIN_USER,"N")
        editor.putString(FBAID_DATA,fbaId)
        editor.putString(SSID_DATA,ssId)
        editor.putString(PARENRT_DATA,parentID)
        editor.commit()
    }



}