@file:Suppress("DEPRECATED_IDENTITY_EQUALS")

package com.bbcsolution.smartagentsms.Receiver

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bbcsolution.smartagentsms.Analyzer
import com.bbcsolution.smartagentsms.MainActivity
import com.bbcsolution.smartagentsms.R
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class SmartReceiver : BroadcastReceiver() {

    var srvEnabled = "enabled"
    var phoneNumber: String? = null
    private var prevState = 0
    private lateinit var obj: Analyzer

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context?, intent: Intent?) {

        if ("android.intent.action.BOOT_COMPLETED" == intent!!.action) {
            val service = Intent(context, SmartReceiver::class.java)
            context?.startService(service)
        }

        obj = Analyzer(context!!)

        if (obj["user_authenticated"].equals("paid_customer") || obj["user_authenticated"].equals("trial_customer")) {

            //========================== Notification Service ==========================
            if (obj["smart_notification"] != null) {
                if (obj["smart_notification"].equals(srvEnabled)) {
                    val channelId = "com.bbcsolution.smartagentsms@2021"
                    val channelName = "BBC Smart Agent SMS"
                    val nm: NotificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val nb = NotificationCompat.Builder(context, channelId)
                    NotificationManagerCompat.from(context).cancelAll()
                    nb.setContentTitle("BBC SMART AGENT SMS")
                    nb.setContentText("Smart Agent SMS Service Started")
                    nb.setSmallIcon(R.drawable.smart_agent_sms_logo)
                    nb.setLargeIcon(
                        BitmapFactory.decodeResource(
                            context.resources,
                            R.drawable.smart_agent_sms_logo
                        )
                    )
                    nb.setOngoing(true)
                    Intent(context, SmartReceiver::class.java)
                    Intent(context.applicationContext, SmartReceiver::class.java)
                    //nb.addAction(R.drawable.sms, "Stop Service", );
                    val mainIntent = Intent(context, MainActivity::class.java)
                    @SuppressLint("UnspecifiedImmutableFlag") val srvIntent =
                        PendingIntent.getActivity(
                            context,
                            0,
                            mainIntent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    nb.setContentIntent(srvIntent)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val nc = NotificationChannel(
                            channelId,
                            channelName,
                            NotificationManager.IMPORTANCE_DEFAULT
                        )
                        nm.createNotificationChannel(nc)
                    }
                    nm.notify(System.currentTimeMillis().toInt(), nb.build())
                }
            }

            //========================== Automation Service ==========================
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CALL_LOG
                ) == PackageManager.PERMISSION_GRANTED
            ) {


                val telephonyManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val smsManager = SmsManager.getDefault()
                @SuppressLint("SimpleDateFormat") val date =
                    SimpleDateFormat("dd-MM-yy").format(
                        Calendar.getInstance().time
                    )
                val c = Calendar.getInstance()
                val min = c[Calendar.MINUTE]
                val hour = c[Calendar.HOUR]
                val time = "$hour:$min"

                //========================== Caller SMS ==========================
                if (obj["caller_sms"] != null) {
                    val callStateListener: PhoneStateListener =
                        object : PhoneStateListener() {
                            override fun onCallStateChanged(
                                state: Int,
                                incomingNumber: String
                            ) {
                                if(incomingNumber.isNotEmpty()) {
                                    phoneNumber = incomingNumber

                                    when (state) {
                                        TelephonyManager.CALL_STATE_RINGING -> {
                                            Log.i("CallState", "SomeOne is Calling!")
                                            prevState = state
                                        }
                                        TelephonyManager.CALL_STATE_OFFHOOK -> {
                                            Log.i("CallState", "Call been Received...")
                                            prevState = state
                                        }
                                        TelephonyManager.CALL_STATE_IDLE -> {
                                            Log.i("CallState", "CALL_STATE_IDLE==> $phoneNumber")
                                            if (prevState === TelephonyManager.CALL_STATE_OFFHOOK) {
                                                prevState = state
                                                //Answered Call which is ended

                                                if (obj["call_receiving"] != null) {
                                                    if (obj["call_receiving"].equals(srvEnabled)) {

                                                        //========================== Caller SMS ==========================
                                                        if (obj["sms_service"] != null && obj["smart_sms_service"] != null) {
                                                            if (obj["sms_service"]
                                                                    .equals("enabled") && obj["smart_sms_service"]
                                                                    .equals("enabled")
                                                            ) {
                                                                obj.sendReceivingMsg(
                                                                    smsManager,
                                                                    obj,
                                                                    date,
                                                                    time,
                                                                    phoneNumber.toString()
                                                                )
                                                            }
                                                        }
                                                    }

                                                    if (prevState === TelephonyManager.CALL_STATE_RINGING) {
                                                        prevState = state
                                                        //Rejected or Missed call
                                                        if (obj["missed_call_sms"].equals("enabled")) {
                                                            val missSms = smsManager.divideMessage(
                                                                obj["missed_call_msg"]
                                                                    .toString() + "\nOn: " + date + "\nAt: " + time
                                                            )
                                                            smsManager.sendMultipartTextMessage(
                                                                phoneNumber,
                                                                null,
                                                                missSms,
                                                                null,
                                                                null,
                                                            )
                                                        }
                                                    }

                                                    if (prevState === TelephonyManager.CALL_STATE_IDLE) {
                                                        prevState = state
                                                        if (obj["dial_call_messaging"] != null) {
                                                            obj.sendDialMsg(smsManager, obj, date, time, incomingNumber)
                                                        }
                                                    }
                                                }
                                            }
                                        }


//                                try {
//
//                                    val callState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
//
//                                    if (callState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
//                                        Toast.makeText(
//                                            context,
//                                            "SomeOne is Calling You.",
//                                            Toast.LENGTH_LONG
//                                        ).show()
//
//                                        //========================== Missed Call SMS ==========================
//                                        if (obj["sms_service"] != null) {
//                                            if (obj["missed_call_msg"] != null && obj["missed_call_sms"] != null) {
//
//                                                if (obj["sms_service"] != null && obj["smart_sms_service"] != null) {
//                                                    if (obj["sms_service"]
//                                                            .equals("enabled") && obj["smart_sms_service"]
//                                                            .equals("enabled")
//                                                    ) {
//                                                        obj.sendMissedCall(
//                                                            smsManager,
//                                                            obj,
//                                                            date,
//                                                            time,
//                                                            phoneNumber
//                                                        )
//                                                    }
//                                                }
//
//                                                if (obj["missed_call_sms"].equals("enabled")) {
//                                                    while (missCallSmsStatus == 0) {
//                                                        val missSms = smsManager.divideMessage(
//                                                            obj["missed_call_msg"]
//                                                                .toString() + "\nOn: " + date + "\nAt: " + time
//                                                        )
//                                                        smsManager.sendMultipartTextMessage(
//                                                            phoneNumber,
//                                                            null,
//                                                            missSms,
//                                                            null,
//                                                            null,
//                                                        )
//                                                        missCallSmsStatus = 1
//                                                    }
//                                                }
//
//                                            }
//                                        }
//
//                                        //========================== Missed Call Whatsapp ==========================
//                                        if (obj["whatsapp_service"] != null) {
//                                            if (obj["whatsapp_service"].equals("enabled")) {
//                                                while (missCallWhatsappStatus == 0) {
//                                                    val whatsappIntent = Intent(
//                                                        Intent.ACTION_VIEW,
//                                                        Uri.parse(
//                                                            "https://api.whatsapp.com/send?phone=$phoneNumber&text=" + obj["missed_call_msg"]
//                                                        )
//                                                    )
//                                                    context.startActivity(whatsappIntent)
//                                                    missCallWhatsappStatus = 1
//                                                }
//                                            }
//                                        }
//                                    }
//                                    if (callState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
//                                        Toast.makeText(context,"Received!",Toast.LENGTH_SHORT).show()
//                                    }
//
//                                    if (callState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
//
//                                        Toast.makeText(context,"Received!",Toast.LENGTH_SHORT).show()
//
//                                        Toast.makeText(
//                                            context, "sms sent!",
//                                            Toast.LENGTH_LONG
//                                        ).show()
//
//                                        if (obj["call_receiving"] != null) {
//                                            if (obj["call_receiving"].equals(srvEnabled)) {
//
//                                                //========================== Caller SMS ==========================
//                                                if (obj["sms_service"] != null && obj["smart_sms_service"] != null) {
//                                                    if (obj["sms_service"]
//                                                            .equals("enabled") && obj["smart_sms_service"]
//                                                            .equals("enabled")
//                                                    ) {
//
//                                                        Toast.makeText(
//                                                            context,
//                                                            "Call been ended!",
//                                                            Toast.LENGTH_LONG
//                                                        ).show()
//
//                                                        obj.sendReceivingMsg(
//                                                            smsManager,
//                                                            obj,
//                                                            date,
//                                                            time,
//                                                            phoneNumber
//                                                        )
//
//                                                    }
//                                                }
//
//
//                                                if (obj["whatsapp_service"] != null) {
//                                                    if (obj["whatsapp_service"].equals("enabled")) {
//                                                        while (whatsappStatus == 0) {
//                                                            val whatsappIntent = Intent(
//                                                                Intent.ACTION_VIEW,
//                                                                Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=" + obj["caller_sms"])
//                                                            )
//                                                            context.startActivity(whatsappIntent)
//                                                            whatsappStatus = 1
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//
//
////                                        if (obj["dial_call_messaging"] != null) {
////                                            obj.sendDialMsg(smsManager, obj, date, time, phoneNumber)
////                                        }
//                                    }
//
//
//                                } catch (e: Exception) {
//                                    e.printStackTrace()
//                                }

//                                if (state == TelephonyManager.CALL_STATE_RINGING) {
//                                    Toast.makeText(
//                                        context,
//                                        "SomeOne is Calling You.",
//                                        Toast.LENGTH_LONG
//                                    ).show()
//                                }


//                                if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
//                                    if (obj["call_receiving"] != null) {
//                                        if (obj["call_receiving"].equals(srvEnabled)) {
//
//                                            //========================== Caller SMS ==========================
//                                            if (obj["sms_service"] != null && obj["smart_sms_service"] != null) {
//                                                if (obj["sms_service"]
//                                                        .equals("enabled") && obj["smart_sms_service"]
//                                                        .equals("enabled")
//                                                ) {
//                                                    obj.sendReceivingMsg(
//                                                        smsManager,
//                                                        obj,
//                                                        date,
//                                                        time,
//                                                        phoneNumber
//                                                    )
//                                                }
//                                            }
//
//                                            if (obj["whatsapp_service"] != null) {
//                                                if (obj["whatsapp_service"].equals("enabled")) {
//                                                    while (whatsappStatus == 0) {
//                                                        val whatsappIntent = Intent(
//                                                            Intent.ACTION_VIEW,
//                                                            Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=" + obj["caller_sms"])
//                                                        )
//                                                        context.startActivity(whatsappIntent)
//                                                        whatsappStatus = 1
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                                if (state == TelephonyManager.CALL_STATE_IDLE) {
//                                    if (obj["dial_call_messaging"] != null) {
//                                        obj.sendDialMsg(smsManager, obj, date, time, phoneNumber)
//                                    }
//                                }
                                        }
                                    }

//                                try {
//
//                                    val callState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
//
//                                    if (callState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
//                                        Toast.makeText(
//                                            context,
//                                            "SomeOne is Calling You.",
//                                            Toast.LENGTH_LONG
//                                        ).show()
//
//                                        //========================== Missed Call SMS ==========================
//                                        if (obj["sms_service"] != null) {
//                                            if (obj["missed_call_msg"] != null && obj["missed_call_sms"] != null) {
//
//                                                if (obj["sms_service"] != null && obj["smart_sms_service"] != null) {
//                                                    if (obj["sms_service"]
//                                                            .equals("enabled") && obj["smart_sms_service"]
//                                                            .equals("enabled")
//                                                    ) {
//                                                        obj.sendMissedCall(
//                                                            smsManager,
//                                                            obj,
//                                                            date,
//                                                            time,
//                                                            phoneNumber
//                                                        )
//                                                    }
//                                                }
//
//                                                if (obj["missed_call_sms"].equals("enabled")) {
//                                                    while (missCallSmsStatus == 0) {
//                                                        val missSms = smsManager.divideMessage(
//                                                            obj["missed_call_msg"]
//                                                                .toString() + "\nOn: " + date + "\nAt: " + time
//                                                        )
//                                                        smsManager.sendMultipartTextMessage(
//                                                            phoneNumber,
//                                                            null,
//                                                            missSms,
//                                                            null,
//                                                            null,
//                                                        )
//                                                        missCallSmsStatus = 1
//                                                    }
//                                                }
//
//                                            }
//                                        }
//
//                                        //========================== Missed Call Whatsapp ==========================
//                                        if (obj["whatsapp_service"] != null) {
//                                            if (obj["whatsapp_service"].equals("enabled")) {
//                                                while (missCallWhatsappStatus == 0) {
//                                                    val whatsappIntent = Intent(
//                                                        Intent.ACTION_VIEW,
//                                                        Uri.parse(
//                                                            "https://api.whatsapp.com/send?phone=$phoneNumber&text=" + obj["missed_call_msg"]
//                                                        )
//                                                    )
//                                                    context.startActivity(whatsappIntent)
//                                                    missCallWhatsappStatus = 1
//                                                }
//                                            }
//                                        }
//                                    }
//                                    if (callState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
//                                        Toast.makeText(context,"Received!",Toast.LENGTH_SHORT).show()
//                                    }
//
//                                    if (callState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
//
//                                        Toast.makeText(context,"Received!",Toast.LENGTH_SHORT).show()
//
//                                        Toast.makeText(
//                                            context, "sms sent!",
//                                            Toast.LENGTH_LONG
//                                        ).show()
//
//                                        if (obj["call_receiving"] != null) {
//                                            if (obj["call_receiving"].equals(srvEnabled)) {
//
//                                                //========================== Caller SMS ==========================
//                                                if (obj["sms_service"] != null && obj["smart_sms_service"] != null) {
//                                                    if (obj["sms_service"]
//                                                            .equals("enabled") && obj["smart_sms_service"]
//                                                            .equals("enabled")
//                                                    ) {
//
//                                                        Toast.makeText(
//                                                            context,
//                                                            "Call been ended!",
//                                                            Toast.LENGTH_LONG
//                                                        ).show()
//
//                                                        obj.sendReceivingMsg(
//                                                            smsManager,
//                                                            obj,
//                                                            date,
//                                                            time,
//                                                            phoneNumber
//                                                        )
//
//                                                    }
//                                                }
//
//
//                                                if (obj["whatsapp_service"] != null) {
//                                                    if (obj["whatsapp_service"].equals("enabled")) {
//                                                        while (whatsappStatus == 0) {
//                                                            val whatsappIntent = Intent(
//                                                                Intent.ACTION_VIEW,
//                                                                Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=" + obj["caller_sms"])
//                                                            )
//                                                            context.startActivity(whatsappIntent)
//                                                            whatsappStatus = 1
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//
//
////                                        if (obj["dial_call_messaging"] != null) {
////                                            obj.sendDialMsg(smsManager, obj, date, time, phoneNumber)
////                                        }
//                                    }
//
//
//                                } catch (e: Exception) {
//                                    e.printStackTrace()
//                                }

//                                if (state == TelephonyManager.CALL_STATE_RINGING) {
//                                    Toast.makeText(
//                                        context,
//                                        "SomeOne is Calling You.",
//                                        Toast.LENGTH_LONG
//                                    ).show()
//                                }


//                                if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
//                                    if (obj["call_receiving"] != null) {
//                                        if (obj["call_receiving"].equals(srvEnabled)) {
//
//                                            //========================== Caller SMS ==========================
//                                            if (obj["sms_service"] != null && obj["smart_sms_service"] != null) {
//                                                if (obj["sms_service"]
//                                                        .equals("enabled") && obj["smart_sms_service"]
//                                                        .equals("enabled")
//                                                ) {
//                                                    obj.sendReceivingMsg(
//                                                        smsManager,
//                                                        obj,
//                                                        date,
//                                                        time,
//                                                        phoneNumber
//                                                    )
//                                                }
//                                            }
//
//                                            if (obj["whatsapp_service"] != null) {
//                                                if (obj["whatsapp_service"].equals("enabled")) {
//                                                    while (whatsappStatus == 0) {
//                                                        val whatsappIntent = Intent(
//                                                            Intent.ACTION_VIEW,
//                                                            Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=" + obj["caller_sms"])
//                                                        )
//                                                        context.startActivity(whatsappIntent)
//                                                        whatsappStatus = 1
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                                if (state == TelephonyManager.CALL_STATE_IDLE) {
//                                    if (obj["dial_call_messaging"] != null) {
//                                        obj.sendDialMsg(smsManager, obj, date, time, phoneNumber)
//                                    }
//                                }
                            }
                        }
                    telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE)
                    telephonyManager.callState

                }
            }

            if (obj["user_authenticated"].equals("trial_customer")) {
                val requestAuthentication: StringRequest =
                    object : StringRequest(Method.POST, obj.userInfo() + obj.trialInfo(),
                        Response.Listener { response: String? ->
                            try {
                                val responseObject = JSONObject(response.toString())
                                val responseArray =
                                    responseObject.getJSONArray("SMART_AGENT_SMS_TRIAL_CUSTOMERS")
                                for (i in 0 until responseArray.length()) {
                                    val jsonObject = responseArray.getJSONObject(i)
                                    val customerName = jsonObject.getString("customer_name")
                                    val customerExpiry =
                                        jsonObject.getString("customer_expiry_date")
                                    if (customerName == obj["user_name"]) {
                                        @SuppressLint("SimpleDateFormat") val dateFormat =
                                            SimpleDateFormat("yyyy-MM-dd")
                                        val calendar = Calendar.getInstance()
                                        val date = dateFormat.format(calendar.time)
                                        obj.save("customer_expiry_date", customerExpiry)
                                        if (customerExpiry == date) {
                                            obj.save("trial_user_status", "expired")
                                        } else {
                                            if (obj["trial_user_status"] != null) {
                                                obj.exitData("trial_user_status")
                                            }
                                        }
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        },
                        Response.ErrorListener { obj.checkConnection() }) {
                        override fun getParams(): Map<String, String> {
                            val map: MutableMap<String, String> = HashMap()
                            map["access_token"] = obj.icgTag() + obj.icgTrialToken()
                            map["customer_name"] = obj["user_name"]!!
                            map["customer_email"] = obj["user_email"]!!
                            return map
                        }
                    }
                val queue: RequestQueue = Volley.newRequestQueue(context)
                queue.add(requestAuthentication)
            }

        }
    }
}