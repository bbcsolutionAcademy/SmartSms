package com.bbcsolution.smartagentsms

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent


class SmartService : AccessibilityService() {

    var analyzer: Analyzer? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName.toString()
        val packageManager = this.packageManager
        try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val applicationLabel = packageManager.getApplicationLabel(applicationInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onInterrupt() {
        TODO("Not yet implemented")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        analyzer = Analyzer(this@SmartService)
        analyzer!!.save("accessibility_service", "enabled")
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or AccessibilityEvent.TYPE_VIEW_FOCUSED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
        info.notificationTimeout = 100
        this.serviceInfo = info
    }

    companion object {
        private const val TAG = "SmartService"
    }

}