@file:Suppress("DEPRECATION")

package com.app.yajari.utils
import android.animation.AnimatorInflater
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.ContentFrameLayout
import androidx.core.content.ContextCompat
import com.app.yajari.R
import com.bumptech.glide.Glide
import dev.b3nedikt.app_locale.AppLocale
import dev.b3nedikt.reword.Reword
import es.dmoral.toasty.Toasty
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * To get desired value for given VALUE whether it is null or not
 * Usage :
 *      any_kind_of_value.ifNotNullOrElse({ your_return_value_if_given_value_is_not_null }, { your_return_value_if_given_value_is_null })
 * */

inline fun <T : Any, R> T?.ifNotNullOrElse(ifNotNullPath: (T) -> R, elsePath: () -> R) =
    let { if (it == null) elsePath() else ifNotNullPath(it) }


/*------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------------*/
fun CharSequence.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

//fun isNameValid(name: String): Boolean {
//    // Regular expression for a valid name
//    val regex = "^[a-zA-Z'\\- ]+\$"
//
//    val pattern = Pattern.compile(regex)
//    val matcher = pattern.matcher(name)
//
//    // Check if the name matches the regular expression
//    return matcher.matches()
//}

fun isNameValid(name: String): Boolean {
    val regex = Regex("^[\\p{L} '-]+$") // Allows letters, spaces, apostrophes, and hyphens
    return regex.matches(name)
}


/**
 * To visible and gone view
 * Usage :
 *      to Visible view : yourView.visible()
 *      to Gone view : yourView.gone()
 *      to Invisible view : yourView.invisible()
 * */
fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}


/*------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------------*/



/**
 * To get string from Edit text or Text View directly
 * Usage :
 *      yourView.asString()
 * */
fun View.asString(): String {
    return when (this) {
        is EditText -> text.toString().trim().ifNotNullOrElse({ it }, { "" })
        is TextView -> text.toString().trim().ifNotNullOrElse({ it }, { "" })
        is AutoCompleteTextView -> text.toString().trim().ifNotNullOrElse({ it }, { "" })
        else -> ""
    }
}


/*------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------------*/



internal fun Dialog.setWidth(width: Float = 0.8f) {
    this.window?.setLayout(
        (Resources.getSystem().displayMetrics.widthPixels * width).toInt(),
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}




/**
 * Inflate view in adapter
 *
 * Usage :
 *      1.) parent.inflate(R.layout.my_layout) -> default root is false
 *      2.) parent.inflate(R.layout.my_layout, true)
 * */
fun ViewGroup.inflate(@LayoutRes layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}


/*------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------------*/

/**
 * Click listener to stop multi click on view
 * */
fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

/*------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------------*/

/**
 * To get Color, Resource, String from xml or res folder
 * Usage :
 *
 * getStr :
 *      getStr(R.string.your_string)
 *
 * getRes :
 *      getRes(R.drawable.your_drawable)
 *
 * getClr :
 *      getClr(R.color.your_color)
 *
 * getDimen :
 *      getDimen(R.dimen.your_dimension)
 *
 * */

fun Context.getStr(id: Int) = resources.getString(id)


fun Context.getClr(id: Int) = ContextCompat.getColor(this, id)

/**
 * To hide keyboard
 * Usage :
 *      hideKeyboard()
 *
 * */






@JvmName("updateLayoutParamsTyped")
private inline fun <reified T : ViewGroup.LayoutParams> View.updateLayoutParams1(block: T.() -> Unit) {
    val params = layoutParams as T
    block(params)
    layoutParams = params
}



fun View.setMarginBottom(value: Int) = updateLayoutParams1<ViewGroup.MarginLayoutParams> {
    bottomMargin = value
}

fun View.setMarginTop(value: Int) = updateLayoutParams1<ViewGroup.MarginLayoutParams> {
    topMargin = value
}





@SuppressLint("HardwareIds")
fun getDeviceID(context: Activity): String {
    return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
}


inline fun <reified T : Activity> Context.start(
    isFinish: String = "2",
    vararg params: Pair<String, Any?>,
    requestCode: Int? = null
) {
    /* isFinish=="0"  : only this Activity finish  || isFinish=="1"  : All Back Activity finish ||  isFinish=="2"  : Not finish    */
    this as Activity
    val intent = Intent(this, T::class.java).apply {
        params.forEach {
            when (val value = it.second) {
                is Int -> putExtra(it.first, value)
                is String -> putExtra(it.first, value)
                is Double -> putExtra(it.first, value)
                is Float -> putExtra(it.first, value)
                is Boolean -> putExtra(it.first, value)
                is Serializable -> putExtra(it.first, value)
                is Parcelable -> putExtra(it.first, value)
                else -> throw IllegalArgumentException("Wrong param type!")
            }
            return@forEach
        }
    }


    // overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    if (isFinish == "1") {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    if (requestCode != null) startActivityForResult(intent, requestCode) else startActivity(intent)
    if (isFinish == "0" || isFinish == "1") {
        finish()
    }
}



internal fun Context.commonDialog(layoutResourceId: Int, dialogBuilder: Dialog.() -> Unit) {
    Dialog(this).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setContentView(layoutResourceId)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setWidth(0.90f)
        dialogBuilder()
        dismiss()
        if (!isShowing) {
            show()
        }
    }
}

internal fun Context.fullScreenDialog(layoutResourceId: Int, dialogBuilder: Dialog.() -> Unit) {
    Dialog(this).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setContentView(layoutResourceId)
        window?.statusBarColor = Color.WHITE
        window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialogBuilder()
        dismiss()
        if (!isShowing) {
            show()
        }
    }
}




fun showToasty(context: Context, msg: String, type: String) {
    when (type) {
        "1" -> {
            Toasty.success(context, msg, Toast.LENGTH_SHORT, true).show()
        }
        "2" -> {
            Toasty.error(context, msg, Toast.LENGTH_SHORT, true).show()
        }
        "3" -> {
            Toasty.info(context, msg, Toast.LENGTH_SHORT, true).show()
        }
        else -> {
            Toasty.normal(context, msg).show()
        }
    }
}




@SuppressLint("ObsoleteSdkInt")
fun Context.changeStatusBarColor(color: String) {
    this as Activity
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = Color.parseColor(color)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else
            window.statusBarColor = Color.parseColor(color)
    }
}

@SuppressLint("ObsoleteSdkInt")
fun Context.changeStatusBarBlackColor(color: String) {
    this as Activity
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = Color.parseColor(color)
            window.decorView.systemUiVisibility = View.STATUS_BAR_VISIBLE
        } else
            window.statusBarColor = Color.parseColor(color)
    }
}



fun <T> ImageView.loadImage(
    path: T,
    placeHolder: Int
) {

    Glide.with(context)
        .load(path)
        .placeholder(placeHolder)
        .error(placeHolder)
        .into(this)
}

fun getFormattedDate(
    dateStr: String?,
    strReadFormat: String?,
    strWriteFormat: String?
): String? {
    var formattedDate = dateStr
    val readFormat: DateFormat = SimpleDateFormat(strReadFormat, Locale.getDefault())
    val writeFormat: DateFormat = SimpleDateFormat(strWriteFormat, Locale.getDefault())
    var date: Date? = null
    try {
        date = readFormat.parse(dateStr!!)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    if (date != null) {
        formattedDate = writeFormat.format(date)
    }
    return formattedDate
}



fun CustomProgressDialog.showProgressDialog() {
    if (!this.isShowing) this.show()
}

fun CustomProgressDialog.dismissProgressDialog() {
    if (this.isShowing) this.dismiss()
}


inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializableExtra( key ) as? T
}




fun TextView.underline() {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

fun AppCompatTextView.makeLinks(
    color: Int,
    font: Typeface?,
    isClick: Boolean = false,
    vararg links: Pair<String, View.OnClickListener>
) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.color = color
                textPaint.isUnderlineText = false
                textPaint.typeface = font
            }

            override fun onClick(view: View) {
                if (isClick) {
                    Selection.setSelection((view as AppCompatTextView).text as Spannable, 0)
                    view.invalidate()
                    link.second.onClick(view)
                }
            }
        }
        startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
        if (startIndexOfLink == -1) continue
        spannableString.setSpan(
            clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod = LinkMovementMethod.getInstance()
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}
fun AppCompatTextView.makeLinks(
    context: Activity,
    vararg links: Pair<String, View.OnClickListener>,
) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
/*textPaint.color = textPaint.linkColor*/
                textPaint.color = context.getClr(R.color.colorPrimary)
                textPaint.isUnderlineText = false
            }

            override fun onClick(view: View) {
                context.runOnUiThread {
                    Selection.setSelection((view as AppCompatTextView).text as Spannable, 0)
                    view.invalidate()
                    link.second.onClick(view)
                }
            }
        }
        startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
        if (startIndexOfLink == -1) continue
        spannableString.setSpan(
            clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD), startIndexOfLink, startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod = LinkMovementMethod.getInstance()
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}



fun Activity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun animateLikeImage(mContext: Context, imageView: ImageView, isLiked: Boolean) {
    val animator = AnimatorInflater.loadAnimator(
        mContext,
        if (isLiked) R.animator.like_scale else R.animator.unlike_scale
    )
    animator.setTarget(imageView)
    animator.start()
}

//Language Extension
fun Activity.changeLanguage(locale: Locale) {
    AppLocale.desiredLocale = locale

    val rootView =
        this.window?.decorView?.findViewById<ContentFrameLayout>(android.R.id.content)
    Reword.reword(rootView!!)
    window.decorView.layoutDirection =
        if (AppLocale.currentLocale.language == "ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
    rootView.invalidate()
    rootView.requestLayout()

}

fun getAddressFromLatLng(addressList: MutableList<Address>):String
{
    var addressString = ""
    try {
        if (addressList.isNotEmpty()) {
            val address = addressList[0]
            val sb = StringBuilder()
            for (i in 0 until address.maxAddressLineIndex) {
                sb.append(address.getAddressLine(i)).append("\n")
            }

            //sb.append(address.subAdminArea).append("\n")
            sb.append(address.locality).append(", ")
            sb.append(address.adminArea).append(", ")
            sb.append(address.countryName).append(", ")
            sb.append(address.postalCode)
            addressString = sb.toString()
        }
    } catch (e: IOException) {
       e.printStackTrace()
    }
    return addressString
}

 fun createPartFromString(string: String): RequestBody {
    return string.toRequestBody(MultipartBody.FORM)
}


 fun createFilePart(partName: String, file: File, uri: Uri,activity: Activity): MultipartBody.Part {
    return MultipartBody.Part.createFormData(
        partName,
        file.name,
        file.asRequestBody(uri.let { activity.contentResolver.getType(it)?.toMediaTypeOrNull() })
    )
}
fun prepareFilePart(file: File, uri: Uri,count: Int,activity: Activity): MultipartBody.Part {
    return MultipartBody.Part.createFormData(
        "files[$count]",
        file.name,
        file.asRequestBody(uri.let { activity.contentResolver.getType(it)?.toMediaTypeOrNull() })
    )
}
fun getMilliSeconds(originalString: String?, format: String, convertToUTC: Boolean = true): Long {
    SimpleDateFormat(format, Locale.getDefault()).apply {
        if (convertToUTC) timeZone = TimeZone.getTimeZone("UTC")
        return parse(originalString ?: "")?.time!!
    }
}
fun getUTCDate(originalString: String?, format: String, convertToUTC: Boolean = true): Date {
    SimpleDateFormat(format, Locale.getDefault()).apply {
        if (convertToUTC) timeZone = TimeZone.getTimeZone("UTC")
        return parse(originalString ?: "")!!
    }
}

@SuppressLint("SimpleDateFormat")
fun getDateFromMillis(millis:Long):String{
    val dateFormat=SimpleDateFormat(Constant.displayFormat)
    val date=Date(millis)
    return dateFormat.format(date)
}

/**
 * Select Time common method
 * */

fun getUtc2LocalDateTime(
    originalString: String?,
    givenFormat: String = "yyyy-MM-dd HH:mm:ss",
    format: String
): String {
    var newstr = ""
    try {
        val sDF = SimpleDateFormat(givenFormat, Locale.getDefault())
        sDF.timeZone = TimeZone.getTimeZone("UTC")
        val date: Date = sDF.parse(originalString)!!
        newstr = SimpleDateFormat(format, Locale.getDefault()).format(date)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return newstr
}


fun isValidPasswordFormat(password: String): Boolean {
    val passwordREGEX = Pattern.compile("^" +
            "(?=.*[0-9])" +         //at least 1 digit
            "(?=.*[a-z])" +         //at least 1 lower case letter
            "(?=.*[A-Z])" +         //at least 1 upper case letter
            "(?=.*[a-zA-Z])" +      //any letter
            "(?=.*[@#$%^&+=!,.])" +    //at least 1 special character
            "(?=\\S+$)" +           //no white spaces
            ".{4,}" +               //at least 4 characters
            "$")
    return passwordREGEX.matcher(password).matches()
}



