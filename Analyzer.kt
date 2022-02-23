package com.bbcsolution.smartagentsms

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.google.android.material.snackbar.Snackbar
import androidx.annotation.RequiresApi
import android.os.Build
import android.telephony.SmsManager
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Toast
import com.bbcsolution.smartagentsms.Receiver.SmartReceiver

@SuppressLint("CommitPrefEdits")
class Analyzer(var context: Context) {

    var sharedPreferences: SharedPreferences
    var editor: SharedPreferences.Editor

    fun save(key: String?, value: String?) {
        editor.putString(key, value)
        editor.apply()
    }

    operator fun get(key: String?): String? {
        return sharedPreferences.getString(key, null)
    }

    fun exitData(key: String?) {
        editor.remove(key)
        editor.apply()
    }

    fun display(display_type: View?, data: String?) {
        Snackbar.make(display_type!!, data!!, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(context.resources.getColor(R.color.smart_agent_header)).show()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun displayEnable(Display: View?, Msg: String?) {
        Snackbar.make(Display!!, Msg!!, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(context.resources.getColor(R.color.bbc_light))
            .setTextColor(context.getColor(R.color.bbc_dark)).show()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun displayDisable(Display: View?, Msg: String?, button: CompoundButton) {
        Snackbar.make(Display!!, Msg!!, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(context.resources.getColor(R.color.bbc_light))
            .setTextColor(context.getColor(R.color.bbc_dark))
            .setAction("Enable") { button.isChecked = true }
            .show()
    }

    fun hashTag(): String {
        return "#";
    }

    fun userName(): String {
        return "name"
    }

    fun userEmail(): String {
        return "email"
    }

    fun userContact(): String {
        return "contact"
    }

    fun userLicense(): String {
        return "license_key"
    } 

    init {
        sharedPreferences = context.getSharedPreferences("SessionId", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

 
    fun sendReceivingMsg(smsManager: SmsManager?, obj: Analyzer, date: String, time: String, phoneNumber: String) {
        val sms = obj["caller_sms"].toString() + "\nOn: " + date + "\nAt: " + time
        smsManager!!.sendTextMessage(phoneNumber,phoneNumber,sms,null,null)
        Toast.makeText(context,"SMS Sent", Toast.LENGTH_SHORT).show()
    }

    fun sendMissedCall(smsManager: SmsManager?, obj: Analyzer, date: String, time: String, phoneNumber: String) {
        val sms = smsManager?.divideMessage(
            obj["missed_call_msg"].toString() + "\nOn: " + date + "\nAt: " + time
        )
        smsManager?.sendMultipartTextMessage(phoneNumber,null,sms,null,null)
        Toast.makeText(context,"SMS Sent", Toast.LENGTH_SHORT).show()
    }

    fun sendDialMsg(smsManager: SmsManager?, obj: Analyzer, date: String, time: String, phoneNumber: String) {
        val sms = smsManager?.divideMessage(
            obj["dial_call_msg"].toString() + "\nOn: " + date + "\nAt: " + time
        )
        smsManager?.sendMultipartTextMessage(phoneNumber,null,sms,null,null)
        Toast.makeText(context,"SMS Sent", Toast.LENGTH_SHORT).show()
    }

}
