package com.utility.finmartcontact.core.requestbuilder

import com.utility.finmartcontact.RetroRequestBuilder
import com.utility.finmartcontact.core.requestentity.CallLogRequestEntity
import com.utility.finmartcontact.core.requestentity.ContactLeadRequestEntity
import com.utility.finmartcontact.core.requestentity.LoginRequestEntity
import com.utility.finmartcontact.core.response.ContactLeadResponse
import com.utility.finmartcontact.core.response.ContactLogResponse
import com.utility.finmartcontact.core.response.LoginResponse
import com.utility.finmartcontact.core.response.LoginResponseEntity
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Created by Rajeev Ranjan on 30/03/2019.
 */
open class LoginRequestBuilder : RetroRequestBuilder() {

    fun getService(): LoginRequestBuilder.LoginNetworkService {
        return super.build().create(LoginRequestBuilder.LoginNetworkService::class.java)
    }


    interface LoginNetworkService {

        @Headers("token:1234567890")
        @POST("/api/Synclogin")
        fun login(@Body body : LoginRequestEntity): Call<LoginResponse>


        @POST()
        fun saveContactLead(@Url url: String, @Body body : ContactLeadRequestEntity): Response<ContactLeadResponse>


        @POST()
        suspend fun saveCallLog(@Url url: String, @Body body : CallLogRequestEntity): Response<ContactLogResponse>



        @POST()
         fun saveContactLeadOld(@Url url: String, @Body body : ContactLeadRequestEntity): Call<ContactLeadResponse>

//        @POST()
//         fun saveCallLog(@Url url: String, @Body body : CallLogRequestEntity): Call<ContactLogResponse>

    }
}

