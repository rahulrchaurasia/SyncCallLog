package com.utility.finmartcontact

import android.app.Application
import com.github.tamir7.contacts.Contacts

/**
 * Created by Rahul on 15/06/2022.
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Contacts.initialize(this);
    }
}