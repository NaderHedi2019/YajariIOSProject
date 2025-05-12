package com.aabhasjindal.otptextview

import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class OTPChildEditText : AppCompatEditText {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        setTextColor(Color.TRANSPARENT)
        setCursorColor(Color.TRANSPARENT)
        setBackgroundColor(Color.TRANSPARENT)
        inputType = InputType.TYPE_CLASS_NUMBER
    }

    private fun setCursorColor(color: Int) {
        try {
            val field = javaClass.superclass?.getDeclaredField("mCursorDrawableRes")
            field?.isAccessible = true
            field?.set(this, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
} 