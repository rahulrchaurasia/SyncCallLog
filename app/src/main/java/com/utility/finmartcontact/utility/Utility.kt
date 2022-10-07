package com.utility.finmartcontact.utility

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log

open class Utility {

    companion object {

        var SHARED_PREF = "finmart_syncContact"
        //var FOLLOWUP_FBA = "follow_up_fba"
        var DISPOSION_FBA = "disposition_fba"
        var MY_FOLLOW_UP = "myfollow_up"
        var CALL_DURATION = "disposition_call_duration"


        val READ_CONTACTS_CODE = 101
        val REQUEST_PERMISSION_SETTING = 102
        var PUSH_NOTIFY = "notifyFlag"

         fun loadWebViewUrlInBrowser(context: Context, url: String?) {
            Log.d("URL", url!!)
            val browserIntent = Intent(Intent.ACTION_VIEW)
            if (Uri.parse(url) != null) {
                browserIntent.data = Uri.parse(url)
            }
            context.startActivity(browserIntent)
        }

        fun getDeviceId(context: Context?): String? {
            val deviceId = ""
            return if (context != null) Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) else deviceId
        }
    }



}