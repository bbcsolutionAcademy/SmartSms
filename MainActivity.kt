package com.bbcsolution.smartagentsms

import java.util.*
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import android.provider.Settings.SettingNotFoundException
import android.telephony.TelephonyManager
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import com.bbcsolution.smartagentsms.Receiver.SmartReceiver
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.luseen.spacenavigation.SpaceItem
import com.luseen.spacenavigation.SpaceNavigationView
import com.luseen.spacenavigation.SpaceOnClickListener
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private lateinit var smartAgentSpaceNav: SpaceNavigationView
    var context: Context? = null
    var myDrawer: DrawerLayout? = null
    var myNav: NavigationView? = null
    var myNavMenu: ActionBarDrawerToggle? = null
    var display: View? = null
    private lateinit var analyzer: Analyzer
    var btnFloatx: FloatingActionButton? = null
    var itemsContainer: LinearLayout? = null
    var smartSetting: ImageView? = null
    var data: TextView? = null
    var settingsLayout: ConstraintLayout? = null
    var smsSettings:ConstraintLayout? = null
    var userCallSummary: ListView? = null
    private var receiver: SmartReceiver = SmartReceiver()
    private lateinit var snackView: View

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE)

        context = this@MainActivity
        analyzer = Analyzer(context as MainActivity)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        myDrawer = findViewById(R.id.smart_agent_drawer)
        myNav = findViewById(R.id.sms_navigation)
        display = findViewById(android.R.id.content)
        btnFloatx = findViewById(R.id.btn_floatx)
        itemsContainer = findViewById(R.id.items_container)
        settingsLayout = findViewById(R.id.settings_layout)
        smartSetting = findViewById(R.id.smart_setting)
        smsSettings = findViewById(R.id.sms_settings)
        userCallSummary = findViewById(R.id.user_call_summary)
        snackView = findViewById(android.R.id.content)
        smartAgentSpaceNav = findViewById(R.id.smartAgentSpaceNav)

        val toggle = arrayOf("open")
        smartAgentSpaceNav.initWithSaveInstanceState(savedInstanceState)
        smartAgentSpaceNav.addSpaceItem(SpaceItem("Settings", R.drawable.settings))
        smartAgentSpaceNav.addSpaceItem(SpaceItem("Bulk Whatsapp", R.drawable.smart_message_ico))
        smartAgentSpaceNav.setCentreButtonIcon(R.drawable.smart_dashboard)
        smartAgentSpaceNav.setInActiveCentreButtonIconColor(resources.getColor(R.color.old_smart_agent_header))
        smartAgentSpaceNav.setActiveCentreButtonBackgroundColor(resources.getColor(R.color.old_smart_agent_header))
        smartAgentSpaceNav.setSpaceOnClickListener(object : SpaceOnClickListener {
            override fun onCentreButtonClick() {
                startActivity(Intent(this@MainActivity, MainActivity::class.java))
                finish()
            }
            override fun onItemClick(itemIndex: Int, itemName: String) {
                if (itemIndex==0) {
                    startActivity(Intent(this@MainActivity, Settings::class.java))
                }
            }
            override fun onItemReselected(itemIndex: Int, itemName: String) {
                if (itemIndex==0) {
                    startActivity(Intent(this@MainActivity, Settings::class.java))
                }
            }
        })

        checkTrialUserStatus(analyzer["trial_user_status"])

        btnFloatx?.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.smart_light))
        btnFloatx?.setOnClickListener {
            if (toggle[0] == "open") {
                itemsContainer?.visibility = View.VISIBLE
                toggle[0] = "close"
            } else if (toggle[0] == "close") {
                itemsContainer?.visibility = View.GONE
                toggle[0] = "open"
            }
        }
        smsSettings?.setOnClickListener {
            //startActivity(new Intent(MainActivity.this, sms_manager.class));
        }

        settingsLayout?.setOnClickListener {
            startActivity(
                Intent(this@MainActivity, Settings::class.java)
            )
        }

        myNavMenu = ActionBarDrawerToggle(this@MainActivity, myDrawer, R.string.open, R.string.close)
        myDrawer?.addDrawerListener(myNavMenu!!)
        myNavMenu!!.syncState()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Dexter.withContext(this@MainActivity).withPermissions(Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.WAKE_LOCK)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {

                        try {
                            val accessibilityEnabled = android.provider.Settings.Secure.getInt(
                                contentResolver,
                                android.provider.Settings.Secure.ACCESSIBILITY_ENABLED
                            )

                            if (accessibilityEnabled==0) {
                                Snackbar.make(snackView, "Accessibility Service", Snackbar.LENGTH_INDEFINITE).setBackgroundTint(resources.getColor(R.color.bbc_opx)).setTextColor(resources.getColor(R.color.bbc_light)).setAction("Enable", View.OnClickListener {
                                    startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                }).show()
                            }


                        } catch (e: SettingNotFoundException) {
                            e.printStackTrace()
                        }
                    }
                    override fun onPermissionRationaleShouldBeShown(
                        list: List<PermissionRequest>,
                        permissionToken: PermissionToken
                    ) {
                        permissionToken.continuePermissionRequest()
                    }
                }).check()
        }

        loadData()
    }

    override fun onStart() {
        val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        registerReceiver(receiver, filter)
        super.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.header_items, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun loadData() {
        try {
            val list = ArrayList<String>()
            val managedCursor = managedQuery(
                CallLog.Calls.CONTENT_URI, null,
                null, null, null
            )
            val number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER)
            val type = managedCursor.getColumnIndex(CallLog.Calls.TYPE)
            val date = managedCursor.getColumnIndex(CallLog.Calls.DATE)
            val name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION)
            while (managedCursor.moveToNext()) {
                val phNumber = managedCursor.getString(number)
                val callType = managedCursor.getString(type)
                val callDate = managedCursor.getString(date)
                var userName: String? = null
                userName = managedCursor.getString(name)
                val callDayTime = Date(callDate.toLong())
                val callDuration = managedCursor.getString(duration)
                var dir: String? = null
                val dircode = callType.toInt()
                when (dircode) {
                    CallLog.Calls.OUTGOING_TYPE -> dir = "Dialled Call"
                    CallLog.Calls.INCOMING_TYPE -> dir = "Incoming Call"
                    CallLog.Calls.MISSED_TYPE -> dir = "Missed Call"
                }
                //sb.append("Name : $user_name\n" + "Phone Number: $phNumber \n" + "Call Type: $dir \n" + "Call Date: $callDayTime \n" + "Call duration in sec : $callDuration")
                if (userName==null) {
                    list.add("Mobile No : $phNumber \n" +
                            "Call Type : $dir \n" +
                            "Call Date: $callDayTime \n" +
                            "Call Duration : $callDuration")
                } else {
                    list.add("Name : $userName\n" +
                            "Mobile No : $phNumber \n" +
                            "Call Type : $dir \n" +
                            "Call Date: $callDayTime \n" +
                            "Call Duration : $callDuration")
                }

                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, list)
                userCallSummary?.adapter = adapter
            }
            managedCursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkTrialUserStatus(trial_user_status: String?) {
        if (trial_user_status != null) {
            if (analyzer["trial_user_status"].equals("expired")) {
                contactSupport()
            }
        } else {
            if (analyzer["user_authenticated"].equals("trial_customer")) {
                showTrialUserTemplate()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showTrialUserTemplate() {
        val builder = AlertDialog.Builder(this)
        val customLayout: View = layoutInflater.inflate(R.layout.trial_user_info_expiry, null)
        builder.setView(customLayout)
        val trialUserInfo: TextView = customLayout.findViewById(R.id.trial_user_information)
        val submit: TextView = customLayout.findViewById(R.id.trial_ui_ok)
        val buy: TextView = customLayout.findViewById(R.id.trial_ui_buy_now)

        if (analyzer["customer_expiry_date"] != null) {
            val expiryDate: String = analyzer["customer_expiry_date"]!!
            trialUserInfo.textAlignment = View.TEXT_ALIGNMENT_INHERIT
            trialUserInfo.text = """
                Dear ${analyzer["user_name"].toString()},
                
                Your BBC Smart Agent SMS Trial Will Be Expired on $expiryDate
                Get Complete Access to All Premium Content by Purchasing a Subscription of It and Use Smart Agent SMS With LifeTime Validity Without Any Further Interruptions.
                
                We are running with Special Launch Offer for Limited Period. Kindly click on (BUY NOW) button Either Contact Our Team: 
                
                BBC SOLUTION
                Contact: +916370445411
                http://www.bbcsolution.in
                info@bbcsolution.in
                """.trimIndent()
        }
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        submit.setOnClickListener { dialog.dismiss() }
        buy.setOnClickListener {
            val callNow = Intent(Intent.ACTION_VIEW, Uri.parse("tel:+916370445411"))
            startActivity(callNow)
            finish()
        }
    }


    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    private fun contactSupport() {
        val builder = AlertDialog.Builder(this)
        val customLayout: View = layoutInflater.inflate(R.layout.trial_expired, null)
        builder.setView(customLayout)
        val trialUserInfo: TextView = customLayout.findViewById(R.id.trial_paragraph)
        val exitView: TextView = customLayout.findViewById(R.id.exit_view)
        val callNow: TextView = customLayout.findViewById(R.id.call_now)

        exitView.setOnClickListener { exitProcess(0) }
        callNow.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("tel:+916370445411")))
            finish()
        }

        if (analyzer["customer_expiry_date"] != null) {
            trialUserInfo.textAlignment = View.TEXT_ALIGNMENT_INHERIT
            trialUserInfo.text = """
                Dear ${analyzer["user_name"].toString()},
                
                Your Trial Copy has been expired
                We are running with Special Launch Offer for Limited Period. Kindly click on (BUY NOW) button Either Contact Our Team: 
                
                BBC SOLUTION
                Contact: +916370445411
                http://www.bbcsolution.in
                info@bbcsolution.in
                """.trimIndent()
        }
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onBackPressed() {
        startActivity(Intent(this@MainActivity, MainActivity::class.java))
        finish()
        super.onBackPressed()
    }

}