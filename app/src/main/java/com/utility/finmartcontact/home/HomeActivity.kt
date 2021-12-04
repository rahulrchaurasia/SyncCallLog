package com.utility.finmartcontact.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import com.github.tamir7.contacts.Contacts
import com.google.gson.Gson
import com.utility.finmartcontact.APIResponse
import com.utility.finmartcontact.BaseActivity
import com.utility.finmartcontact.IResponseSubcriber
import com.utility.finmartcontact.R
import com.utility.finmartcontact.core.controller.login.LoginController
import com.utility.finmartcontact.core.model.CallLogEntity
import com.utility.finmartcontact.core.model.ContactlistEntity
import com.utility.finmartcontact.core.requestentity.CallLogRequestEntity
import com.utility.finmartcontact.core.requestentity.ContactLeadRequestEntity
import com.utility.finmartcontact.core.response.ContactLeadResponse
import com.utility.finmartcontact.core.response.ContactLogResponse
import com.utility.finmartcontact.utility.Utility
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.text.SimpleDateFormat
import java.util.*


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        //region Initialization
        contactlist = ArrayList<ContactlistEntity>()
        templist = ArrayList<String>()
        callLogList = ArrayList<CallLogEntity>()
        btnSync.setOnClickListener(this)
        progressBar = findViewById(R.id.progressBar)
        progress_circular = findViewById(R.id.progress_circular)
        txtPercent = findViewById(R.id.txtPercent)
        rvCallList.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        lySync.visibility = View.GONE

        val currentDateandTime = formatter.format(Date())
        val date = formatter.parse(currentDateandTime)
        // val calObj = Calendar.getInstance()
        calenderPrevDate.time = date
        calenderPrevDate.add(Calendar.YEAR, -1)

        currentPrevDate = formatter.format(calenderPrevDate.time)
        Log.d(TAGCALL, "Prev One Month :" + currentPrevDate)



        //endregion

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSync -> {

                if (!checkPermission()) {
                    if (checkRationalePermission()) {
                        requestPermission()
                    } else {
                        permissionAlert()
                    }
                } else {

                   // showMessage(btnSync, "Hi", "", null)

                    // API For Contact

                    currentProgress =0
                    progressBar!!.setProgress(currentProgress)
                    lySync.visibility = View.VISIBLE
                    progress_circular!!.visibility = View.VISIBLE
                    btnSync.background?.alpha = 80
                    btnSync.isEnabled = false


                    syncContactNumber()


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



    //region AsyncTask Handling

    inner class LoadContactTask : AsyncTask<Void, Int, Void>() {
        override fun onPreExecute() {
            super.onPreExecute()
            //   txtCount.setText("" + phones!!.getCount())

        }

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
                            "" + phones!!.getString(phones!!.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
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
            LoadCallLogTask().execute()


        }


    }

    inner class LoadCallLogTask : AsyncTask<Void, Void, Void>() {
        override fun onPreExecute() {
            super.onPreExecute()
            //   txtCount.setText("" + phones!!.getCount())
           // showLoading("Please Wait!!")

            callLogList!!.clear()

            // rvCallList.adapter = CallLogAdapter(callLogList!!, this@HomeActivity)

        }

        override fun doInBackground(vararg voids: Void): Void? {
            // Get Contact list from Phone


            getCallDetails(this@HomeActivity)


            return  null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            lySync.visibility = View.VISIBLE


           // txtCount.setText("" + callLogList?.size ?: "0")

          //  Log.i(TAGCALL, Gson().toJson(callLogList)).toString()


//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                getJsonInFile(""+ Gson().toJson(callLogList).toString())
//               // getJsonInFile("" + "hello first line 1")
//            }




            Log.d(TAGCALL, "Total Count are " + callLogList?.size ?: "0")

            //dismissDialog()

//            rvCallList.adapter = null
//            val adapter = CallLogAdapter(callLogList!!, this@HomeActivity)
//            rvCallList.adapter = adapter

            sendCallLogToServer()


        }


    }

    //endregion

    //region method callLog and contact Details

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
        )
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

            val callDayTime = formatter.format(Date(java.lang.Long.valueOf(callDate)))

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
            }

            // var timeDiff : Long =  (callDayTime2) - (currentDateMinus)
            // var days_difference: Long = timeDiff /  (1000 * 60 * 60 * 24) % 365

            // Log.i("DAYS DIFFERENCE", (days_difference).toString())
            if(calenderMobDate.compareTo(calenderPrevDate) == 1)  {

               // Log.i(TAGCALL, "ID added to Call Log" + count)

                if (pName.isNotEmpty()) {

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

            for (i in 0..contactlist!!.size - 1 step 1000) {

                Log.d(TAGCALL, "Contact Number of data jumped ${i}")
                subcontactlist = contactlist!!.filter { it.id > i && it.id <= (1000 + i) }


                //region  Adding in Request Entity and Send to server
                val contactRequestEntity = ContactLeadRequestEntity(
                    fbaid = applicationPersistance!!.getFBAID().toString(),
                    ssid = applicationPersistance!!.getSSID().toString(),
                    contactlist = subcontactlist,
                    raw_data = Gson().toJson(getAllContactDetails)
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


            for (i in 0..callLogList!!.size - 1 step 1000) {

                Log.d(TAGCALL, "CallLog Number of data jumped ${i}")

                subLoglist = callLogList!!.filter { it.id > i && it.id <= (1000 + i) }



                val callLogRequestEntity = CallLogRequestEntity(

                    call_history = subLoglist,
                    fba_id = applicationPersistance!!.getFBAID(),
                    ss_id = Integer.valueOf(applicationPersistance!!.getSSID())
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

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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
        when (requestCode) {
            Utility.READ_CONTACTS_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    // syncContactNumber()
                    getCallDetails(this)
                }
            }
        }
    }


    // endregion






}


