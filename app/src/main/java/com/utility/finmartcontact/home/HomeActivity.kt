package com.utility.finmartcontact.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.github.tamir7.contacts.Contacts
import com.google.android.material.snackbar.Snackbar
import com.utility.finmartcontact.APIResponse
import com.utility.finmartcontact.BaseActivity
import com.utility.finmartcontact.IResponseSubcriber
import com.utility.finmartcontact.R
import com.utility.finmartcontact.core.controller.login.LoginController
import com.utility.finmartcontact.core.model.CallLogEntity
import com.utility.finmartcontact.core.model.ContactlistEntity
import com.utility.finmartcontact.core.model.NotifyEntity
import com.utility.finmartcontact.core.requestentity.CallLogRequestEntity
import com.utility.finmartcontact.core.requestentity.ContactLeadRequestEntity
import com.utility.finmartcontact.core.response.ContactLeadResponse
import com.utility.finmartcontact.core.response.ContactLogResponse
import com.utility.finmartcontact.home.Worker.CallLogWorkManager
import com.utility.finmartcontact.home.Worker.ContactLogWorkManager
import com.utility.finmartcontact.login.LoginActivity
import com.utility.finmartcontact.utility.Constant
import com.utility.finmartcontact.utility.NetworkUtils
import com.utility.finmartcontact.utility.Utility
import com.utility.finmartcontact.webview.CommonWebViewActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Long
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Array
import kotlin.Boolean
import kotlin.Exception
import kotlin.Int
import kotlin.IntArray
import kotlin.String
import kotlin.arrayOf
import kotlin.let
import kotlin.toString


class HomeActivity : BaseActivity(), View.OnClickListener, IResponseSubcriber {

    private val TAG = "CONTACT"
    private val TAGCALL = "CALL_LOG"

    var contactlist: MutableList<ContactlistEntity>? = null
    var templist: MutableList<String>? = null
    var phones: Cursor? = null
    var progressBar: ProgressBar? = null
    var progress_circular : ProgressBar? = null
    lateinit var  txtPercent :TextView
    var callLogList: MutableList<CallLogEntity>? = null
   // rvCallList
    var  formatter : SimpleDateFormat =  SimpleDateFormat("dd-MMM-yyyy HH:mm:ss")

    var date = Date()
    var calenderMobDate = Calendar.getInstance()
    var calenderPrevDate = Calendar.getInstance()

    var currentProgress = 0
    var maxProgress = 0
    var remainderProgress = 0

    var  maxProgressContact = 0
    var remainderProgressContact = 0

    lateinit var currentPrevDate : String

    var perms = arrayOf(
        "android.permission.READ_CONTACTS",
        "android.permission.READ_CALL_LOG"
    )
    lateinit var pinfo: PackageInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        //region Initialization
        contactlist = ArrayList<ContactlistEntity>()
        templist = ArrayList<String>()
        callLogList = ArrayList<CallLogEntity>()
        CvSync.setOnClickListener(this)
        progressBar = findViewById(R.id.progressBar)
        progress_circular = findViewById(R.id.progress_circular)
        txtPercent = findViewById(R.id.txtPercent)
        rvCallList.layoutManager = LinearLayoutManager(this)

        lySync.visibility = View.GONE

        val currentDateandTime = formatter.format(Date())
        val date = formatter.parse(currentDateandTime)
        // val calObj = Calendar.getInstance()
        calenderPrevDate.time = date
        calenderPrevDate.add(Calendar.YEAR, -1)

        currentPrevDate = formatter.format(calenderPrevDate.time)
        Log.d(TAGCALL, "Prev One Month :" + currentPrevDate)


        Log.d(TAG,"FBAID is ${applicationPersistance.getFBAID()} and SSID is ${applicationPersistance.getSSID()} " +
                "Parent ID is ${applicationPersistance.getParentID()} " )
    //    endregion


        // getPackage Info
        try {
            pinfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0)



        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        getNotificationAction()

    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId){
            R.id.action_logout -> {
                showAlert("SynContact","Do you want to logout!!") { type  : String, dialog : DialogInterface ->

                    when(type){
                        "Y" -> {
                            // toast("Logout Successfully...!!")

                            applicationPersistance.clearAll()
                            val intent = Intent(this, LoginActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                            finish()
                        }
                        "N" -> {
                            dialog.dismiss()
                        }

                    }



                }
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        //Note : Used For handling Notification Click Action
      //  checkIntent(intent)
    }

    private fun getNotificationAction() {

        if (applicationPersistance!!.getFBAID() == 0) {

            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
        // region Activity Open Using Notification
       else if (intent.extras != null) {


            // step1: boolean verifyLogin = prefManager.getIsUserLogin();
            // region verifyUser : when user logout and when Apps in background

            if (intent.extras!!.getParcelable<Parcelable?>(Constant.PUSH_NOTIFY) != null) {
                val notificationEntity: NotifyEntity? = intent.extras!!.getParcelable(Constant.PUSH_NOTIFY)

                if (notificationEntity?.web_url != null) {

                    navigateViaNotification(notificationEntity.notifyFlag, notificationEntity.web_url, notificationEntity.web_title)
                }
            }


            //endregion
        }

        //endregion
    }

    private fun navigateViaNotification(prdID: String, WebURL: String, Title: String) {

        //   if (prdID.equals("18")) {
        //       startActivity(new Intent(HomeActivity.this, TermSelectionActivity.class));
        //   }


        var WebURL = WebURL
        if (prdID == "WB") {
            startActivity(Intent(this@HomeActivity, CommonWebViewActivity::class.java)
                .putExtra("URL", WebURL)
                .putExtra("NAME", Title)
                .putExtra("TITLE", Title))
        } else if (prdID == "CB") {
            Utility.loadWebViewUrlInBrowser(this@HomeActivity, WebURL)
        }else if (prdID == "SY") {

            CvSync.performClick()
        }
        else {

            if(WebURL.trim().length ==0 || Title.trim().length == 0){

                return
            }

            var ipaddress = "0.0.0.0"

            //&ip_address=10.0.3.64&mac_address=10.0.3.64&app_version=2.2.0&product_id=1
            val append = ("&ss_id=" + applicationPersistance.getSSID() + "&fba_id=" + applicationPersistance.getFBAID() + "&sub_fba_id=" +
                    "&ip_address=" + ipaddress + "&mac_address=" + ipaddress
                    + "&app_version=" + pinfo.versionName
                    + "&device_id=" + Utility.getDeviceId(this@HomeActivity)
                    + "&product_id=" + prdID
                    + "&login_ssid=")
            WebURL = WebURL + append
            startActivity(Intent(this@HomeActivity, CommonWebViewActivity::class.java)
                .putExtra("URL", WebURL)
                .putExtra("NAME", Title)
                .putExtra("TITLE", Title))
        }
    }

    //region Event
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.CvSync -> {

                if (!checkPermission()) {
                    if (checkRationalePermission()) {
                        requestPermission()
                    } else {
                        permissionAlert()
                    }
                } else {

                   // syncContactNumber()
                    // API For Contact

                    if(NetworkUtils.isNetworkAvailable(this@HomeActivity)) {
                        initData()
                        setOneTimeRequestWithCoroutine()
                    }else{
                        Snackbar.make(view,"No Internet Connection",Snackbar.LENGTH_SHORT).show()
                    }




                }
            }
        }
    }

    override fun onSuccess(response: APIResponse, message: String) {


        currentProgress = currentProgress + 1
        // For Contact Response
        if(response is ContactLeadResponse){

            txtMessage.text = "Data is Sync with Server ..."
            progressBar!!.setProgress(currentProgress)
            if(maxProgress >0){
                txtPercent.text = "${(currentProgress*100)/maxProgress} %"
            }

        }
        else if(response is ContactLogResponse)   // For Call Log Response
        {



            progressBar!!.setProgress(currentProgress)

            if(maxProgress == currentProgress){

                txtMessage.text = "Data Save Successfully..."
                txtPercent.text ="100%"

                btnSync.setBackgroundColor(ContextCompat.getColor(this@HomeActivity!!, R.color.colorPrimaryDark))
                btnSync.isEnabled = true
                progress_circular!!.visibility = View.GONE
            }else{
                txtMessage.text = "Data is Uploading to the Server..."
                if(maxProgress >0){
                    txtPercent.text = "${(currentProgress*100)/maxProgress} %"
                }
            }


        }
        Log.d(
            TAGCALL,
            "progressBar progress max:  ${maxProgress} progressBar Count  ${currentProgress} "
        )


    }

    override fun onFailure(error: String) {
       // dismissDialog()
        showMessage(btnSync, error, "", null)



        resetData()

    }

    //endregion


    //region AsyncTask Handling

    inner class LoadContactTask : AsyncTask<Void, Int, Void>() {
        override fun onPreExecute() {
            super.onPreExecute()
            //   txtCount.setText("" + phones!!.getCount())

        }

        @SuppressLint("Range")
        override fun doInBackground(vararg voids: Void): Void? {
            // Get Contact list from Phone

            Log.d("---Phone", phones.toString())

            // val regex = Regex("[^0-9\\s]")
            val regex = Regex("[^.0-9]")
            if (phones != null && phones!!.getCount() > 0) {
                try {
                    var i = 1
                    while (phones!!.moveToNext()) {


                        var name =
                            "" + phones?.getString(phones!!.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        var phoneNumber =
                            "" + phones!!.getString(phones!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                        //  phoneNumber = phoneNumber.trim().replace("[^0-9\\s+]", "");   // remove Special character and Space

                        phoneNumber = regex.replace(phoneNumber, "") // works
                        //.replace("\\s".toRegex(), "")

                        if (phoneNumber.length >= 10) {

                            phoneNumber = phoneNumber.takeLast(10)
                            // check whether the number alreday added to list or not

                            if (!templist!!.contains(phoneNumber)) {
                                templist?.add(phoneNumber)

                                val selectUser = ContactlistEntity(
                                    name = name,
                                    mobileno = phoneNumber,
                                    id = i
                                )
                                Log.i(
                                    TAG,
                                    "Key ID: " + i + " Name: " + name + " Mobile: " + phoneNumber + "\n"
                                );
                                contactlist?.add(selectUser)
                                publishProgress(i++)
                            }


                        }


                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }


            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            lySync.visibility = View.VISIBLE

            dismissDialog()
            // Toast.makeText(this@HomeActivity,"Contact Added Successfully..",Toast.LENGTH_SHORT).show()
            txtCount.setText("" + contactlist!!.size)



             sendContactToServer()


            // API For CallLog
           // LoadCallLogTask().execute()


        }


    }

    inner class LoadCallLogTask : AsyncTask<Void, Void, Void>() {
        override fun onPreExecute() {
            super.onPreExecute()
            //   txtCount.setText("" + phones!!.getCount())
           // showLoading("Please Wait!!")

            callLogList!!.clear()

           //  rvCallList.adapter = CallLogAdapter(callLogList!!, this@HomeActivity)    // temp hide recyclerView

        }

        override fun doInBackground(vararg voids: Void): Void? {
            // Get Contact list from Phone


            getCallDetails(this@HomeActivity)


            return  null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            lySync.visibility = View.VISIBLE


            // Todo :  Donot show Call Log Count hence below commented

           // txtCount.setText("" + callLogList?.size ?: "0")

          //  Log.i(TAGCALL, Gson().toJson(callLogList)).toString()


//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                getJsonInFile(""+ Gson().toJson(callLogList).toString())
//               // getJsonInFile("" + "hello first line 1")
//            }




            Log.d(TAGCALL, "Total Count are " + callLogList?.size ?: "0")

            //dismissDialog()

            // temp hide recyclerView

//            rvCallList.adapter = null
//            val adapter = CallLogAdapter(callLogList!!, this@HomeActivity)
//            rvCallList.adapter = adapter

             sendCallLogToServer()


        }


    }

    //endregion

    //region method callLog and contact Details

    fun initData(){

        currentProgress =0
        maxProgress = 0
        progressBar!!.setProgress(currentProgress)

        progressBarButton.visibility = View.VISIBLE

      //  progress_circular!!.visibility = View.VISIBLE

        lySync.visibility = View.VISIBLE
        CvSync.background?.alpha = 80
        CvSync.isEnabled = false
        txtPercent.text = "0%"

        txtMessage.text = ""
        txtCount.text = ""

    }

    private fun syncContactNumber() {

        txtMessage.text = ""
        txtMessage.text = "Please Wait, Data is Loading..."

        val PROJECTION = arrayOf(
            ContactsContract.RawContacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Photo.CONTACT_ID
        )

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val filter =
            "" + ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0 and " + ContactsContract.CommonDataKinds.Phone.TYPE + "=" + ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
        val order =
            ContactsContract.Contacts.DISPLAY_NAME + " ASC"// LIMIT " + limit + " offset " + lastId + "";

        phones = this.contentResolver.query(uri, PROJECTION, filter, null, order)


        if (phones == null || phones!!.getCount() == 0) {
            Log.e("count", "" + phones!!.getCount())

        } else {

           // showLoading("Uploading Data..")
            LoadContactTask().execute()

        }

    }

    private fun getCallDetails(context: Context) {

        var count: Int = 0;

        val stringBuffer = StringBuffer()
        val cursor: Cursor = context.getContentResolver().query(
            CallLog.Calls.CONTENT_URI,
            null, null, null, CallLog.Calls.DATE + " DESC"
        )!!
        val number = cursor.getColumnIndex(CallLog.Calls.NUMBER)
        val type = cursor.getColumnIndex(CallLog.Calls.TYPE)
        val date = cursor.getColumnIndex(CallLog.Calls.DATE)
        val duration = cursor.getColumnIndex(CallLog.Calls.DURATION)
        var cachedName = 0


        cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)?.let {
            cachedName = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)

        }


        while (cursor.moveToNext()) {
            count++

            //   if(count <10){

            val phNumber = cursor.getString(number)
            val callType = cursor.getString(type)
            val callDate = cursor.getString(date)

            val callDayTime = formatter.format(Date(Long.valueOf(callDate)))

            var mob_date: Date = formatter.parse(callDayTime)

            calenderMobDate.time =  mob_date


            val callDuration = cursor.getString(duration)
            var pName = ""

            cursor.getString(cachedName)?.let {
                pName = "" + cursor.getString(cachedName)
            }


            var dir: String? = ""
            val dircode = callType.toInt()
            var phoneCallType: String? = ""
            when (dircode) {
                CallLog.Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                CallLog.Calls.INCOMING_TYPE -> dir = "INCOMING"
                CallLog.Calls.MISSED_TYPE -> dir = "MISSED"
                CallLog.Calls.REJECTED_TYPE -> dir = "REJECTED"
                4 -> dir = "NEW"
                6 -> dir = "BLOCK"
                7 -> dir = "NEW 7"
                8 -> dir = "NEW FEATURES_WIFI"

            }

            // var timeDiff : Long =  (callDayTime2) - (currentDateMinus)
            // var days_difference: Long = timeDiff /  (1000 * 60 * 60 * 24) % 365

            // Log.i("DAYS DIFFERENCE", (days_difference).toString())
            if(calenderMobDate.compareTo(calenderPrevDate) == 1)  {

               // Log.i(TAGCALL, "ID added to Call Log" + count)


               // if (pName.isNotEmpty()) { }
               if ((callDuration.toInt()) > 0 && ( dircode ==  1  || dircode ==  2 ) ) {

                   if (pName.isEmpty()) {

                       pName = "NA"
                   }
                    callLogList?.add(
                        CallLogEntity(
                            phNumber,
                            pName,
                            dir!!,
                            callDuration,
                            callDayTime.toString(),
                            count
                        )
                    )

                }



            }


//            stringBuffer.append(
//                """
//                    Phone Number:--- $phNumber
//                    Call Type:--- $dir
//                    Name:---$cachedName
//                    Call Date:--- $callDayTime
//                    Call duration in sec :--- $callDuration"""
//            )
//            stringBuffer.append("\n----------------------------------")

            //}
        }



        /////
        cursor.close()



//        rvCallList.adapter = null
//        rvCallList.adapter = CallLogAdapter(callLogList!!, this)

//        Log.d("----calllog", stringBuffer.toString())
//        Log.d("----calllog", count.toString())
//        txtLogCount.setText("${count}")
//        return stringBuffer.toString()
    }

    fun sendContactToServer() {


       // showLoading("Sending to Server...")
        var subcontactlist: List<ContactlistEntity>

        // id is from 1 to lisSize-1

        if (contactlist != null && contactlist!!.size > 0) {

            var getAllContactDetails = Contacts.getQuery().find()
            //Log.d("raw_contact", Gson().toJson(getAllContactDetails))
            //Log.d("raw_contact--size", getAllContactDetails.size.toString())


            maxProgressContact = 0
            remainderProgressContact = 0

            maxProgressContact = contactlist!!.size/1000

            remainderProgressContact = contactlist!!.size % 1000

            if(remainderProgressContact > 0){
                maxProgressContact = maxProgressContact + 1
            }

            var tfbaid = ""
            var tsub_fba_id = ""

            if (applicationPersistance!!.getParentID().isEmpty() || applicationPersistance!!.getParentID().equals("0")){

                tfbaid = applicationPersistance!!.getFBAID().toString()
                tsub_fba_id =  applicationPersistance!!.getParentID()

            }else{
                tfbaid = applicationPersistance!!.getParentID()
                tsub_fba_id =  applicationPersistance!!.getFBAID().toString()
            }

            for (i in 0..contactlist!!.size - 1 step 1000) {

                Log.d(TAGCALL, "Contact Number of data jumped ${i}")
                subcontactlist = contactlist!!.filter { it.id > i && it.id <= (1000 + i) }



                //region  Adding in Request Entity and Send to server

                //Gson().toJson(getAllContactDetails)
                val contactRequestEntity = ContactLeadRequestEntity(
                    fbaid = tfbaid,
                    ssid = applicationPersistance!!.getSSID(),
                    sub_fba_id = tsub_fba_id,
                    contactlist = subcontactlist,
                    batchid =  System.currentTimeMillis().toString(),
                    raw_data = ""
                )
                LoginController(this@HomeActivity).uploadContact(
                    contactRequestEntity,
                    this@HomeActivity
                )


                //endregion


            }


        }





    }

    fun sendCallLogToServer() {


      //  showLoading("Sending to Server...")
        var subLoglist: List<CallLogEntity>

        // id is from 1 to lisSize-1

        if (callLogList != null && callLogList!!.size > 0) {

            remainderProgress = 0
            maxProgress = 0

            maxProgress = callLogList!!.size/1000

            remainderProgress = callLogList!!.size % 1000

            if(remainderProgress > 0){
                maxProgress = maxProgress + 1
            }
            maxProgress = maxProgress + maxProgressContact
            progressBar!!.max = maxProgress


            Log.d(TAGCALL, "Progrss bar size" +  maxProgress +  " And Contact Size" +maxProgressContact)

            var tfbaid = ""
            var tsub_fba_id = ""

            if (applicationPersistance!!.getParentID().isEmpty() || applicationPersistance!!.getParentID().equals("0")){

                tfbaid = applicationPersistance!!.getFBAID().toString()
                tsub_fba_id =  applicationPersistance!!.getParentID()

            }else{
                tfbaid = applicationPersistance!!.getParentID()
                tsub_fba_id =  applicationPersistance!!.getFBAID().toString()
            }


            for (i in 0..callLogList!!.size - 1 step 1000) {

                Log.d(TAGCALL, "CallLog Number of data jumped ${i}")

                subLoglist = callLogList!!.filter { it.id > i && it.id <= (1000 + i) }

                val callLogRequestEntity = CallLogRequestEntity(

                    call_history = subLoglist,
                    fba_id =  Integer.valueOf(tfbaid),
                    sub_fba_id =  Integer.valueOf(tsub_fba_id),
                    ss_id = Integer.valueOf(applicationPersistance.getSSID())
                )
                LoginController(this@HomeActivity).uploadCallLog(
                    callLogRequestEntity,
                    this@HomeActivity
                )



                //endregion


            }


        }


    }

    fun resetData(){

        progressBar!!.setProgress(0)
        lySync.visibility = View.GONE
        progress_circular!!.visibility = View.GONE
        btnSync.isEnabled = true

        btnSync.setBackgroundColor(ContextCompat.getColor(this@HomeActivity!!, R.color.colorPrimaryDark))
        txtMessage.text = "Data is not send to the Server due to some issue , Please try again!!"
        txtCount.text = ""

    }

    //endregion


    //region Not in Used (For generate json Text File

    @RequiresApi(Build.VERSION_CODES.O)
    fun getJsonInFile(strBody: String) {


        var textFile = createFile("CallLog")

        var os: FileOutputStream? = null
        try {
            // os = FileOutputStream(textFile)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.write(
                    textFile!!.toPath(),
                    strBody.toByteArray(),
                    StandardOpenOption.CREATE_NEW
                )
                Files.write(textFile!!.toPath(), strBody.toByteArray(), StandardOpenOption.APPEND)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun getJsonPdf(strBody: String){


        var pdfFile =  stringtopdf(strBody)

        var screenshotUri : Uri? =     pdfFile?.let {

            FileProvider.getUriForFile(
                this@HomeActivity,
                getString(R.string.file_provider_authority),
                it
            )


        }

        sharePdfFile("Sample", "Testing Data..", screenshotUri)


    }


    @SuppressLint("Range")
    fun getNameForNumber(context: Context, number: String): String? {

        var res: String? = null
        try {
            val resolver = context.contentResolver
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(
                    number
                )
            )
            val c = resolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null,
                null,
                null
            )

            if (c != null) {
                if (c.moveToFirst()) {
                    res = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                }
                c.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return res
    }


    fun jsonAlert(

        strBody: String
    ) {
        val builder = AlertDialog.Builder(this)
        val btnClose: Button

        val etData: EditText
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.layout_data_message, null)
        builder.setView(dialogView)
        val alertDialog = builder.create()

        btnClose = dialogView.findViewById(R.id.btnClose)
        etData = dialogView.findViewById(R.id.etData)

        etData.setText("" + strBody)
        btnClose.setOnClickListener {
            alertDialog.dismiss()



        }
        alertDialog.setCancelable(false)
        alertDialog.show()
        alertDialog.getWindow()!!.setGravity(Gravity.CENTER);

    }



    //endregion

    // region Permission Handling

    private fun checkPermission(): Boolean {
        val read_contact = ActivityCompat.checkSelfPermission(this@HomeActivity, perms[0])
        val read_call_log = ActivityCompat.checkSelfPermission(this@HomeActivity, perms[1])

        return (read_contact == PackageManager.PERMISSION_GRANTED) && (read_call_log == PackageManager.PERMISSION_GRANTED)
    }


    private fun requestPermission() {
        ActivityCompat.requestPermissions(this@HomeActivity, perms, Utility.READ_CONTACTS_CODE)

    }

    private fun checkRationalePermission(): Boolean {
        val readContact =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this@HomeActivity,
                Manifest.permission.READ_CONTACTS
            )

        val readCallLog =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this@HomeActivity,
                Manifest.permission.READ_CALL_LOG
            )

        return readContact && readCallLog
    }

    fun permissionAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Need  Permission")
        builder.setMessage("This App Required Contact Permissions.")
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->


            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivityForResult(intent, Utility.REQUEST_PERMISSION_SETTING)

        }



        builder.show()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Utility.READ_CONTACTS_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    // syncContactNumber()
                   // getCallDetails(this)

                    if(NetworkUtils.isNetworkAvailable(this@HomeActivity)) {
                        initData()
                        setOneTimeRequestWithCoroutine()
                    }else{
                        Snackbar.make(btnSync,"No Internet Connection",Snackbar.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }


    // endregion


    // onNewIntent : use with android:launchMode="singleTop"
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

         Toast.makeText(this,"OnNewIntent",Toast.LENGTH_LONG ).show()
    }

    private fun setOneTimeRequestWithCoroutine() {




        lySync.visibility = View.VISIBLE
        txtMessage.visibility = View.VISIBLE
        val workManager: WorkManager = WorkManager.getInstance(applicationContext)

        //callLogList: MutableList<CallLogEntity>

        val data: Data = Data.Builder()
            .putInt(Constant.KEY_fbaid, applicationPersistance!!.getFBAID())
            .putString(Constant.KEY_parentid, applicationPersistance!!.getParentID())
            .putString(Constant.KEY_ssid, applicationPersistance!!.getSSID())
            .build()


//        WorkManager.getInstance(this)
//            .beginUniqueWork("CallLogWorkManager", ExistingWorkPolicy.APPEND_OR_REPLACE,
//                OneTimeWorkRequest.from(CallLogWorkManager::class.java)).enqueue().state
//            .observe(this) { state ->
//                Log.d(TAGCALL, "CallLogWorkManager: $state")
//            }

        val constraintNetworkType: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()


        val callLogWorkRequest: OneTimeWorkRequest =
            OneTimeWorkRequest.Builder(CallLogWorkManager::class.java)
                .addTag(Constant.TAG_SAVING_CALL_LOG)
                .setInputData(data)
                .build()


        val ContactWorkRequest: OneTimeWorkRequest =
            OneTimeWorkRequest.Builder(ContactLogWorkManager::class.java)
                .addTag(Constant.TAG_SAVING_CALL_LOG)
                .setInputData(data)
                .build()

        // Todo : For Chain (Parallel Chaining)
        val parallelWorks: MutableList<OneTimeWorkRequest> = mutableListOf<OneTimeWorkRequest>()
         parallelWorks.add(ContactWorkRequest)
         parallelWorks.add(callLogWorkRequest)
        workManager.beginWith(parallelWorks)
            .enqueue()



        workManager.getWorkInfoByIdLiveData(callLogWorkRequest.id)
            .observe(this, { workInfo: WorkInfo? ->


                // txtMessage.text = workInfo.state.name

                if (workInfo != null) {

                    val progress = workInfo.progress
                    val valueprogress = progress.getInt(Constant.CALL_LOG_Progress, 0)
                    val valueMaxProgress = progress.getInt(Constant.CALL_LOG_MAXProgress, 0)
                    updateProgrees(valueprogress, valueMaxProgress)
                    Log.d(
                        "CALL_LOG",
                        "MaxProgress Progress :--> ${valueMaxProgress} and currentProgress :  ${valueprogress}"
                    )
                    if (workInfo.state.isFinished) {
                        val opData: Data = workInfo.outputData
                        val msg: String? = opData.getString(Constant.KEY_result)
                        val errormsg: String? = opData.getString(Constant.KEY_error_result)
                        Log.d("CALL_LOG", workInfo.state.name + "\n\n" + msg)


                        if (!errormsg.isNullOrEmpty()) {
                            //  errormsg?.let { saveMessage(it) }
                            errorMessage(errormsg)
                        } else {

                            if (msg.isNullOrEmpty()) {
                                saveMessage()
                            } else {
                                saveMessage(msg)
                            }
                        }


                    }


                }


            })


        //region Commented :-- contact Progress Not Showing


        workManager.getWorkInfoByIdLiveData(ContactWorkRequest.id)
            .observe(this,{workInfo: WorkInfo? ->


                // txtMessage.text = workInfo.state.name

                if(workInfo != null ){

                    //region commented
         //          val progress = workInfo.progress
 //                   val valueprogress = progress.getInt(Constant.CONTACT_LOG_Progress, 0)
//                    val valueMaxProgress = progress.getInt(Constant.CONTACT_LOG_MAXProgress, 0)
//                    updateProgrees(valueprogress,valueMaxProgress)
                  //  Log.d("CALL_LOG", "MaxProgress Progress :--> ${valueMaxProgress} and currentProgress :  ${valueprogress}")
                      //endregion

                    if( workInfo.state.isFinished){
                        val opData : Data  = workInfo.outputData
                        val msg : String? = opData.getString(Constant.KEY_result)

                        txtCount.text = msg?: ""

                        //region commented
//                        if(!errormsg.isNullOrEmpty()){
//                            //  errormsg?.let { saveMessage(it) }
//                            errorMessage(errormsg)
//                        }else{
//                            if(msg.isNullOrEmpty()){
//                                saveMessage()
//                            }else{
//                                saveMessage(msg)
//                            }
//                        }
                        //endregion




                    }


                }



            })



        // endregion



    }

    private fun updateProgrees(currentProg : Int , maxProg : Int){

        progressBar!!.max = maxProg
        progressBar!!.setProgress(currentProg)

        if(maxProg >0){
            txtPercent.text = "${(currentProg*100)/maxProg} %"
        }

    }

    private fun saveMessage(opMessage : String = "Data Save Successfully..."){


        CvSync.setCardBackgroundColor(
            ContextCompat.getColor(
                this@HomeActivity!!,
                R.color.colorPrimaryDark
            )
        )
        CvSync.isEnabled = true
       // progress_circular!!.visibility = View.GONE
        txtMessage.text = opMessage
        txtPercent.text ="100%"
        progressBar!!.max = 100
        progressBar!!.setProgress(100)
        progressBarButton.visibility = View.GONE

    }

    private fun errorMessage(opMessage : String = "Data not Uploade. Please Try Again..."){

        btnSync.setBackgroundColor(
            ContextCompat.getColor(
                this@HomeActivity!!,
                R.color.colorPrimaryDark
            )
        )
        CvSync.isEnabled = true
        btnSync.isEnabled = true
        progress_circular!!.visibility = View.GONE
        txtMessage.text = opMessage

        progressBarButton.visibility = View.GONE
    }

    private fun checkIntent(intent: Intent?) {
        intent?.let {
            if (it.hasExtra(Constant.NOTIFICATION_EXTRA)) {

                val progress = it.getIntExtra(Constant.NOTIFICATION_PROGRESS,100)
                val maxProgress = it.getIntExtra(Constant.NOTIFICATION_MAX,100)
                val message = it.getStringExtra(Constant.NOTIFICATION_MESSAGE)

                lySync.visibility = View.VISIBLE
                CvSync.background?.alpha = 80
                CvSync.isEnabled = false

                updateProgrees( progress,maxProgress)
                txtMessage.text = message
            }
        }
    }



}


