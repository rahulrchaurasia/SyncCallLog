package com.utility.finmartcontact.splashScreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.utility.finmartcontact.R
import com.utility.finmartcontact.core.controller.facade.ApplicationPersistance
import com.utility.finmartcontact.core.response.LoginResponseEntity
import com.utility.finmartcontact.home.HomeActivity
import com.utility.finmartcontact.login.LoginActivity
import com.utility.finmartcontact.utility.Constant

class SplashScreenActivity : AppCompatActivity() {

    private val SPLASH_DISPLAY_LENGTH = 1000
   var loginResponseEntity  : LoginResponseEntity? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        var prefManager = ApplicationPersistance(this)

        FirebaseMessaging.getInstance().token.addOnSuccessListener { result ->
            if (result != null) {
                //fbToken = result
                // DO your thing with your firebase token
                prefManager.setToken(result)
                Log.d(Constant.TAG, "Refreshed token: " + result)
            }


        }
       loginResponseEntity = prefManager.getUser()

            //user behaviour data collection in Async
            Handler(Looper.getMainLooper()).postDelayed({
                if (loginResponseEntity?.FBAId != null) {
                    startActivity(Intent(this@SplashScreenActivity, HomeActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashScreenActivity, LoginActivity::class.java))
                }
            }, SPLASH_DISPLAY_LENGTH.toLong())


    }
}