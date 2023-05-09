package com.cmu.project.core.dialog

import android.app.Activity
import android.app.AlertDialog
import com.cmu.project.R

class CustomDialog(private val activity: Activity) {

    private lateinit var dialog: AlertDialog

    fun startDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setView(activity.layoutInflater.inflate(R.layout.custom_dialog, null))
        builder.setCancelable(false)
        dialog = builder.create()
        dialog.show()
    }

    fun dismissDialog() {
        dialog.dismiss()
    }

}