package com.app.yajari.ui.create_announcement

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.widget.addTextChangedListener
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.data.CategoryResponse
import com.app.yajari.data.MediaModel
import com.app.yajari.data.MyAnnouncementResponse
import com.app.yajari.databinding.ActivityPublishAnnouncementBinding
import com.app.yajari.databinding.BottomDialogCategoryBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.ui.create_announcement.viewmodel.AnnouncementViewModel
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.utils.*
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class PublishAnnouncementActivity : BaseActivity<ActivityPublishAnnouncementBinding>(), CategoryAdapter.CategoryListener,
    UploadImageAdapter.ImageUploadListener, BaseActivity.LocationListener {
    private var type = ""
    private var from = ""
    private var condition = ""
    private var objectType = ""
    private var categoryId = ""
    private var categoryName = ""
    private var address = ""
    private var expDate = ""
    private var latitude = 0.00
    private var longitude = 0.00
    private var isPhotoRemove = false
    private var isSubmit = false
    private var media: File? = null
    private var fileType = ""
    private var deleteGalleryPosition = 0
    private var deleteGalleryId = ""
    private var commercial = Constant.NO
    private var imageUri: Uri? = null
    private var mediaList: MutableList<MediaModel> = mutableListOf()
    private lateinit var imageAdapter: UploadImageAdapter
    private var categoryAdapter: CategoryAdapter? = null
    private var announcementData: MyAnnouncementResponse.Data? = null
    private val announcementViewModel: AnnouncementViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }
    override fun getViewBinding() = ActivityPublishAnnouncementBinding.inflate(layoutInflater)


    override fun initObj() {
        changeStatusBarColor("#FFFFFF")

        //Title should not have only numbers.
        binding.apply {
            titleEDT.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) { }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    titleEDT.filters = arrayOf(InputFilter { source, start, end, _, _, _ ->
                        for (i in start until end) {
                            if (!Character.isDigit(source[i])) {
                                return@InputFilter null
                            }
                        }
                        return@InputFilter ""
                    })
                    if (s?.length ?: 0 > 20) {
                        titleEDT.setText(s?.subSequence(0, 20))
                        titleEDT.setSelection(20)
                    }
                }
            })
        }



        assignLocationListener(this@PublishAnnouncementActivity)
        from = intent.getStringExtra(Constant.FROM).toString()
        if (from == Constant.CREATE) {
            type = intent.getStringExtra(Constant.TYPE).toString()
            objectType = intent.getStringExtra(Constant.OBJECT_TYPE).toString()
        } else if (from == Constant.EDIT) {
            announcementData = intent.serializable(Constant.ANNOUNCEMENT_DATA)
            type = announcementData?.type.toString()
            objectType = announcementData?.objectType.toString()
            setupEditAnnouncement()
        }

    }

    private fun setupEditAnnouncement() {

        binding.apply {
            postBTN.text = getStr(R.string.update_announcement)
            toolbar.titleTV.text = getStr(R.string.edit_announcement)
            condition = announcementData?.conditionId.toString()
            categoryId = announcementData?.categoryId.toString()
            categoryName = announcementData?.category.toString()
            latitude = announcementData?.lat!!.toDouble()
            longitude = announcementData?.lng!!.toDouble()
            titleEDT.setText(announcementData?.title)
            descEDT.setText(announcementData?.description)
            locationEDT.setText(announcementData?.location)
            setGallery()
            if (type == Constant.OBJECT) {
                categoryATV.setText(announcementData?.category)
            }
            if (type == Constant.FOOD) {
                if (announcementData?.commercial == "Yes") {
                    commercialRG.check(R.id.yesRB)
                } else {
                    commercialRG.check(R.id.noRB)
                }
            }
            if (type == Constant.OBJECT && objectType != Constant.REQUEST) {
                conditionATV.setText(announcementData?.condition)
            }
            if (type == Constant.FOOD && objectType != Constant.REQUEST) {
                expDate = announcementData?.expirationDate.toString()
                if (expDate.isNotEmpty()) {
                    expEDT.setText(getFormattedDate(expDate, "yyyy-MM-dd", "dd/MM/yyyy"))
                }
            }

        }

    }

    private fun setGallery() {
        announcementData?.gallery?.map {
            mediaList.add(MediaModel(type = Constant.EDIT, imageUrl = it.file, id = it.id.toString()))
        }
    }

    override fun click() {
        binding.apply {
            toolbar.backIV.setSafeOnClickListener {
                if (isPhotoRemove) {
                    setResult(RESULT_OK)
                    finish()
                } else {
                    finish()
                }
            }
            pictureRV.apply {
                imageAdapter = UploadImageAdapter(mediaList, this@PublishAnnouncementActivity)
                adapter = imageAdapter
            }
            if (from == Constant.CREATE) {
                toolbar.titleTV.text = getStr(R.string.publish_announcement)
            }
            postBTN.setSafeOnClickListener {
                if (isValid()) {
                    callCreateEditAnnouncement()
                }
            }

            descEDT.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    aboutCL.background = ContextCompat.getDrawable(this@PublishAnnouncementActivity, R.drawable.shape_edt_selected)
                    aboutIV.setImageResource(R.drawable.ic_write_about_selected)
                } else {
                    aboutCL.background = ContextCompat.getDrawable(this@PublishAnnouncementActivity, R.drawable.shape_edt_unselected)
                    aboutIV.setImageResource(R.drawable.ic_write_about_unselected)
                }
            }
            categoryATV.setSafeOnClickListener {
                showCategoryDialog()
            }
            locationEDT.setSafeOnClickListener {
                startPlacesActivity()
            }
            expEDT.setSafeOnClickListener {
                showDatePicker(expEDT)
            }
            currentLocationIV.setSafeOnClickListener {
                runTimePermission()
            }
            commercialRG.setOnCheckedChangeListener { radioGroup, _ ->
                val radioCheckedId = radioGroup.checkedRadioButtonId
                if (radioCheckedId == R.id.yesRB) {
                    commercial = Constant.YES
                } else if (radioCheckedId == R.id.noRB) {
                    commercial = Constant.NO
                }
            }

            conditionATV.setSafeOnClickListener {
                val arrayAdapter = ArrayAdapter(
                    this@PublishAnnouncementActivity, R.layout.adapter_dropdown, R.id.optionNameTV,
                    Constant.conditionList
                )
                conditionATV.setAdapter(arrayAdapter)
                conditionATV.setOnItemClickListener { _, _, position, _ ->
                    condition = Constant.conditionList[position].uniqueId!!
                    conditionATV.setText(Constant.conditionList[position].title!!, false)

                }
                conditionATV.showDropDown()
            }
            addPicIV.setSafeOnClickListener {
                if (mediaList.size >= 3) {
                    showToasty(
                        this@PublishAnnouncementActivity,
                        getString(R.string.you_can_select_only_5_image), "2"
                    )
                } else {
                    openImagePicker()
                }
            }
            if (type == Constant.OBJECT) {
                infoHealthyIV.gone()
                beHealthyTV.gone()
                healthyDescTV.gone()
                categoryTV.visible()
                categoryATV.visible()
                if (objectType == Constant.REQUEST) {
                    conditionTV.gone()
                    conditionATV.gone()
                    uploadPhotoTV.text = getStr(R.string.upload_photo)
                } else {
                    conditionTV.visible()
                    conditionATV.visible()
                }
                commercialTV.gone()
                commercialRG.gone()
                expDateTV.gone()
                expEDT.gone()
                callGetCategory()
            } else if (type == Constant.FOOD) {
                categoryTV.gone()
                categoryATV.gone()
                conditionTV.gone()
                conditionATV.gone()
                commercialTV.visible()
                commercialRG.visible()
                if (objectType == Constant.REQUEST) {
                    expDateTV.gone()
                    expEDT.gone()
                    uploadPhotoTV.text = getStr(R.string.upload_photo)
                } else {
                    expDateTV.visible()
                    expEDT.visible()
                }
            }

        }

    }

    private fun callGetCategory() {
        announcementViewModel.getCategory()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun setUpObserver() {
        announcementViewModel.categoryResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    response.data?.data?.let { it ->
                        Constant.categoryList = it.category!!
                        Constant.conditionList = it.condition!!
                        Constant.categoryList.find { it.uniqueId == categoryId }?.isSelected == true
                    }
                }

                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        response.message?.let { msg ->
                            showToasty(this@PublishAnnouncementActivity,
                                msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, "2"
                            )
                        }
                    }
                }

                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    announcementViewModel.logoutUser()
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
                                    callGetCategory()
                                }
                            }
                        }
                    }
                }
            }
        }
        announcementViewModel.deletePhotoResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    mediaList.removeAt(deleteGalleryPosition)
                    imageAdapter.notifyDataSetChanged()
                    isPhotoRemove = true
                }

                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        response.message?.let { msg ->
                            showToasty(this@PublishAnnouncementActivity,
                                msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, "2"
                            )
                        }
                    }
                }

                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    announcementViewModel.logoutUser()
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
                                    callRemoveGallery()
                                }
                            }
                        }
                    }
                }
            }
        }
        announcementViewModel.createAnnouncementResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    start<AnnouncementSubmissionActivity>("0")
                }

                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        response.message?.let { msg ->
                            showToasty(this@PublishAnnouncementActivity,
                                msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, "2"
                            )
                        }
                    }
                }

                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    announcementViewModel.logoutUser()
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
                                    callCreateEditAnnouncement()
                                }
                            }
                        }
                    }
                }
            }
        }
        announcementViewModel.editAnnouncementResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    setResult(RESULT_OK)
                    finish()
                }

                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    runOnUiThread {
                        response.message?.let { msg ->
                            showToasty(this@PublishAnnouncementActivity,
                                msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, "2"
                            )
                        }
                    }
                }

                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismissProgressDialog()
                    announcementViewModel.logoutUser()
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
                                    callCreateEditAnnouncement()
                                }
                            }
                        }
                    }
                }
            }
        }
        announcementViewModel.logout.observe(this) {
            if (it) {
                start<LoginActivity>("1")
            }
        }
    }

    override fun onBaseBackPressed() {
        if (isPhotoRemove) {
            setResult(RESULT_OK)
            finish()
        } else {
            finish()
        }
    }


    private fun showCategoryDialog() {
        val mBottomSheetDialog = BottomSheetDialog(this@PublishAnnouncementActivity, R.style.AppBottomSheetDialogTheme)
        val mBottomCategoryBinding = BottomDialogCategoryBinding.inflate(layoutInflater)
        mBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        mBottomSheetDialog.setCancelable(false)
        mBottomSheetDialog.setContentView(mBottomCategoryBinding.root)
        for (i in Constant.categoryList.indices) {
            if (categoryId == Constant.categoryList[i].uniqueId) {
                Constant.categoryList[i].isSelected = true
            }
        }
        mBottomCategoryBinding.apply {
            commonListRV.apply {
                categoryAdapter = CategoryAdapter(this@PublishAnnouncementActivity, Constant.categoryList, this@PublishAnnouncementActivity,true)
                adapter = categoryAdapter
            }
            closeIV.setSafeOnClickListener {
                if (!isSubmit && !categoryAdapter?.categoryList!!.any { it.isSelected }) {
                    categoryId = ""
                    categoryName = ""
                    binding.categoryATV.setText("")
                    categoryAdapter?.categoryList!!.map { it.isSelected = false }
                }
                mBottomSheetDialog.dismiss()
            }
            submitBTN.setSafeOnClickListener {
                if (categoryAdapter?.categoryList!!.any { it.isSelected }) {
                    binding.categoryATV.setText(categoryName)
                    isSubmit = true
                    mBottomSheetDialog.dismiss()

                } else {
                    showToasty(
                        this@PublishAnnouncementActivity,
                        getString(R.string.please_select_category), "2"
                    )
                }
            }
            searchEDT.addTextChangedListener {
                if (it.toString().isNotEmpty()) {
                    clearIV.visible()
                } else {
                    clearIV.gone()
                }
                categoryAdapter?.filter?.filter(it.toString())
            }
            clearIV.setSafeOnClickListener {
                searchEDT.setText("")
            }
        }
        mBottomSheetDialog.show()
    }

    override fun onItemClickCategory(categoryData: CategoryResponse.Category) {
        categoryId = categoryData.uniqueId!!
        categoryName = categoryData.title.toString()
        isSubmit = false
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addMediaToModel() {
        mediaList.add(MediaModel(media = media, imageUri = imageUri, type = "add"))
        imageAdapter.notifyDataSetChanged()
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
                    imageUri = data?.data
                    fileType = "Image"
                    media = imageUri?.toFile()
                    addMediaToModel()
                }

                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
            }
        }

    @SuppressLint("NotifyDataSetChanged")
    override fun onMediaRemoveClick(data: MediaModel, position: Int) {
        deleteGalleryPosition = position
        deleteGalleryId = data.id.toString()
        if (data.type == "edit") {
            callRemoveGallery()
        } else {
            mediaList.removeAt(deleteGalleryPosition)
            imageAdapter.notifyDataSetChanged()
        }
    }

    private fun callRemoveGallery() {
        announcementViewModel.deletePhoto(mapOf(Constant.GALLERY_ID to deleteGalleryId))
    }

    private fun callCreateEditAnnouncement() {
        val filePart: MutableList<MultipartBody.Part> = mutableListOf()
        val map = HashMap<String, RequestBody>()
        if (type == Constant.OBJECT) {
            map["condition"] = createPartFromString(condition)
            map["category_id"] = createPartFromString(categoryId)
        } else if (type == Constant.FOOD) {
            map["expiration_date"] = createPartFromString(expDate)
            map["commercial"] = createPartFromString(commercial)
        }
        if (from != Constant.EDIT) {
            map["object_type"] = createPartFromString(objectType)
            map["type"] = createPartFromString(type)
        }
        map["title"] = createPartFromString(binding.titleEDT.asString())
        map["description"] = createPartFromString(binding.descEDT.asString())
        map["location"] = createPartFromString(binding.locationEDT.asString())
        map["lat"] = createPartFromString(latitude.toString())
        map["lng"] = createPartFromString(longitude.toString())
        if (mediaList.isNotEmpty()) {
            for (i in 0 until mediaList.size) {
                if (mediaList[i].type == "add") {
                    val file = prepareFilePart(mediaList[i].media!!, mediaList[i].imageUri!!, i, this@PublishAnnouncementActivity)
                    filePart.add(file)
                }
            }
        }
        if (from == Constant.EDIT) {
            map["announcement_id"] = createPartFromString(announcementData?.id.toString())
            announcementViewModel.editAnnouncement(map, filePart)
        } else {
            announcementViewModel.createAnnouncement(map, filePart)
        }
    }

    private fun isValid(): Boolean {
        if (type == Constant.OBJECT && binding.categoryATV.asString() == "" && categoryId == "") {
            showToasty(
                this,
                getStr(R.string.please_select_category).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            binding.categoryATV.requestFocus()
            return false
        } else if (binding.titleEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_title).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            binding.titleEDT.requestFocus()
            return false
        } else if (!isNameValid(binding.titleEDT.asString())) {
            showToasty(
                this,
                getStr(R.string.please_enter_valid_title).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            binding.titleEDT.requestFocus()
            return false
        } else if (binding.descEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_desc).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            binding.descEDT.requestFocus()
            return false
        } else if (objectType != Constant.REQUEST && type == Constant.OBJECT && binding.conditionATV.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_select_condition).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            binding.conditionATV.requestFocus()
            return false
        } else if (latitude == 0.00 && longitude == 0.00 || binding.locationEDT.asString() == "") {
            showToasty(
                this,
                getStr(R.string.please_enter_location).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            binding.locationEDT.requestFocus()
            return false
        } else if (objectType != Constant.REQUEST && type == Constant.FOOD && binding.expEDT.asString() == "" && expDate.isEmpty()) {
            showToasty(
                this,
                getStr(R.string.please_select_exp_date).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            binding.expEDT.requestFocus()
            return false
        } else if (objectType == Constant.DONATION && mediaList.size == 0) {
            showToasty(
                this,
                getStr(R.string.please_select_pic).ifNotNullOrElse({ it }, { "" }),
                "2"
            )
            return false
        }
        return true

    }


    private fun startPlacesActivity() {
        Places.initialize(
            this@PublishAnnouncementActivity, getString(R.string.map_key)
        )
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS
        )
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(this@PublishAnnouncementActivity)
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
        val geocoder = Geocoder(this@PublishAnnouncementActivity, Locale.getDefault())
        val addressList = geocoder.getFromLocation(latitude, longitude, 1)
        binding.locationEDT.setText(getAddressFromLatLng(addressList!!.toMutableList()))
    }


    @SuppressLint("SimpleDateFormat")
    private fun showDatePicker(dateEDT: AppCompatEditText) {
        val c = Calendar.getInstance()
        val datePicker: MaterialDatePicker<Long> = MaterialDatePicker
            .Builder
            .datePicker()
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setStart(c.timeInMillis)
                    .setValidator(DateValidatorPointForward.now())
                    .build()
            )
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .setTitleText(getStr(R.string.select_dob_title))
            .build()
        datePicker.show(supportFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener {
            val dateFormatDisplay = SimpleDateFormat(Constant.displayFormat)
            dateFormatDisplay.timeZone = TimeZone.getTimeZone("UTC")
            val apiFormat = SimpleDateFormat(Constant.apiFormat)
            apiFormat.timeZone = TimeZone.getTimeZone("UTC")
            dateEDT.setText(dateFormatDisplay.format(it))
            expDate = apiFormat.format(it)
        }
    }

    override fun onLocationGet(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
        getAddressFromLocation()
    }

}