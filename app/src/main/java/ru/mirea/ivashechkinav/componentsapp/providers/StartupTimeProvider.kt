package ru.mirea.ivashechkinav.componentsapp.providers

import android.content.ContentProvider
import android.content.ContentValues
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log

class StartupTimeProvider : ContentProvider() {

    companion object {
        private val TAG = StartupTimeProvider::class.simpleName
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(): Boolean {
        try {
            StartupTrace.onColdStartInitiated(context!!)
            mainHandler.post(StartupTrace.StartFromBackgroundRunnable)
            Log.i(TAG, "StartupTrace initialization successful");
        } catch (e: Exception) {
            Log.e(TAG,"Failed to initialize StartupTimeProvider", e)
        }
        return true
    }

    override fun query(
        p0: Uri,
        p1: Array<out String>?,
        p2: String?,
        p3: Array<out String>?,
        p4: String?
    ) = null

    override fun getType(p0: Uri) = null
    override fun insert(p0: Uri, p1: ContentValues?) = null
    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?) = 0
    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?) = 0
}