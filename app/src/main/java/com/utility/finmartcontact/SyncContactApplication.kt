package com.utility.finmartcontact

import android.app.Application
import com.github.tamir7.contacts.Contacts

class SyncContactApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Contacts.initialize(this)
    }
}