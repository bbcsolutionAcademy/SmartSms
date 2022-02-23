package com.bbcsolution.smartagentsms

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import com.bbcsolution.smartagentsms.Import.ImportMessages
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import java.util.*

class Settings : AppCompatActivity() {

    var analyzer: Analyzer? = null
    private lateinit var smartSmsSrvBtn: SwitchMaterial
    private lateinit var  msgSrvBtn:SwitchMaterial
    private lateinit var  callReceivedOption:SwitchMaterial
    private lateinit var  smartNotificationBtn:SwitchMaterial
    private lateinit var  missedCallOption:SwitchMaterial
    private lateinit var  smsOption:SwitchMaterial
    private lateinit var  whatsappOption:SwitchMaterial
    private lateinit var  callerMsg: EditText
    private lateinit var  missedCallMsg:EditText
    private lateinit var  btnSave: MaterialButton
    private lateinit var  saveMissedCallMsg: MaterialButton
    private lateinit var  importCallerMsg:MaterialButton
    private lateinit var  importMissedCallMsg:MaterialButton
    private lateinit var  sendToAll: RadioButton
    private lateinit var  sendToSelected:RadioButton
    private lateinit var  settingsLayout: LinearLayout
    private lateinit var  smsLayout:LinearLayout
    private lateinit var  obj: View
    private lateinit var  selectOptionToSendTheCaller: RadioGroup

    private lateinit var dialCallOption: SwitchCompat
    private lateinit var dialCallMsg: EditText
    private lateinit var importDialMessages: MaterialButton
    private lateinit var saveDialMessage: MaterialButton

    private val srvEnabled = "enabled"
    private val srvDisabled = "disabled"
    private val smartSmsServiceEnabled = "Smart SMS Service Enabled"
    private val messageServiceEnabled = "Message Service Enabled"
    private val smartSmsServiceDisabled = "Smart SMS Service Disabled"
    private val messageServiceDisabled = "Message Service Disabled"
    private val callReceivingEnabled = "Call Receiving Enabled"
    private val callReceivingDisabled = "Call Receiving Disabled"
    private val callerSmsEnabled = "Caller message has been saved"
    private val sendToAllCallerEnabled = "Send to all Caller Enabled"
    private val sendToSelectedCallerEnabled = "Send to Selected Caller Enabled"
    private val smartNotificationEnabled = "Smart Notification Enabled"
    private val smartNotificationDisabled = "Smart Notification Disabled"
    private val missedCallEnabled = "Missed Call Enabled"
    private val missedCallDisabled = "Missed Call Disabled"
    private lateinit var uiTablayout: TabLayout
    var pos1 = 0
    var pos2 = 1

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Settings"
        analyzer = Analyzer(this)
        uiTablayout = findViewById(R.id.tab_layout)
        uiTablayout.newTab().let { uiTablayout.addTab(it.setText("Settings")) }
        uiTablayout.newTab().let { uiTablayout.addTab(it.setText("SMS")) }
        obj = findViewById(android.R.id.content)
        settingsLayout = findViewById(R.id.settings_layout)
        smsLayout = findViewById(R.id.sms_layout)
        callerMsg = findViewById(R.id.receive_msg)
        callReceivedOption = findViewById(R.id.call_recieved_option)
        smartSmsSrvBtn = findViewById(R.id.smart_sms_service_btn)
        msgSrvBtn = findViewById(R.id.message_service_btn)
        obj = findViewById(android.R.id.content)
        btnSave = findViewById(R.id.save_sms_msg)
        smartNotificationBtn = findViewById(R.id.smart_notification_btn)
        selectOptionToSendTheCaller = findViewById(R.id.choose_caller)
        sendToAll = findViewById(R.id.send_to_all_caller)
        sendToSelected = findViewById(R.id.send_to_selected_caller)
        missedCallOption = findViewById(R.id.missed_call_option)
        smsOption = findViewById(R.id.sms_option)
        missedCallMsg = findViewById(R.id.missed_call_msg)
        saveMissedCallMsg = findViewById(R.id.save_missed_call_msg)
        importCallerMsg = findViewById(R.id.import_call_receiving_messages)
        importMissedCallMsg = findViewById(R.id.import_missed_call_messages)
        whatsappOption = findViewById(R.id.whatsapp_option)
        dialCallOption = findViewById(R.id.dial_call_option)
        dialCallMsg = findViewById(R.id.dial_call_msg)
        importDialMessages = findViewById(R.id.import_dial_call_messages)
        saveDialMessage = findViewById(R.id.save_dial_call_msg)
        checkEnabledState()

        val msgIntent = intent
        if (msgIntent.getStringExtra("message") != null && msgIntent.getStringExtra("message_of") != null) {
            smsLayout.visibility = View.VISIBLE
            settingsLayout.visibility = View.GONE
            if (msgIntent.getStringExtra("message_of") == "caller_msg") {
                callerMsg.setText(msgIntent.getStringExtra("message"))
            }
            if (msgIntent.getStringExtra("message_of") == "missedCall_msg") {
                missedCallMsg.setText(msgIntent.getStringExtra("message"))
            }
            if (msgIntent.getStringExtra("message_of") == "dialCall_msg") {
                dialCallMsg.setText(msgIntent.getStringExtra("message"))
            }
        }

        uiTablayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == pos1) {
                    smsLayout.visibility = View.GONE
                    settingsLayout.visibility = View.VISIBLE
                } else if (tab.position == pos2) {
                    settingsLayout.visibility = View.GONE
                    smsLayout.visibility = View.VISIBLE
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        smartSmsSrvBtn.setOnCheckedChangeListener { button: CompoundButton, _: Boolean ->
            if (button.isChecked) {
                analyzer!!.save(
                    "smart_sms_service", srvEnabled
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    analyzer!!.displayEnable(
                        obj, smartSmsServiceEnabled
                    )
                }
            } else if (!button.isChecked) {
                analyzer!!.save(
                    "smart_sms_service", srvDisabled
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    analyzer!!.displayDisable(
                        obj, smartSmsServiceDisabled,
                        button
                    )
                }
            }
        }

        msgSrvBtn.setOnCheckedChangeListener { button: CompoundButton, _: Boolean ->
            if (button.isChecked) {
                analyzer!!.save("msg_service", srvEnabled)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    analyzer!!.displayEnable(
                        obj, messageServiceEnabled
                    )
                }
            } else if (!button.isChecked) {
                analyzer!!.save("msg_service", srvDisabled)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    analyzer!!.displayDisable(
                        obj, messageServiceDisabled,
                        button
                    )
                }
            }
        }

        selectOptionToSendTheCaller.setOnCheckedChangeListener { _: RadioGroup?, i: Int ->
            when (i) {
                R.id.send_to_all_caller -> {
                    analyzer!!.save(
                        "send_to_all_caller", srvEnabled
                    )
                    analyzer!!.displayEnable(
                        obj, sendToAllCallerEnabled
                    )
                }
                R.id.send_to_selected_caller -> {
                    analyzer!!.save(
                        "send_to_selected_caller", srvEnabled
                    )
                    analyzer!!.displayEnable(
                        obj, sendToSelectedCallerEnabled
                    )
                }
            }
        }

        smartNotificationBtn.setOnCheckedChangeListener { button: CompoundButton, _: Boolean ->
            if (button.isChecked) {
                analyzer!!.save(
                    "smart_notification", srvEnabled
                )
                analyzer!!.displayEnable(
                    obj, smartNotificationEnabled
                )
            } else if (!button.isChecked) {
                analyzer!!.save(
                    "smart_notification", srvDisabled
                )
                analyzer!!.displayDisable(
                    obj, smartNotificationDisabled,
                    button
                )
            }
        }

        // SMS Layout
        callReceivedOption.setOnCheckedChangeListener { button: CompoundButton, _: Boolean ->
            if (button.isChecked) {
                analyzer!!.save("call_receiving", srvEnabled)
                analyzer!!.displayEnable(
                    obj, callReceivingEnabled
                )
            } else if (!button.isChecked) {
                analyzer!!.save("call_receiving", srvDisabled)
                analyzer!!.displayDisable(
                    obj, callReceivingDisabled,
                    button
                )
            }
        }

        btnSave.setOnClickListener {
            if (callerMsg.text.toString() == "") {
                callerMsg.error = "Enter Valid Message!"
            } else {
                analyzer!!.save("caller_sms", callerMsg.text.toString())
                analyzer!!.displayEnable(obj, "Caller Message Has Been Saved.")
            }
        }

        // Missed Call Settings
        missedCallOption.setOnCheckedChangeListener { button, _ ->
            if (button.isChecked) {
                analyzer!!.save("missed_call_sms", "enabled")
                analyzer!!.displayEnable(
                    obj, missedCallEnabled
                )
            } else if (!button.isChecked) {

            }
        }

        saveMissedCallMsg.setOnClickListener {
            if (missedCallMsg.text.toString().isEmpty()) {
                missedCallMsg.setText("Please Enter The Message!")
            } else {
                analyzer!!.save(
                    "missed_call_msg",
                    missedCallMsg.text.toString().trim { it <= ' ' })
                analyzer!!.displayEnable(obj, "Missed call message has been Saved.")
            }
        }

        // Send Through
        smsOption.setOnCheckedChangeListener { button, _ ->
            if (button.isChecked) {
                analyzer!!.save("sms_service", srvEnabled)
                analyzer!!.displayEnable(obj, "SMS Service Enabled")
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))

                if (whatsappOption.isChecked) {
                    whatsappOption.isChecked = false
                    analyzer!!.save(
                        "whatsapp_service", srvDisabled
                    )
                }
            } else if (!button.isChecked) {
                analyzer!!.save("sms_service", srvEnabled)
                analyzer!!.displayDisable(obj, "SMS Service Disabled", button)
            }
        }

        whatsappOption.setOnCheckedChangeListener { button, _ ->
            if (button.isChecked) {
                smsOption.isChecked = false
                analyzer!!.save("sms_service", srvDisabled)
                analyzer!!.save("whatsapp_service", srvEnabled)
                analyzer!!.displayEnable(obj, "Whatsapp Service Enabled")
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } else if (!button.isChecked) {
                analyzer!!.save("whatsapp_service", srvEnabled)
                analyzer!!.displayDisable(obj, "Whatsapp Service Disabled", button)
            }
        }

        // Dial Call Settings
        dialCallOption.setOnCheckedChangeListener {button, _ ->
            if (button.isChecked) {
                analyzer!!.save("dial_call_messaging", srvEnabled)
                analyzer!!.displayEnable(obj, "Dial Call Enabled")
            } else if (!button.isChecked) {
                analyzer!!.save("dial_call_messaging", srvEnabled)
                analyzer!!.displayDisable(obj, "Dial Call Disabled", button)
            }
        }
        saveDialMessage.setOnClickListener {
            analyzer?.save("dial_call_msg",dialCallMsg.text.toString())
        }
        importDialMessages.setOnClickListener {
            val importIntent = Intent(this@Settings, ImportMessages::class.java)
            importIntent.putExtra("import", "dialCall_msg")
            startActivity(importIntent)
            finish()
        }


        // Importing Messages
        importCallerMsg.setOnClickListener {
            val importIntent = Intent(this@Settings, ImportMessages::class.java)
            importIntent.putExtra("import", "caller_msg")
            startActivity(importIntent)
            finish()
        }

        importMissedCallMsg.setOnClickListener {
            val importIntent = Intent(this@Settings, ImportMessages::class.java)
            importIntent.putExtra("import", "missedCall_msg")
            startActivity(importIntent)
            finish()
        }
    }


    private fun checkEnabledState() {
        if (analyzer!!["smart_sms_service"] != null) {
            if (analyzer!!["smart_sms_service"].equals(srvEnabled)) {
                smartSmsSrvBtn.isChecked = true
            }
        }
        if (analyzer!!["msg_service"] != null) {
            if (analyzer!!["msg_service"].equals(srvEnabled)) {
                msgSrvBtn.isChecked = true
            }
        }
        if (analyzer!!["call_receiving"] != null) {
            if (analyzer!!["call_receiving"].equals(srvEnabled)) {
                callReceivedOption.isChecked = true
            }
        }
        if (analyzer!!["caller_sms"] != null) {
            callerMsg.setText(analyzer!!["caller_sms"])
        }
        if (analyzer!!["smart_notification"] != null) {
            if (analyzer!!["smart_notification"].equals(srvEnabled)) {
                smartNotificationBtn.isChecked = true
            }
        }
        if (analyzer!!["send_to_all_caller"] != null) {
            if (analyzer!!["send_to_all_caller"].equals(srvEnabled)) {
                sendToAll.isChecked = true
            }
        }
        if (analyzer!!["send_to_selected_caller"] != null) {
            if (analyzer!!["send_to_selected_caller"].equals(srvEnabled)) {
                sendToAll.isChecked = true
            }
        }
        if (analyzer!!["sms_service"] != null) {
            if (analyzer!!["sms_service"].equals(srvEnabled)) {
                smsOption.isChecked = true
            }
        }
        if (analyzer!!["whatsapp_service"] != null) {
            if (analyzer!!["whatsapp_service"].equals(srvEnabled)) {
                whatsappOption.isChecked = true
            }
        }
        if (analyzer!!["missed_call_sms"] != null) {
            if (analyzer!!["missed_call_sms"].equals(srvEnabled)) {
                missedCallOption.isChecked = true
            } else if (analyzer!!["missed_call_sms"].equals(srvDisabled)) {
                missedCallOption.isChecked = false
            }
        }
        if (analyzer!!["missed_call_msg"] != null) {
            missedCallMsg.setText(analyzer!!["missed_call_msg"])
        }

        if (analyzer!!["dial_call_msg"] != null) {
            dialCallMsg.setText(analyzer!!["dial_call_msg"])
        }
        if (analyzer!!["dial_call_messaging"] != null) {
            dialCallOption.isChecked = true
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@Settings, MainActivity::class.java))
        finish()
        super.onBackPressed()
    }
}