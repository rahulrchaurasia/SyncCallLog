package com.utility.finmartcontact.core.requestentity

import com.utility.finmartcontact.core.model.ContactlistEntity

/**
 * Created by Rajeev Ranjan on 01/04/2019.
 */
data class ContactLeadRequestEntity (
    var fbaid: String,
    var ssid : String,
    var contactlist: List<ContactlistEntity>? = null,
    var raw_data: String
)
