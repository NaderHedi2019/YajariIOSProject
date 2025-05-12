package com.aabhasjindal.otptextview

import android.content.Context
import android.util.TypedValue

object Utils {
    fun getPixels(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }
} 