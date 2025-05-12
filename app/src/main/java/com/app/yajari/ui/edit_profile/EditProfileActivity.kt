package com.app.yajari.ui.edit_profile
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toFile
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivityEditProfileBinding
import com.app.yajari.databinding.DialogCommonMsgBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.ui.profile.viewmodel.ProfileViewModel
import com.app.yajari.utils.*
import com.app.yajari.utils.commonDialog
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class EditProfileActivity : BaseActivity<ActivityEditProfileBinding>(),BaseActivity.LocationListener {
    private var userPicUri: Uri? = null
    private var userPicFile: File? = null
    private var address = ""
    private var apiDob = ""
    private var latitude = 0.00
    private var longitude = 0.00
    private val profileViewModel: ProfileViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }

    override fun getViewBinding()= ActivityEditProfileBinding.inflate(layoutInflater)

    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
        assignLocationListener(this@EditProfileActivity)
    }

    override fun click() {
        binding.apply {
            toolbar.titleTV.text=getStr(R.string.edit_profile)
            toolbar.backIV.setSafeOnClickListener {
                finish()
            }
            mobileEDT.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if(hasFocus)
                {
                    mobileCL.background= ContextCompat.getDrawable(this@EditProfileActivity, R.drawable.shape_edt_selected)
                    mobileIV.setImageResource(R.drawable.ic_mobile_selected)
                }
                else{
                    mobileCL.background= ContextCompat.getDrawable(this@EditProfileActivity, R.drawable.shape_edt_unselected)
                    mobileIV.setImageResource(R.drawable.ic_mobile_unselected)
                }
            }
            currentLocationIV.setSafeOnClickListener {
                runTimePermission()
            }
            profileIV.setSafeOnClickListener {
                openImagePicker()
            }
            locationEDT.setSafeOnClickListener {
                startPlacesActivity()
            }
            dobEDT.setSafeOnClickListener {
                showDatePicker(dobEDT)
            }
            saveBTN.setSafeOnClickListener {
                if (isValid())
                {
                    callEditProfile()
                }
            }
        }
    }

    override fun setUpObserver() {
        profileViewModel.getUserFromPref()
        profileViewModel.userDataResponse.observe(this)
        {
            binding.apply {
                nameEDT.setText(it.name)
                if (!it.bod.isNullOrEmpty())
                {
                    dobEDT.setText(getFormattedDate(it.bod, "yyyy-MM-dd", "dd/MM/yyyy"))
                }
                latitude=it.lat!!.toDouble()
                longitude=it.lng!!.toDouble()
                locationEDT.setText(it.address)
                mobileEDT.setText(it.mobile)
                signupCCP.setCountryForPhoneCode(it.countryCode?.replace("+","")!!.toInt())
                emailEDT.setText(it.email)
                profileIV.loadImage(it.profileImage,R.drawable.img_user)
                apiDob=it.bod.toString()
                address=it.address.toString()
            }
        }
        profileViewModel.editProfileResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    showMessageDialog()
                }
                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread { response.message?.let { msg -> showToasty(this@EditProfileActivity,
                        msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, "2") } }
                }
                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    profileViewModel.logoutUser()
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
                                    callEditProfile()
                                }
                            }
                        }
                    }
                }
            }
        }
        profileViewModel.logout.observe(this){
            if(it)
            {
                start<LoginActivity>("1")
            }
        }
    }

    override fun onBaseBackPressed() {
        finish()
    }

    private fun showMessageDialog()
    {
        commonDialog(R.layout.dialog_common_msg) {
            run {
                val msgDialog = DialogCommonMsgBinding.inflate(layoutInflater)
                setContentView(msgDialog.root)
                msgDialog.apply {
                    msgTV.typeface=ResourcesCompat.getFont(this@EditProfileActivity,R.font.rale_way_semi_bold)
                    msgTV.text=getStr(R.string.profile_update_msg)
                    descTV.gone()
                    continueBTN.setSafeOnClickListener {
                        dismiss()
                        finish()
                    }
                }
            }
        }
    }

    private fun callEditProfile() {
        try {
            val map = mapOf(
                Constant.NAME to createPartFromString(binding.nameEDT.asString()),
                Constant.BOD to createPartFromString(apiDob),
               Constant.MOBILE to createPartFromString(binding.mobileEDT.asString()),
               Constant.COUNTRY_CODE to createPartFromString(binding.signupCCP.selectedCountryCodeWithPlus),
               Constant.ADDRESS to createPartFromString( binding.locationEDT.asString()),
               Constant.LAT to createPartFromString(latitude.toString()),
               Constant.LNG to createPartFromString(longitude.toString())
            )
            val filePart = userPicFile?.let { createFilePart("profile_image", it, userPicUri!!,this@EditProfileActivity) }
            if (filePart != null) {
                profileViewModel.editProfile(map, filePart)
            } else {
                profileViewModel.editProfile(map, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun openImagePicker() {
        ImagePicker.with(this)
            .compress(1024)
            .crop()
            .maxResultSize(1080, 1080).createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }

    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data!!
                    userPicUri = fileUri
                    userPicFile = fileUri.toFile()
                    Glide.with(this).load(userPicUri).placeholder(R.drawable.img_user)
                        .into(binding.profileIV)
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
            }
        }



    private fun startPlacesActivity() {
        Places.initialize(
            this@EditProfileActivity, getString(R.string.map_key)
        )
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS
        )
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(this@EditProfileActivity)
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
        val geocoder = Geocoder(this@EditProfileActivity, Locale.getDefault())
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
                  //  .setOpenAt(fifteenYearsAgo.timeInMillis)
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
                showToasty(this@EditProfileActivity, getString(R.string.dob_15_msg),"2")
            }
        }
    }
    private fun isValid(): Boolean {
        if (binding.nameEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_name).ifNotNullOrElse({  it }, { "" }),
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
        else if (apiDob == "" || binding.dobEDT.asString()=="") {
            showToasty(
                this,
                getStr(R.string.please_enter_dob).ifNotNullOrElse({  it }, { "" }),
                "2"
            )
            binding.dobEDT.requestFocus()
            return false
        }
        else if (binding.mobileEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_mobile_number).ifNotNullOrElse({ it: String -> it }, { "" }),
                "2"
            )
            binding.mobileEDT.requestFocus()
            return false
        }
        else if (latitude == 0.00 && longitude==0.00 || binding.locationEDT.asString()=="") {
            showToasty(
                this,
                getStr(R.string.please_enter_location).ifNotNullOrElse({  it }, { "" }),
                "2"
            )
            binding.mobileEDT.requestFocus()
            return false
        }
        return true
    }

    override fun onLocationGet(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
        getAddressFromLocation()
    }

}