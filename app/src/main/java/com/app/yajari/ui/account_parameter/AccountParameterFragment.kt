package com.app.yajari.ui.account_parameter
import androidx.navigation.fragment.findNavController
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.databinding.BottomDialogConfirmationBinding
import com.app.yajari.databinding.BottomDialogDeleteConfirmationBinding
import com.app.yajari.databinding.FragmentAccountParameterBinding
import com.app.yajari.ui.change_password.ChangePasswordActivity
import com.app.yajari.ui.edit_profile.EditProfileActivity
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.getStr
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.start
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog


class AccountParameterFragment : BaseFragment<FragmentAccountParameterBinding>() {
    private var mBottomConfirmBinding: BottomDialogDeleteConfirmationBinding? = null

    override fun getViewBinding()=FragmentAccountParameterBinding.inflate(layoutInflater)

    override fun initObj() {
        requireActivity().changeStatusBarColor("#FFFFFF")

    }

    override fun click() {
        binding.apply {
            toolbar.titleTV.text = getString(R.string.account_parameter)
            toolbar.backIV.setImageResource(R.drawable.ic_side_menu_black)
            toolbar.backIV.setSafeOnClickListener {
              //  (activity as MainActivity).openDrawer()
            }
            changePasswordTV.setSafeOnClickListener {
                requireActivity().start<ChangePasswordActivity>("2")
            }
            infoTV.setSafeOnClickListener {
                requireActivity().start<EditProfileActivity>("2")
            }
            deleteAccountTV.setSafeOnClickListener {
                showConfirmDialog()
            }
        }
    }

    override fun setUpObserver() {

    }

    override fun onBaseBackPressed() {
        findNavController().navigate(R.id.navigation_home)
    }
    private fun showConfirmDialog() {
        val mBottomSheetDialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        mBottomConfirmBinding = BottomDialogDeleteConfirmationBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomConfirmBinding!!.root)
        mBottomSheetDialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        mBottomSheetDialog.behavior.state= BottomSheetBehavior.STATE_EXPANDED
        mBottomConfirmBinding?.apply {
            yesBTN.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
            }
            noBTN.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
            }
            closeIV.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
            }

        }
        mBottomSheetDialog.show()
    }
}