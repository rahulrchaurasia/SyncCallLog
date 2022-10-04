package com.utility.finmartcontact.core.requestbuilder

import com.utility.finmartcontact.RetroRequestBuilder
import com.utility.finmartcontact.core.model.TokenRequestEntity
import com.utility.finmartcontact.core.requestentity.CallLogRequestEntity
import com.utility.finmartcontact.core.requestentity.ContactLeadRequestEntity
import com.utility.finmartcontact.core.requestentity.LoginRequestEntity
import com.utility.finmartcontact.core.response.*
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


//        @POST()
//        fun saveContactLead(@Url url: String, @Body body : ContactLeadRequestEntity): Response<ContactLeadResponse>

        @Headers("token:1234567890")
        @POST("/api/Insertsynccontacttoken")
        suspend fun insertToken(@Body body : TokenRequestEntity): Response<TokenResponse>


        @POST()
        fun saveContactLead(@Url url: String, @Body body : ContactLeadRequestEntity): Call<ContactLeadResponse>


        @POST()
        suspend fun saveCallLog(@Url url: String, @Body body : CallLogRequestEntity): Response<ContactLogResponse>

        @POST()
        suspend fun saveCallLogOld(@Url url: String, @Body body : CallLogRequestEntity): Call<ContactLogResponse>



        @POST()
         fun saveContactLeadOld(@Url url: String, @Body body : ContactLeadRequestEntity): Call<ContactLeadResponse>

//        @POST()
//         fun saveCallLog(@Url url: String, @Body body : CallLogRequestEntity): Call<ContactLogResponse>

    }
}

