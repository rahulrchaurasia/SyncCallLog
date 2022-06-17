package com.utility.finmartcontact.home.Worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.os.Build
import android.provider.CallLog
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.utility.finmartcontact.R
import com.utility.finmartcontact.RetroHelper
import com.utility.finmartcontact.core.model.CallLogEntity
import com.utility.finmartcontact.core.requestentity.CallLogRequestEntity
import com.utility.finmartcontact.home.HomeActivity
import com.utility.finmartcontact.utility.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

import java.lang.Long
import java.text.SimpleDateFormat
import com.utility.finmartcontact.BuildConfig
import java.util.*
import kotlin.Exception
import kotlin.Int
import kotlin.String
import kotlin.apply
import kotlin.let
import kotlin.to
import kotlin.toString

/**
 * Created by Rahul on 04/06/2022.
 */
class CallLogWorkManager(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    private val TAG = "CALL_LOG"

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager




    override suspend fun doWork(): Result {


        return try {
                Log.d("CallLogWorker", "Run work manager")
                //Do Your task here

                callLogTask()

                // Log.d("CallLogWorker", callLogList.toString())
                val outPutData: Data = Data.Builder()
                    .putString(Constant.KEY_result, "Data Uploaded Successfully..")
                    .putInt(Constant.KEY_result, 0)
                    .build()
                Result.success(outPutData)
            }
            catch (e: Exception) {
                Log.d(TAG, "exception in doWork1 ${e.message}")
                val errorData: Data = Data.Builder()
                    .putString(Constant.KEY_error_result, "Data Not Uploaded.Please Try Again.")
                    .build()
                Result.failure(errorData)

            }

    }

    private suspend fun callLogTask() {

        var strbody = "Contact is Uploading Please wait.."
        var strResultbody = "Successfully Uploaded.."
        setForeground(createForegroundInfo(0, 0, strbody))

        // region getting Input Data
        val fbaid = inputData.getInt(Constant.KEY_fbaid, 0)
        val ssid = inputData.getString(Constant.KEY_ssid)
        val parentid = inputData.getString(Constant.KEY_parentid)

        var tfbaid = ""
        var tsub_fba_id = ""

        if (parentid.isNullOrEmpty() || parentid.equals("0")) {

            tfbaid = fbaid.toString()
            tsub_fba_id = parentid.toString()

        } else {
            tfbaid = parentid.toString()
            tsub_fba_id = fbaid.toString()
        }

        //endregion

        //delay(2000)

        withContext(Dispatchers.IO) {

            var url = BuildConfig.SYNC_CONTACT_URL + "/contact_call_history"


            var callLogList = getCallDetails()

            //temp 05 added : have to remove below one line
           // callLogList = callLogList.take(41) as MutableList<CallLogEntity>

            var remainderProgress = 0
            var maxProgress = 0
            var currentProgress = 0
            var defaultProgress = 1
            maxProgress = callLogList!!.size / 1000

            remainderProgress = callLogList!!.size % 1000
            maxProgress = maxProgress + defaultProgress
            currentProgress = defaultProgress
            if (remainderProgress > 0) {
                maxProgress = maxProgress + 1
            }
            //  maxProgress = maxProgress + maxProgressContact
           // Log.d(TAG, "maxProgress ${maxProgress}")
            setForeground(createForegroundInfo(maxProgress, currentProgress, strbody))
            val workProgessDefault = workDataOf(Constant.CALL_LOG_Progress to currentProgress,
                Constant.CALL_LOG_MAXProgress to maxProgress)
            setProgress(workProgessDefault)
            /////

            var subLoglist: List<CallLogEntity>
            if (callLogList != null && callLogList!!.size > 0) {

                for (i in 0..callLogList!!.size - 1 step 1000) {

                    Log.d(TAG, "CallLog Number of data jumped ${i}")

                    subLoglist = callLogList!!.filter { it.id > i && it.id <= (1000 + i) }



                    // region calling to server

                    val callLogRequestEntity = CallLogRequestEntity(

                        call_history = subLoglist,
                        fba_id = Integer.valueOf(tfbaid),
                        sub_fba_id = Integer.valueOf(tsub_fba_id),
                        ss_id = Integer.valueOf(ssid)
                    )

                    val resultResp = RetroHelper.api.saveCallLog(url, callLogRequestEntity)
                   // val resultResp = RetroHelper.api.saveCallLog(url, callLogRequestEntity).await()

                    if (resultResp.isSuccessful) {


                        //region send Notification Progress
                       // Log.d(TAG, resultResp.Message ?: "Save")

                        currentProgress = currentProgress + 1
                        val workProgess = workDataOf(Constant.CALL_LOG_Progress to currentProgress,
                                                     Constant.CALL_LOG_MAXProgress to maxProgress)
                        setProgress(workProgess)
                        if (currentProgress < maxProgress) {
                            setForeground(
                                createForegroundInfo(
                                    maxProgress = maxProgress,
                                    progress = currentProgress,
                                    strbody = strbody
                                )
                            )

                        } else {
                            setForeground(
                                createForegroundInfo(
                                    maxProgress = maxProgress,
                                    progress = currentProgress,
                                    strbody = strResultbody
                                )
                            )

                        }
                        //endregion

                      //  delay(2000)

                    }

                    //endregion



                    // region Commented : -> For Testing without Service
                    ////////////// temp Handling ////////////////////////////
//                    currentProgress = currentProgress + 1
//                    val workProgess = workDataOf(Constant.CALL_LOG_Progress to currentProgress,
//                        Constant.CALL_LOG_MAXProgress to maxProgress)
//                    setProgress(workProgess)
//                    if (currentProgress < maxProgress) {
//                        setForeground(
//                            createForegroundInfo(
//                                maxProgress = maxProgress,
//                                progress = currentProgress,
//                                strbody = strbody
//                            )
//                        )
//
//                    } else {
//                        setForeground(
//                            createForegroundInfo(
//                                maxProgress = maxProgress,
//                                progress = currentProgress,
//                                strbody = strResultbody
//                            )
//                        )
//
//                    }
//                    delay(6000)
                    /////////////////////////////////////////////
                    //endregion

                }

            }


        }



    }


    //region Creates notifications for service
    private fun createForegroundInfo(
        maxProgress: Int,
        progress: Int,
        strbody: String
    ): ForegroundInfo {


        val id = "com.utility.PolicyBossPro.notifications1225"
        val channelName = "SynContact channel"
        val title = "Sync Contact"
        val cancel = "Cancel"

        val body = strbody
//            .setProgress(0, 0, true)    // for indeterminate progress

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(id, channelName)
        }


      // region Commented :--> For Handling  cancel
//        val intent = WorkManager.getInstance(applicationContext)
//            .createCancelPendingIntent(getId())

        // .setSummaryText(NotifyData.get("body"));

        //  new createBitmapFromURL(NotifyData.get("img_url")).execute();

        //endregion
    /////////////////////////////////////

        val notifyIntent = Intent(applicationContext, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        notifyIntent.putExtra(Constant.NOTIFICATION_EXTRA, true)
        notifyIntent.putExtra(Constant.NOTIFICATION_PROGRESS, progress)
        notifyIntent.putExtra(Constant.NOTIFICATION_MAX, maxProgress)
        notifyIntent.putExtra(Constant.NOTIFICATION_MESSAGE, strbody)

        //region Commented : Adding Pending Intent For Handling Notification Click Action
//        val notifyPendingIntent = PendingIntent.getActivities(
//            applicationContext, 0, arrayOf(notifyIntent), PendingIntent.FLAG_UPDATE_CURRENT
//        )

        //or

//        val notifyPendingIntent: PendingIntent
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            notifyPendingIntent = PendingIntent.getActivity(
//                applicationContext,
//                0, notifyIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
//            )
//        } else {
//            notifyPendingIntent = PendingIntent.getActivity(
//                applicationContext,
//                0, notifyIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT
//            )
//        }
//
        //endregion


        /////////////////////////

        val notificationBuilder: NotificationCompat.Builder

        notificationBuilder = NotificationCompat.Builder(applicationContext, id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationBuilder.setSmallIcon(R.drawable.ic_notification)
            notificationBuilder.color = applicationContext.getColor(R.color.colorPrimary)
        } else {
            notificationBuilder.setSmallIcon(R.drawable.ic_notification)
        }

     // .addAction(android.R.drawable.ic_delete, cancel, intent)

        notificationBuilder
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(body)
            .setOngoing(false)
            .setProgress(maxProgress, progress, false)

           // .setContentIntent(notifyPendingIntent)
            .setAutoCancel(true)

            .build()


        return ForegroundInfo(1, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel1(id: String, channelName: String) {
        notificationManager.createNotificationChannel(
            NotificationChannel(id, channelName, NotificationManager.IMPORTANCE_DEFAULT)

        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(id: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                id,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.lightColor = Color.BLUE
            channel.description = "PoliyBoss Pro"
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            channel.lockscreenVisibility =
                Notification.VISIBILITY_PUBLIC // Notification.VISIBILITY_PRIVATE
            notificationManager.createNotificationChannel(channel)
        }
    }

    //endregion

    private  fun getCallDetails(): MutableList<CallLogEntity> {

        var formatter: SimpleDateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss")
        var calenderMobDate = Calendar.getInstance()
        var calenderPrevDate = Calendar.getInstance()

        val currentDateandTime = formatter.format(Date())
        val currdate = formatter.parse(currentDateandTime)

        calenderPrevDate.time = currdate
        calenderPrevDate.add(Calendar.YEAR, -1)


        var callLogList: MutableList<CallLogEntity> = ArrayList<CallLogEntity>()

        var count: Int = 0;

        // val stringBuffer = StringBuffer()
        val cursor: Cursor = applicationContext.getContentResolver().query(
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

            calenderMobDate.time = mob_date


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
            if (calenderMobDate.compareTo(calenderPrevDate) == 1) {

                // Log.i(TAGCALL, "ID added to Call Log" + count)


                // if (pName.isNotEmpty()) { }
                if ((callDuration.toInt()) > 0 && (dircode == 1 || dircode == 2)) {

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


        }


        /////
        cursor.close()


        return callLogList

    }


}




