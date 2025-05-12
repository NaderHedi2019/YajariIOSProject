package com.app.yajari.ui.signup

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivitySignUpBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.ui.otp.OtpVerificationActivity
import com.app.yajari.ui.pages.PagesActivity
import com.app.yajari.ui.signup.viewmodel.AuthViewModel
import com.app.yajari.utils.Constant
import com.app.yajari.utils.CustomProgressDialog
import com.app.yajari.utils.Status
import com.app.yajari.utils.asString
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.fullScreenDialog
import com.app.yajari.utils.getAddressFromLatLng
import com.app.yajari.utils.getStr
import com.app.yajari.utils.ifNotNullOrElse
import com.app.yajari.utils.isNameValid
import com.app.yajari.utils.isValidEmail
import com.app.yajari.utils.isValidPasswordFormat
import com.app.yajari.utils.makeLinks
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.showToasty
import com.app.yajari.utils.start
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


class SignUpActivity : BaseActivity<ActivitySignUpBinding>(),BaseActivity.LocationListener {
    private var address = ""
    private var apiDob = ""
    private var msgExists = ""
    private var type = ""
    private var latitude = 0.00
    private var longitude = 0.00
    private var isEmailMobileExists = false
    private val authViewModel: AuthViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }




    override fun getViewBinding() = ActivitySignUpBinding.inflate(layoutInflater)

    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
        assignLocationListener(this@SignUpActivity)
    }

    override fun click() {
        binding.apply {
            mobileEDT.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    mobileCL.background = ContextCompat.getDrawable(
                        this@SignUpActivity,
                        R.drawable.shape_edt_selected
                    )
                    mobileIV.setImageResource(R.drawable.ic_mobile_selected)
                } else {
                    mobileCL.background = ContextCompat.getDrawable(
                        this@SignUpActivity,
                        R.drawable.shape_edt_unselected
                    )
                    mobileIV.setImageResource(R.drawable.ic_mobile_unselected)
                }
            }

            privacyTV.makeLinks(
                this@SignUpActivity,
                Pair(getString(R.string.terms_of_services_), View.OnClickListener {
                    start<PagesActivity>("2", Constant.KEY to Constant.TERMS)
                })
            )
            privacyTV.makeLinks(this@SignUpActivity, Pair(getString(R.string.privacy_policy_l), View.OnClickListener {
                start<PagesActivity>("2", Constant.KEY to Constant.PRIVACY)
            }))

            loginTV.setSafeOnClickListener {
                finish()
            }
            createAccountBTN.setSafeOnClickListener {
                if (isValid()) {
                    callSendOTP()
                }

            }

            currentLocationIV.setSafeOnClickListener {
                runTimePermission()
            }
            locationEDT.setSafeOnClickListener {
                startPlacesActivity()
            }
            dobEDT.setSafeOnClickListener {
                showDatePicker(dobEDT)
            }
            emailEDT.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (binding.emailEDT.asString().isValidEmail()) {
                        type = "email"
                        callCheckEMailMobile()

                    }
                }
                false
            }
            mobileEDT.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (binding.mobileEDT.asString().length >= 8) {
                        type = "mobile_number"
                        callCheckEMailMobile()

                    }
                }
                false
            }

            emailEDT.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    if (binding.emailEDT.asString().isValidEmail()) {
                        type = "email"
                        callCheckEMailMobile()
                    }
                }
            }
            mobileEDT.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    if (binding.mobileEDT.asString().length >= 8) {
                        type = "mobile_number"
                        callCheckEMailMobile()
                    }
                }
            }

        }
    }

    override fun setUpObserver() {
        authViewModel.sendOTPResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    start<OtpVerificationActivity>(
                        "2",
                        Constant.NAME to binding.nameEDT.asString(),
                        Constant.COUNTRY_CODE to binding.signupCCP.selectedCountryCodeWithPlus,
                        Constant.MOBILE to binding.mobileEDT.asString(),
                        Constant.EMAIL to binding.emailEDT.asString(),
                        Constant.DOB to apiDob,
                        Constant.ADDRESS to binding.locationEDT.asString(),
                        Constant.LAT to latitude.toString(),
                        Constant.LNG to longitude.toString(),
                        Constant.PASSWORD to binding.passwordEDT.asString()
                    )
                }

                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        response.message?.let { msg ->
                            showToasty(this,
                                msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                                "2"
                            )
                        }
                    }
                }

                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    showToasty(this, response.message.toString(), "2")
                }

                Status.NETWORK_ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        fullScreenDialog(R.layout.dialog_no_internet) {
                            val commonDialog = DialogNoInternetBinding.inflate(layoutInflater)
                            setContentView(commonDialog.root)
                            commonDialog.apply {
                                tryAgainBTN.setSafeOnClickListener {
                                    dismiss()
                                    callSendOTP()
                                }
                            }
                        }
                    }
                }
            }
        }


        authViewModel.emailMobileExistsResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {
                }

                Status.SUCCESS -> {
                    isEmailMobileExists = false
                }

                Status.ERROR -> {
                    isEmailMobileExists = true
                    runOnUiThread {
                        response.message?.let { msg ->
                            msgExists = msg
                            showToasty(
                                this, msg.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(
                                        Locale.ROOT
                                    ) else it.toString()
                                }, "2"
                            )
                        }
                    }
                }

                Status.UNAUTHORIZED -> {
                    showToasty(this, response.message.toString(), "2")
                }

                Status.NETWORK_ERROR -> {
                    runOnUiThread {
                        fullScreenDialog(R.layout.dialog_no_internet) {
                            val commonDialog = DialogNoInternetBinding.inflate(layoutInflater)
                            setContentView(commonDialog.root)
                            commonDialog.apply {
                                tryAgainBTN.setSafeOnClickListener {
                                    dismiss()
                                    callCheckEMailMobile()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onBaseBackPressed() {
        finish()
    }



    private fun startPlacesActivity() {
        Places.initialize(
            this@SignUpActivity, getString(R.string.map_key)
        )
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS
        )
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(this@SignUpActivity)
        autoCompleteResult.launch(intent)

    }


    private var autoCompleteResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.let {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    println(place.address!!.toString())
                    latitude = place.latLng!!.latitude
                    longitude = place.latLng!!.longitude
                    address = place.address!!
                    binding.locationEDT.setText(address)
                }
            }
        }

    @Suppress("DEPRECATION")
    private fun getAddressFromLocation() {
        val geocoder = Geocoder(this@SignUpActivity, Locale.getDefault())
        val addressList = geocoder.getFromLocation(latitude, longitude, 1)
        binding.locationEDT.setText(getAddressFromLatLng(addressList!!.toMutableList()))
    }


    @SuppressLint("SimpleDateFormat")
    private fun showDatePicker(dateEDT: AppCompatEditText) {
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.set(Calendar.MONTH, 0)
        c.set(Calendar.YEAR, 1000)
        val fifteenYearsAgo = Calendar.getInstance()
        fifteenYearsAgo.add(Calendar.YEAR, -15)
        val datePicker: MaterialDatePicker<Long> = MaterialDatePicker
            .Builder
            .datePicker()
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setStart(c.timeInMillis)
                    .setEnd(Calendar.getInstance().timeInMillis)
                    .setValidator(DateValidatorPointBackward.before(Calendar.getInstance().timeInMillis))
                    .build()
            )
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .setTitleText(getStr(R.string.select_dob_title))
            .build()
        datePicker.show(supportFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener {
            if (fifteenYearsAgo.timeInMillis>=it) {
                val dateFormatDisplay = SimpleDateFormat(Constant.displayFormat)
                dateFormatDisplay.timeZone = TimeZone.getTimeZone("UTC")
                val apiFormat = SimpleDateFormat(Constant.apiFormat)
                apiFormat.timeZone = TimeZone.getTimeZone("UTC")
                dateEDT.setText(dateFormatDisplay.format(it))
                apiDob = apiFormat.format(it)
            }
            else{
                showToasty(this@SignUpActivity, getString(R.string.dob_15_msg),"2")
            }
        }
    }

    private fun isValid(): Boolean {
        if (binding.nameEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_name).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            binding.nameEDT.requestFocus()
            return false
        }
        else if (!isNameValid(binding.nameEDT.asString())) {
            showToasty(
                this,
                getStr(R.string.please_enter_valid_name).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            binding.nameEDT.requestFocus()
            return false
        }
        else if (apiDob == "" || binding.dobEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_dob).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            binding.dobEDT.requestFocus()
            return false
        } else if (binding.mobileEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_mobile_number).ifNotNullOrElse(
                    { it: String -> it },
                    { "" }),
                "2"
            )
            binding.mobileEDT.requestFocus()
            return false
        } else if (latitude == 0.00 && longitude == 0.00 || binding.locationEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_location).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            binding.locationEDT.requestFocus()
            return false
        }
        if (binding.emailEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_email).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            binding.emailEDT.requestFocus()
            return false
        } else if (!binding.emailEDT.asString().isValidEmail()) {
            showToasty(
                this,
                getStr(R.string.please_enter_valid_email).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            binding.emailEDT.requestFocus()
            return false
        } else if (binding.passwordEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_password).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            binding.passwordEDT.requestFocus()
            return false
        }
        else if(!isValidPasswordFormat(binding.passwordEDT.asString()))
        {
            showToasty(this, getStr(R.string.password_invalid).ifNotNullOrElse({it }, { "" }), "2")
            binding.passwordEDT.requestFocus()
            return false
        }
        else if (binding.confirmPasswordEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_confirm_password).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            binding.confirmPasswordEDT.requestFocus()
            return false
        }
        else if(!isValidPasswordFormat(binding.confirmPasswordEDT.asString()))
        {
            showToasty(this, getStr(R.string.confirm_password_invalid).ifNotNullOrElse({it }, { "" }), "2")
            binding.confirmPasswordEDT.requestFocus()
            return false
        }
        else if (binding.confirmPasswordEDT.asString() != binding.passwordEDT.asString()) {
            showToasty(
                this,
                getStr(R.string.password_miss_match).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            binding.passwordEDT.requestFocus()
            return false
        } else if (isEmailMobileExists) {
            showToasty(
                this,
                msgExists.ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            return false
        } else if (!binding.acceptCB.isChecked) {
            showToasty(
                this,
                getString(R.string.terms_accept_msg).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            return false
        }
        return true
    }

    private fun callSendOTP() {
        authViewModel.sendOTP(mapOf(Constant.EMAIL to binding.emailEDT.asString()))
    }


    private fun callCheckEMailMobile() {
        if (type == Constant.EMAIL) {
            authViewModel.checkMobile(
                mapOf(
                    Constant.VALUE to binding.emailEDT.asString(),
                    Constant.TYPE to type,
                    Constant.CHECK_TYPE to "unique"
                )
            )

        } else {
            authViewModel.checkMobile(
                mapOf(
                    Constant.TYPE to type,
                    Constant.VALUE to binding.mobileEDT.asString(),
                    Constant.COUNTRY_CODE to binding.signupCCP.selectedCountryCodeWithPlus,
                    Constant.CHECK_TYPE to "unique"
                )
            )

        }
    }

    override fun onLocationGet(location: Location) {
        latitude=location.latitude
        longitude=location.longitude
        getAddressFromLocation()
    }


}