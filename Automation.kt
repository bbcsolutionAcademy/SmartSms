package com.bbcsolution.smartagentsms

import android.Manifest
import android.content.Intent
import androidx.annotation.RequiresApi
import android.os.Build
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.IBinder
import android.os.SystemClock
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bbcsolution.smartagentsms.Receiver.SmartReceiver
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Automation : Service() {


    private lateinit var obj: Analyzer
    private lateinit var context: Context
    var srvEnabled = "enabled"

    override fun onCreate() {
        Log.d("Automation", "Started")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        context = this@Automation

        if (obj["user_authenticated"].equals("trial_customer")) {
            val requestAuthentication: StringRequest = object : StringRequest(
                Method.POST, obj.userInfo() + obj.trialInfo(),
                Response.Listener { response: String? ->
                    try {
                        val responseObject = JSONObject(response.toString())
                        val responseArray = responseObject.getJSONArray("SMART_AGENT_SMS_TRIAL_CUSTOMERS")
                        for (i in 0 until responseArray.length()) {
                            val jsonObject = responseArray.getJSONObject(i)
                            val customerName = jsonObject.getString("customer_name")
                            val customerExpiry = jsonObject.getString("customer_expiry_date")
                            if (customerName == obj["user_name"]) {
                                @SuppressLint("SimpleDateFormat") val dateFormat = SimpleDateFormat("yyyy-MM-dd")
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
            val queue: RequestQueue = Volley.newRequestQueue(context);
            queue.add(requestAuthentication)
        }

        if (obj["user_authenticated"].equals("paid_customer") && obj["user_authenticated"].equals("trial_customer")) {

            if (obj["smart_notification"] != null && obj["accessibility_service"] != null) {
                showNotification()
            }

            if (obj["accessibility_service"] != null) {
                if (obj["accessibility_service"].equals("enabled")) {
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
                        if (obj["caller_sms"] != null) {
                            val callStateListener: PhoneStateListener =
                                object : PhoneStateListener() {
                                    override fun onCallStateChanged(
                                        state: Int,
                                        incomingNumber: String
                                    ) {
                                        var missCallSmsStatus = 0
                                        var missCallWhatsappStatus = 0
                                        var smsStatus = 0
                                        var whatsappStatus = 0
                                        if (state == TelephonyManager.CALL_STATE_RINGING) {
                                            Toast.makeText(
                                                context, "SomeOne is Calling You.",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            // Missed Call SMS
                                            if (obj["sms_service"] != null) {
                                                if (obj["missed_call_msg"] != null && obj["missed_call_sms"] != null) {
                                                    if (obj["missed_call_sms"].equals("enabled")) {
                                                        while (missCallSmsStatus == 0) {
                                                            val missSms = smsManager.divideMessage(
                                                                obj["missed_call_msg"]
                                                                    .toString() + "\nOn: " + date + "\nAt: " + time
                                                            )
                                                            smsManager.sendMultipartTextMessage(
                                                                incomingNumber,
                                                                null,
                                                                missSms,
                                                                null,
                                                                null
                                                            )
                                                            missCallSmsStatus++
                                                        }
                                                    }
                                                }
                                            }

                                            // Missed Call Whatsapp
                                            if (obj["whatsapp_service"] != null) {
                                                if (obj["whatsapp_service"].equals("enabled")) {
                                                    while (missCallWhatsappStatus == 0) {
                                                        val whatsappIntent = Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse(
                                                                "https://api.whatsapp.com/send?phone=$incomingNumber&text=" + obj["missed_call_msg"]
                                                            )
                                                        )
                                                        context.startActivity(whatsappIntent)
                                                        missCallWhatsappStatus++
                                                    }
                                                }
                                            }
                                        }
                                        if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                                            if (obj["call_receiving"] != null) {
                                                if (obj["call_receiving"].equals(srvEnabled)) {

                                                    // Caller SMS
                                                    if (obj["sms_service"] != null && obj["smart_sms_service"] != null) {
                                                        if (obj["sms_service"]
                                                                .equals("enabled") && obj["smart_sms_service"]
                                                                .equals("enabled")
                                                        ) {
                                                            while (smsStatus == 0) {
                                                                val sms = smsManager.divideMessage(
                                                                    obj["caller_sms"]
                                                                        .toString() + "\nOn: " + date + "\nAt: " + time
                                                                )
                                                                smsManager.sendMultipartTextMessage(
                                                                    incomingNumber,
                                                                    null,
                                                                    sms,
                                                                    null,
                                                                    null
                                                                )
                                                                Toast.makeText(
                                                                    context,
                                                                    "SMS Sent",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                smsStatus++
                                                            }
                                                        }
                                                    }
                                                    if (obj["whatsapp_service"] != null) {
                                                        if (obj["whatsapp_service"].equals("enabled")) {
                                                            while (whatsappStatus == 0) {
                                                                val whatsappIntent = Intent(
                                                                    Intent.ACTION_VIEW,
                                                                    Uri.parse(
                                                                        "https://api.whatsapp.com/send?phone=" + incomingNumber + "&text=" + obj!!.get(
                                                                            "caller_sms"
                                                                        )
                                                                    )
                                                                )
                                                                context.startActivity(whatsappIntent)
                                                                whatsappStatus++
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (state == TelephonyManager.CALL_STATE_IDLE) {
                                            if (incomingNumber == "") {
                                                //Toast.makeText(context, "No Call", Toast.LENGTH_SHORT).show();
                                            } else {
                                                //Toast.makeText(context, "No Call", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }
                            telephonyManager.listen(
                                callStateListener,
                                PhoneStateListener.LISTEN_CALL_STATE
                            )
                            telephonyManager.callState
                        } //Toast.makeText(context, "Not Available", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        return START_STICKY
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onTaskRemoved(rootIntent: Intent) {
        val restartService = Intent(applicationContext, this.javaClass)
        restartService.setPackage(packageName)
        @SuppressLint("UnspecifiedImmutableFlag") val restartServicePI = PendingIntent.getService(
            applicationContext, 1, restartService,
            PendingIntent.FLAG_ONE_SHOT
        )
        val alarmService = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmService[AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000] =
            restartServicePI
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        Log.d("Automation", "Destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        private const val serviceEnabled = "enabled"
    }


    private fun showNotification() {
        val channelId = "smart.agent@sms.gateway"
        val channelName = "Smart Agent SMS"
        val nm: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val nb = NotificationCompat.Builder(context, channelId)
        NotificationManagerCompat.from(context).cancelAll()
        nb.setContentTitle("SMART AGENT SMS")
        nb.setContentText("Smart Agent SMS Service Started")
        nb.setSmallIcon(R.drawable.smart_agent_sms_logo)
        nb.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.smart_agent_sms_logo))
        nb.setOngoing(true)
        val intent = Intent(context, SmartReceiver::class.java)
        val stopService = Intent(context.applicationContext, SmartReceiver::class.java)
        //nb.addAction(R.drawable.sms, "Stop Service", );
        val mainIntent = Intent(context, MainActivity::class.java)
        @SuppressLint("UnspecifiedImmutableFlag") val srvIntent =
            PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_CANCEL_CURRENT)
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