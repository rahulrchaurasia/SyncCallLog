package com.utility.finmartcontact.core.controller.login

import android.content.Context
import com.utility.finmartcontact.BaseController
import com.utility.finmartcontact.BuildConfig
import com.utility.finmartcontact.IResponseSubcriber
import com.utility.finmartcontact.core.controller.facade.ApplicationPersistance
import com.utility.finmartcontact.core.requestbuilder.LoginRequestBuilder
import com.utility.finmartcontact.core.requestentity.CallLogRequestEntity
import com.utility.finmartcontact.core.requestentity.ContactLeadRequestEntity
import com.utility.finmartcontact.core.requestentity.LoginRequestEntity
import com.utility.finmartcontact.core.response.ContactLeadResponse
import com.utility.finmartcontact.core.response.ContactLogResponse
import com.utility.finmartcontact.core.response.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * Created by Rajeev Ranjan on 30/03/2019.
 */
class LoginController(val context : Context) : BaseController() ,ILogin {



    var loginNetwork :LoginRequestBuilder.LoginNetworkService


   init {
       loginNetwork = LoginRequestBuilder().getService()
   }

    override fun login(loginRequestEntity: LoginRequestEntity, iResponseSubcriber: IResponseSubcriber) {


        loginNetwork.login(loginRequestEntity).enqueue(object : Callback<LoginResponse>{


            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response!!.isSuccessful) {
                    if (response.body()?.StatusNo == 0) {
                        ApplicationPersistance(context).saveUser(response.body()!!)
                        iResponseSubcriber.onSuccess(response.body()!!, response.message())
                    } else {
                        iResponseSubcriber.onFailure(response.body()?.Message!!)

                    }
                } else {
                    iResponseSubcriber.onFailure(errorStatus(response.code().toString()))
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {

                iResponseSubcriber.onFailure("Server error, Try again later")
            }
        })


    }


    override fun uploadContact(contactLeadRequestEntity: ContactLeadRequestEntity, iResponseSubcriber: IResponseSubcriber) {

        var url = BuildConfig.SYNC_CONTACT_URL + "/contact_entry"

        loginNetwork.saveContactLeadOld(url ,contactLeadRequestEntity).enqueue(object : Callback<ContactLeadResponse>{


            override fun onResponse(call: Call<ContactLeadResponse>, response: Response<ContactLeadResponse>) {
                if (response!!.isSuccessful) {
                    if (response.body()?.StatusNo == 0) {

                        iResponseSubcriber.onSuccess(response.body()!!, response.message())
                    } else {
                        iResponseSubcriber.onFailure(response.body()?.Message!!)
                    }
                } else {
                    iResponseSubcriber.onFailure(errorStatus(response.code().toString()))
                }
            }

            override fun onFailure(call: Call<ContactLeadResponse>, t: Throwable) {

                iResponseSubcriber.onFailure("Server error, Try again later")
            }
        })
    }


    override fun uploadCallLog(
        callLogRequestEntity: CallLogRequestEntity,
        iResponseSubcriber: IResponseSubcriber
    ) {
        var url = BuildConfig.SYNC_CONTACT_URL + "/contact_call_history"

//        loginNetwork.saveCallLog(url ,callLogRequestEntity).enqueue(object : Callback<ContactLogResponse>{
//
//
//            override fun onResponse(call: Call<ContactLogResponse>, response: Response<ContactLogResponse>) {
//                if (response!!.isSuccessful) {
//                    if (response.body()?.StatusNo == 0) {
//
//                        iResponseSubcriber.onSuccess(response.body()!!, response.message())
//                    } else {
//                        iResponseSubcriber.onFailure(response.body()?.Message!!)
//                    }
//                } else {
//                    iResponseSubcriber.onFailure(errorStatus(response.code().toString()))
//                }
//            }
//
//            override fun onFailure(call: Call<ContactLogResponse>, t: Throwable) {
//
//                iResponseSubcriber.onFailure("Server error, Try again later")
//            }
//        })
    }
}