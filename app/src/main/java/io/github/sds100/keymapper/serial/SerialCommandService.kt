package io.github.sds100.keymapper.serial

import android.app.IntentService
import android.content.Intent
import android.util.Log

class SerialCommandService : IntentService("SerialCommandService") {

    override fun onHandleIntent(arg0: Intent?) {
        Log.i("Service", "Intent Service started")
    }
}