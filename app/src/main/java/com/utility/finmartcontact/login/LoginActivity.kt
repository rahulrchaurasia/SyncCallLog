package com.utility.finmartcontact.login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.utility.finmartcontact.*
import com.utility.finmartcontact.core.controller.facade.ApplicationPersistance
import com.utility.finmartcontact.core.controller.login.LoginController
import com.utility.finmartcontact.core.model.TokenRequestEntity
import com.utility.finmartcontact.core.requestentity.LoginRequestEntity
import com.utility.finmartcontact.core.response.LoginResponse
import com.utility.finmartcontact.home.HomeActivity
import com.utility.finmartcontact.utility.Constant
import com.utility.finmartcontact.utility.NetworkUtils
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.content_login.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class LoginActivity : BaseActivity(), View.OnClickListener, IResponseSubcriber {

    private val TAG = "Permission"
    private val READ_CONTACTS_CODE = 101
    private lateinit var prefManager : ApplicationPersistance

    var perm = arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE
        )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //  setSupportActionBar(toolbar)
        prefManager = ApplicationPersistance(this)


        btnSignIn.setOnClickListener(this)
        setupPermissions()

//        if(intent.getStringExtra("fbaid") != null){
//
//           if(intent.hasExtra("fbaid") && intent.hasExtra("ssid") && intent.hasExtra("parentid")) {
//                val fbaid = intent.getStringExtra("fbaid")
//               val ssid=intent.getStringExtra("ssid")
//               val parentid=intent.getStringExtra("parentid")
//
//
//               //endregion
//               if(fbaid != null && ssid != null && parentid != null){
//                   ApplicationPersistance(this@LoginActivity).setFBAAndSSID(fbaid,ssid,parentid)
//                   val intent = Intent(this, HomeActivity::class.java)
//                   startActivity(intent)
//                   this.finish()
//               }
//
//
//
//
//            }
//
//        }



    }

    private fun setupPermissions() {
        val readContact = ContextCompat.checkSelfPermission(this, perm[0])
        val readCallLog = ContextCompat.checkSelfPermission(this, perm[1])

        val readStorsge = ContextCompat.checkSelfPermission(this, perm[2])
        val writeStorage = ContextCompat.checkSelfPermission(this, perm[3])


        if (readContact != PackageManager.PERMISSION_GRANTED && readCallLog != PackageManager.PERMISSION_GRANTED &&
            readStorsge != PackageManager.PERMISSION_GRANTED && writeStorage != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "Permission to read denied")
            makeRequest()
        }
    }

    private fun getVersionNo() : Int {

        try {
            val pInfo: PackageInfo = this@LoginActivity.getPackageManager().getPackageInfo(packageName, 0)
            val version = pInfo.versionCode
            return version
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return 3
        }
    }

    private fun checkRationalePermission(): Boolean {
        val readContact = ActivityCompat.shouldShowRequestPermissionRationale(
            this@LoginActivity,
            Manifest.permission.READ_CONTACTS
        )

        val readCallLog = ActivityCompat.shouldShowRequestPermissionRationale(
            this@LoginActivity,
            Manifest.permission.READ_CALL_LOG
        )

        return readContact && readCallLog
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            perm,
            READ_CONTACTS_CODE
        )
    }


    override fun onClick(view: View?) {

        when (view?.id) {
            R.id.btnSignIn -> {



                hideKeyBoard(btnSignIn)

                if (etEmail.text.toString().isEmpty()) {
                    showMessage(etEmail, "Enter Email/User ID", "", null)
                    return
                }
                if (etPassword.text.toString().isEmpty()) {
                    showMessage(etPassword, "Enter Password", "", null)
                    return
                }
                if(!NetworkUtils.isNetworkAvailable(this@LoginActivity)){
                    Snackbar.make(view,"No Internet Connection", Snackbar.LENGTH_SHORT).show()
                    return
                }
                var loginRequestEntity = LoginRequestEntity(
                    UserName = etEmail.text.toString(),
                    Password = etPassword.text.toString(),
                    VersionNo = getVersionNo()
                )

                showLoading("Authenticating user..")
               LoginController(this).login(loginRequestEntity, this)




            }
        }
    }



    override fun onSuccess(response: APIResponse, message: String) {

        dismissDialog()

        if (response is LoginResponse) {


            lifecycleScope.launch(Dispatchers.IO) {


                val tokenRequestEntity = TokenRequestEntity(

                    FBAID = applicationPersistance.getFBAID().toString(),
                    SSID = applicationPersistance.getSSID().toString(),
                    tokenid = applicationPersistance.getToken(),
                    DeviceId = "",
                    AppVersNumb = BuildConfig.VERSION_CODE.toString()
                )

                val resultResp = RetroHelper.api.insertToken(body = tokenRequestEntity )

                if(resultResp.isSuccessful){

                    resultResp.body()?.let { res ->

                        if(res.StatusNo == 0){
                            Log.d(TAG, "Success"+ res.MasterData[0].Message)

                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()

                        }else{
                            Log.d(TAG, "Failure")
                            showMessage(etPassword, Constant.ErrorMssg, "", null)
                        }

                    } ?:showMessage(etPassword, Constant.ErrorMssg, "", null)


                }else{
                    Log.d(TAG, "Failure")
                    showMessage(etPassword, Constant.ErrorMssg, "", null)
                }



          }


        }
    }

    override fun onFailure(error: String) {
        dismissDialog()
        showMessage(etPassword, error, "", null)
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        when (requestCode) {
//            READ_CONTACTS_CODE -> {
//
//                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//
//                    Log.i(TAG, "Permission has been denied by user")
//                    makeRequest()
//
//                } else {
//                    Log.i(TAG, "Permission has been granted by user")
//
//                }
//            }
//        }
//    }


}
