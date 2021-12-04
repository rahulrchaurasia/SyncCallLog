package com.utility.finmartcontact.core.controller.login

import com.utility.finmartcontact.IResponseSubcriber
import com.utility.finmartcontact.core.requestentity.CallLogRequestEntity
import com.utility.finmartcontact.core.requestentity.ContactLeadRequestEntity
import com.utility.finmartcontact.core.requestentity.LoginRequestEntity

/**
 * Created by Rajeev Ranjan on 30/03/2019.
 */
interface ILogin {



    fun login(loginRequestEntity : LoginRequestEntity, iResponseSubcriber : IResponseSubcriber);

     fun uploadContact(contactLeadRequestEntity: ContactLeadRequestEntity , iResponseSubcriber : IResponseSubcriber);

    fun uploadCallLog(callLogRequestEntity: CallLogRequestEntity, iResponseSubcriber : IResponseSubcriber);
}