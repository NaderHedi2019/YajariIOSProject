package com.app.yajari.ui.my_package
import androidx.navigation.fragment.findNavController
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.data.PlanData
import com.app.yajari.databinding.BottomDialogConfirmationBinding
import com.app.yajari.databinding.DialogSuccessMsgBinding
import com.app.yajari.databinding.FragmentMyPackageBinding
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.commonDialog
import com.app.yajari.utils.getStr
import com.app.yajari.utils.gone
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.visible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog


class MyPackageFragment : BaseFragment<FragmentMyPackageBinding>(),PackageAdapter.PackageListener,PackageDescAdapter.PackageDescListener {
    private lateinit var packageAdapter: PackageAdapter
    private var mBottomConfirmBinding: BottomDialogConfirmationBinding? = null

    override fun getViewBinding()= FragmentMyPackageBinding.inflate(layoutInflater)

    override fun initObj() {
        requireActivity().changeStatusBarColor("#FFFFFF")
        setPackageRV()
        setSubDescRV()
    }

    override fun click() {
        binding.apply {
            toolbar.titleTV.text = getString(R.string.my_package)
            toolbar.backIV.setImageResource(R.drawable.ic_side_menu_black)
            toolbar.backIV.setSafeOnClickListener {
               // (activity as MainActivity).openDrawer()
            }
            subBTN.setSafeOnClickListener {
                showConfirmDialog()

            }
        }
    }

    override fun setUpObserver() {
    }

    override fun onBaseBackPressed() {
        findNavController().navigate(R.id.navigation_home)
    }

    private fun setPackageRV()
    {
        val packageList= mutableListOf<PlanData>()
        val descList= mutableListOf<String>()
        descList.add("Unlimited announcement add")
        descList.add("Chat with anyone")
        descList.add("Unlimited coin saving")
        descList.add("No ads")
        packageList.add(PlanData("\$75.00","/ month","Included",descList,true))
        packageList.add(PlanData("\$150.00","/ Yearly","Included",descList,false))
        binding.packageRV.apply {
            packageAdapter=PackageAdapter(requireContext(),packageList,this@MyPackageFragment,this@MyPackageFragment)
            adapter=packageAdapter
        }
    }

    override fun onItemPackageClick() {
    }

    override fun onItemDescClick(adapterPosition: Int) {
        packageAdapter.updateData(adapterPosition)
    }

    private fun setSubDescRV()
    {
        val descList= mutableListOf<String>()
        descList.add("Unlimited announcement add")
        descList.add("Chat with anyone")
        descList.add("Unlimited coin saving")
        descList.add("No ads")
        binding.packageDescRV.apply {
            adapter=PackageDescAdapter(descList,this@MyPackageFragment,0)
        }
    }

    private fun showConfirmDialog() {
        val mBottomSheetDialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        mBottomConfirmBinding = BottomDialogConfirmationBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomConfirmBinding!!.root)
        mBottomSheetDialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation
        mBottomSheetDialog.behavior.state= BottomSheetBehavior.STATE_EXPANDED
        mBottomConfirmBinding?.apply {
            msgTV.text=requireActivity().getStr(R.string.sub_confirm_msg)
            yesBTN.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
                showSuccessDialog()

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
    private fun showSuccessDialog()
    {
        requireActivity().commonDialog(R.layout.dialog_verify) {
            run {
                val verifyDialog = DialogSuccessMsgBinding.inflate(layoutInflater)
                setContentView(verifyDialog.root)
                verifyDialog.apply {
                    msgTV.text=getString(R.string.plan_purchase_msg)
                    continueBTN.setSafeOnClickListener {
                        dismiss()
                        binding.apply {
                            sTitleTV.gone()
                            packageRV.gone()
                            subBTN.gone()
                            subscriptionCL.visible()
                        }
                    }
                }
            }
        }
    }
}