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

    fun userInfo(): String {
        return "https://agentlic.in/bbcsolution-base/smart-agent/"
    }

    fun userRegistration(): String {
        return "smart_agent_new_registration.php"
    }

    fun userKey(): String {
        return k20 + k20 + g92 + s23 + h73
    }

    fun userRegistered(): String {
        return "user_registered"
    }

    fun checkConnection(): String {
        return "Check your Internet connection!"
    }

    fun authentication(): String {
        return "smart_agent_sms_authentication.php"
    }

    fun token(): String {
        return "permission_token"
    }

    fun tokenKey(): String {
        return "BBC-SAS"
    }

    fun trialRegistration(): String {
        return "smart_agent_sms_trial.php";
    }

    fun checkLicense(license: EditText) {
        if (license.text.toString().length == 5) {
            license.append("-")
        }
        if (license.text.toString().length == 10) {
            license.append("-")
        }
        if (license.text.toString().length == 15) {
            license.append("-")
        }
        if (license.text.toString().length == 20) {
            license.append("-")
        }
    }

    companion object {
        private const val g92 = "C"
        private const val h73 = "G"
        private const val k20 = "B"
        private const val s23 = "I"
    }

    init {
        sharedPreferences = context.getSharedPreferences("SessionId", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    fun trialApi(): String {
        return "BBCSAS-TRIAL"
    }

    fun trialInfo(): String {
        return "smart_agent_sms_trial_info.php"
    }

    fun icgTag(): String {
        return "#"
    }

    fun icgTrialToken(): String {
        return "BBCSAS-TRIAL"
    }

    fun userLicenseVerification(): String {
        return "smart_agent_sms_license_verification.php"
    }

    fun licenseVerificationToken(): String {
        return "smart_agent_license_key"
    }

    fun authVerification(): String {
        return "SAS_AUTH_VERIFICATION"
    }

    fun licenseVerificationTokenKey(): String {
        return "smart_agent_verification_token"
    }

    fun registerPaidUser(): String {
        return "smart_agent_new_registration.php"
    }

    fun product(): String {
        return "customer_product"
    }

    fun device(): String {
        return "register_device"
    }

    fun ourProduct(): String {
        return "SMART AGENT SMS"
    }

    fun regName(): String {
        return "register_name"
    }
    fun regEmail(): String {
        return "register_email"
    }
    fun regContact(): String {
        return "register_contact"
    }
    fun regBranch(): String {
        return "register_branch"
    }
    fun regLicense(): String {
        return "register_license_key"
    }
    fun customerInfo(): String {
        return "customer_info"
    }
    fun loginCustomer(): String {
        return "paid_customer"
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