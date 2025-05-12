package com.app.yajari.utils

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.app.yajari.R

class CustomProgressDialog(context: Activity) : Dialog(context) {
    @Deprecated("Deprecated in Java")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_progress)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    init {
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.6f)
        }
    }

    fun showProgressDialog() {
        if (!this.isShowing) this.show()
    }

    fun dismissProgressDialog() {
        if (this.isShowing) this.dismiss()
    }
}
