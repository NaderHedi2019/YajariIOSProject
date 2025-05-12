package com.app.yajari.ui.edit_announcement
import android.view.View
import androidx.core.content.ContextCompat
import com.app.yajari.R
import com.app.yajari.base.BaseActivity
import com.app.yajari.databinding.ActivityEditAnnouncementBinding
import com.app.yajari.utils.*

class EditAnnouncementActivity : BaseActivity<ActivityEditAnnouncementBinding>() {
    override fun getViewBinding()= ActivityEditAnnouncementBinding.inflate(layoutInflater)

    override fun initObj() {
        changeStatusBarColor("#FFFFFF")
    }

    override fun click() {
        binding.apply {
            toolbar.backIV.setSafeOnClickListener {
                finish()
            }
            toolbar.titleTV.text = getStr(R.string.edit_announcement)

            descEDT.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if(hasFocus)
                {
                    aboutCL.background= ContextCompat.getDrawable(this@EditAnnouncementActivity, R.drawable.shape_edt_selected)
                    aboutIV.setImageResource(R.drawable.ic_write_about_selected)
                }
                else{
                    aboutCL.background= ContextCompat.getDrawable(this@EditAnnouncementActivity, R.drawable.shape_edt_unselected)
                    aboutIV.setImageResource(R.drawable.ic_write_about_unselected)
                }
            }
        }
    }

    override fun setUpObserver() {

    }

    override fun onBaseBackPressed() {
       finish()
    }

}