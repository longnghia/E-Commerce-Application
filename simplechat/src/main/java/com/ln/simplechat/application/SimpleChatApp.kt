package com.ln.simplechat.application

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.luck.picture.lib.thread.PictureThreadUtils

class SimpleChatApp {

    companion object {
        val TAG = SimpleChatApp::class.java.simpleName
        var currentChannel: String? = null
        var instance: Application? = null

        private val mActivityLifecycleCallbacks = ChatActivityLifecycleCallbacks()

        fun install(application: Application) {
            instance = application
            application.registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
        }

        fun currentActivity(): Activity? {
            return mActivityLifecycleCallbacks.currentActivity
        }
    }

    class ChatActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        var currentActivity: Activity? = null
        override fun onActivityCreated(p0: Activity, p1: Bundle?) {}
        override fun onActivityStarted(p0: Activity) {
            currentActivity = p0
        }

        override fun onActivityResumed(p0: Activity) {
            currentActivity = p0
        }

        override fun onActivityPaused(p0: Activity) {}
        override fun onActivityStopped(p0: Activity) {}
        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
        override fun onActivityDestroyed(p0: Activity) {}
    }
}

fun toast(stringRes: Int, activity: Activity? = null) {
    (activity ?: SimpleChatApp.currentActivity())?.apply {
        val string = getString(stringRes)
        PictureThreadUtils.runOnUiThread {
            Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
        }
        Log.d(SimpleChatApp.TAG, string)
    }
}

fun toast(string: String?, activity: Activity? = null) {
    if (string != null) {
        (activity ?: SimpleChatApp.currentActivity())?.apply {
            PictureThreadUtils.runOnUiThread {
                Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
            }
        }
        println(string)
    }
}

fun toast(exception: Exception, activity: Activity? = null) {
    (activity ?: SimpleChatApp.currentActivity())?.apply {
        val string = exception.message
        PictureThreadUtils.runOnUiThread {
            Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
        }
        exception.printStackTrace()
    }
}