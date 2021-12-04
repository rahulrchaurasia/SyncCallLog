package com.utility.finmartcontact.login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import com.utility.finmartcontact.APIResponse
import com.utility.finmartcontact.BaseActivity
import com.utility.finmartcontact.IResponseSubcriber
import com.utility.finmartcontact.R
import com.utility.finmartcontact.core.controller.login.LoginController
import com.utility.finmartcontact.core.requestentity.LoginRequestEntity
import com.utility.finmartcontact.core.response.LoginResponse
import com.utility.finmartcontact.home.HomeActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.content_login.*


class LoginActivity : BaseActivity(), View.OnClickListener, IResponseSubcriber {

    private val TAG = "Permission"
    private val READ_CONTACTS_CODE = 101

    var perm = arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //  setSupportActionBar(toolbar)

        btnSignIn.setOnClickListener(this)
        setupPermissions()

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


    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.btnSignIn -> {

                hideKeyBoard(btnSignIn)

                if (etEmail.text.toString().isEmpty()) {
                    showMessage(etEmail, "Invalid input", "", null)
                    return
                }
                if (etPassword.text.toString().isEmpty()) {
                    showMessage(etPassword, "Invalid password", "", null)
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

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
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
