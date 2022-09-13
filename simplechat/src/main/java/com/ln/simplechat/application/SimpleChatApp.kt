package com.ln.simplechat.application

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

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
        runOnUiThread {
            Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
        }
        Log.d(SimpleChatApp.TAG, string)
    }
}

fun toast(string: String?, activity: Activity? = null) {
    if (string != null) {
        (activity ?: SimpleChatApp.currentActivity())?.apply {
            runOnUiThread {
                Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
            }
        }
        println(string)
    }
}

fun toast(exception: Exception, activity: Activity? = null) {
    (activity ?: SimpleChatApp.currentActivity())?.apply {
        val string = exception.message
        runOnUiThread {
            Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
        }
        exception.printStackTrace()
    }
}

fun saveData(fileName: String, data: Any?, activity: Context? = null) {
    try {
        val a = activity ?: SimpleChatApp.currentActivity()
        if (a != null) {
            val fos: FileOutputStream = a.openFileOutput(fileName, Context.MODE_PRIVATE)
            val os = ObjectOutputStream(fos)
            os.writeObject(data)
            os.close()
            fos.close()
        }
    } catch (e: Exception) {
        toast(e)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> loadData(fileName: String, activity: Context? = null, toast: Boolean = true): T? {
    val a = activity ?: SimpleChatApp.currentActivity()
    try {
        if (a?.fileList() != null)
            if (fileName in a.fileList()) {
                val fileIS: FileInputStream = a.openFileInput(fileName)
                val objIS = ObjectInputStream(fileIS)
                val data = objIS.readObject() as T
                objIS.close()
                fileIS.close()
                return data
            }
    } catch (e: Exception) {
        toast(e)
    }
    return null
}