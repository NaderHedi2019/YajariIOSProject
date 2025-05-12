package com.app.yajari.ui.alert_settings

import android.annotation.SuppressLint
import android.util.Log
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.data.CategoryResponse
import com.app.yajari.data.LoginResponse
import com.app.yajari.databinding.ActivityAlertSettingsBinding
import com.app.yajari.databinding.BottomDialogAlertCategoryBinding
import com.app.yajari.databinding.DialogNoInternetBinding
import com.app.yajari.ui.profile.viewmodel.ProfileViewModel
import com.app.yajari.utils.Constant
import com.app.yajari.utils.Constant.Companion.categoryList
import com.app.yajari.utils.CustomProgressDialog
import com.app.yajari.utils.Status
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.fullScreenDialog
import com.app.yajari.utils.gone
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.showToasty
import com.app.yajari.utils.visible
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.slider.Slider
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale


class AlertSettingsActivity : BaseActivity<ActivityAlertSettingsBinding>(),
    AlertCategoryAdapter.CategoryListener,AlertSelectedCategory.OnItemCategoryListener {
    override fun getViewBinding() = ActivityAlertSettingsBinding.inflate(layoutInflater)
    private val profileViewModel: ProfileViewModel by viewModel()
    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }
    private var isSubmit = false
    private var categoryId = ""
    private var distance = Constant.objectRequestDistance
    private var categoryAdapter: AlertCategoryAdapter? = null
    private var categoryName = ""
    private var categorySelectedIds= listOf<String>()
    private val selectedCategories = mutableListOf<CategoryResponse.Category>()
    private lateinit var selectedCategoryAdapter: AlertSelectedCategory

    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
        bindToolbar()
        setupAlertSetting()
        selectedCategories.clear()
        selectedCategories.addAll(categoryList)
        getProfile()
    }

    private fun getProfile() {
        profileViewModel.getProfile()
    }

    private fun alertSelectedCategory() {
        selectedCategoryAdapter = AlertSelectedCategory(this@AlertSettingsActivity)
        selectedCategoryAdapter.selectedCategoryList= selectedCategories.filter { it.isSelected } as ArrayList<CategoryResponse.Category>
        binding.selectedCategoryRV.apply {
            val lManager = FlexboxLayoutManager(this@AlertSettingsActivity)
            lManager.flexDirection = FlexDirection.ROW
            lManager.justifyContent = JustifyContent.FLEX_START
            layoutManager = lManager
            adapter = selectedCategoryAdapter
        }

    }

    private fun setupAlertSetting() {
        binding.apply {

            objectSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    categoryTV.visible()
                    categoryATV.visible()
                    line1.visible()
                    selectedCategoryRV.visible()
                    distanceTV.visible()
                    kmRS.visible()
                    endKmTV.visible()
                } else {
                    categoryTV.gone()
                    line1.gone()
                    categoryATV.gone()
                    selectedCategoryRV.gone()
                    distanceTV.gone()
                    kmRS.gone()
                    endKmTV.gone()
                }
            }

            foodSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    foodDistanceTV.visible()
                    line2.visible()
                    foodKmRS.visible()
                    foodEndKmTV.visible()
                } else {
                    foodDistanceTV.gone()
                    line2.gone()
                    foodKmRS.gone()
                    foodEndKmTV.gone()
                }
            }

            if (distance.isNotEmpty()) {
              //  kmRS.value = distance.toFloat()
                endKmTV.text = getString(R.string.km, distance)
            }
        }
    }

    private fun bindToolbar() {
        binding.toolbar.apply {
            backIV.setSafeOnClickListener { finish() }
            titleTV.text = getString(R.string.alert_setting)
        }
    }


    override fun click() {
        binding.apply {
            categoryATV.setSafeOnClickListener {
                showCategoryDialog()
            }
            sendBTN.setSafeOnClickListener {
                var objectCategory = ""
                var objectDuration = ""
                var foodDuration = ""
                if (objectSwitch.isChecked) {
                    val listCatOfID = selectedCategories.filter { it.isSelected }.map { it.uniqueId }
                    objectCategory = listCatOfID.joinToString(",")
                    objectDuration = (kmRS.value.toInt()).toString()
                } else {
                    objectCategory = ""
                    objectDuration = ""
                }

                foodDuration = if (foodSwitch.isChecked) {
                    (foodKmRS.value.toInt()).toString()
                } else {
                    ""
                }

                Log.i("TAG==>", "click: $objectCategory==$objectDuration==$foodDuration")
                profileViewModel.alertSetting(
                    mapOf(
                        "object_category" to objectCategory,
                        "object_duration" to objectDuration,
                        "food_duration" to foodDuration,
                    )
                )
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showCategoryDialog() {
        val mBottomSheetDialog = BottomSheetDialog(this@AlertSettingsActivity, R.style.AppBottomSheetDialogTheme)
        val mBottomCategoryBinding = BottomDialogAlertCategoryBinding.inflate(layoutInflater)
        mBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        mBottomSheetDialog.setCancelable(false)
        mBottomSheetDialog.setContentView(mBottomCategoryBinding.root)

        mBottomCategoryBinding.apply {
            commonListRV.apply {

                categoryAdapter = AlertCategoryAdapter(
                    this@AlertSettingsActivity,
                    categoryList,
                    this@AlertSettingsActivity
                ) { selectedItems ->
                    selectedCategories.clear()
                    selectedCategories.addAll(selectedItems)
                    selectedCategoryAdapter.notifyDataSetChanged()
                }
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
                    isSubmit = true
                    mBottomSheetDialog.dismiss()
                    alertSelectedCategory()
                } else {
                    showToasty(
                        this@AlertSettingsActivity,
                        getString(R.string.please_select_category), "2"
                    )
                }
            }
        }
        mBottomSheetDialog.show()
    }

    override fun setUpObserver() {
        profileViewModel.profileResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.show()
                Status.SUCCESS -> {
                    mProgressDialog.dismiss()
                    binding.foodCL.visible()
                    binding.objectCL.visible()
                    binding.sendBTN.visible()
                    response.data!!.data?.let {
                        setUserData(it)
                    }
                }

                Status.ERROR -> {
                    mProgressDialog.dismiss()
                    runOnUiThread {
                        response.message?.let { msg ->
                            showToasty(
                                this,
                                msg.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                                "2"
                            )
                        }
                    }
                }

                Status.UNAUTHORIZED -> {
                    mProgressDialog.dismiss()
                    profileViewModel.logoutUser()
                }

                Status.NETWORK_ERROR -> {
                    mProgressDialog.dismiss()
                    runOnUiThread {
                        fullScreenDialog(R.layout.dialog_no_internet) {
                            val commonDialog = DialogNoInternetBinding.inflate(layoutInflater)
                            setContentView(commonDialog.root)
                            commonDialog.apply {
                                tryAgainBTN.setSafeOnClickListener {
                                    dismiss()
                                    getProfile()
                                }
                            }
                        }
                    }
                }
            }
        }
        profileViewModel.alertSettingResponse.observe(this) { response ->
            when (response.status) {
                Status.LOADING -> mProgressDialog.showProgressDialog()
                Status.SUCCESS -> {
                    mProgressDialog.dismissProgressDialog()
                    finish()
                }

                Status.ERROR -> {
                    mProgressDialog.dismissProgressDialog()
                    finish()
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
                                    binding.sendBTN.post { binding.sendBTN.performClick() }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun setUserData(it: LoginResponse.Data) {
        Log.i(
            "TAG==>",
            "setUpObserver: " + it.objectCategory + "==" + it.objectDuration + "==" + it.foodDuration
        )

        binding.apply {
            try {
                categorySelectedIds = it.objectCategory?.split(",")!!
                Log.i("TAG==>", "list slit: " + categorySelectedIds.size)
                kmRS.setLabelFormatter { value ->
                    String.format(Locale.US, "%.0f", value)
                }
                foodKmRS.setLabelFormatter { value ->
                    String.format(Locale.US, "%.0f", value)
                }
                kmRS.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: Slider) {
                    }
                    override fun onStopTrackingTouch(slider: Slider) {
                        val startValue = String.format(Locale.US, "%.0f", slider.value)
                        endKmTV.text = getString(R.string.km, startValue)
                    }
                })
                foodKmRS.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: Slider) {
                    }
                    override fun onStopTrackingTouch(slider: Slider) {
                        val startValue = String.format(Locale.US, "%.0f", slider.value)
                        foodEndKmTV.text = getString(R.string.km, startValue)
                    }
                })


                if (it.objectDuration.isNullOrEmpty()) {
                    kmRS.value = 10F
                    objectSwitch.isChecked = false
                } else {
                    objectSwitch.isChecked = true
                    kmRS.value = it.objectDuration.toFloat()
                    endKmTV.text="${it.objectDuration} km"
                }

                if (it.foodDuration.isNullOrEmpty()) {
                    foodKmRS.value = 10F
                    foodSwitch.isChecked = false
                } else {
                    foodSwitch.isChecked = true
                    foodKmRS.value = it.foodDuration.toFloat()
                    foodEndKmTV.text="${it.foodDuration} km"
                }
                for (i in selectedCategories.indices)
                {
                    for(j in categorySelectedIds.indices)
                    {
                        if(selectedCategories[i].uniqueId.toString()==categorySelectedIds[j])
                        {
                            selectedCategories[i].isSelected=true
                        }
                    }
                }
                alertSelectedCategory()
                selectedCategoryAdapter.notifyDataSetChanged()
                categoryAdapter?.notifyDataSetChanged()

            } catch (e: Exception) {
                Log.i("TAG==>", "setUserData: " + e.message)
            }
        }
    }


    override fun onBaseBackPressed() {
        finish()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemClickCategory(categoryData: CategoryResponse.Category) {
//        categoryId = categoryData.uniqueId!!
//        categoryName = categoryData.title.toString()
//        isSubmit = false
//
//        if (!selectedCategories.contains(categoryData)) {
//            selectedCategories.add(categoryData)
//            selectedCategoryAdapter.notifyDataSetChanged()
//        }

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemRemoveCategory(position: Int, id: String) {
        selectedCategories.find { it.uniqueId==id }?.isSelected=false
        selectedCategoryAdapter.selectedCategoryList.removeAt(position)
        selectedCategoryAdapter.notifyItemRemoved(position)
    }
}