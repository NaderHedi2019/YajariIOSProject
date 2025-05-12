package com.app.yajari.ui.our_partner
import androidx.navigation.fragment.findNavController
import com.app.yajari.R
import com.app.yajari.base.BaseFragment
import com.app.yajari.data.PartnerData
import com.app.yajari.databinding.FragmentOurPartnerBinding
import com.app.yajari.utils.changeStatusBarColor
import com.app.yajari.utils.setSafeOnClickListener


class OurPartnerFragment : BaseFragment<FragmentOurPartnerBinding>() {
    override fun getViewBinding()= FragmentOurPartnerBinding.inflate(layoutInflater)
    override fun initObj() {
        requireActivity().changeStatusBarColor("#FFFFFF")
        setOurPartnerRV()
    }

    override fun click() {
    binding.apply {
        toolbar.titleTV.text = getString(R.string.out_partner)
        toolbar.backIV.setImageResource(R.drawable.ic_side_menu_black)
        toolbar.backIV.setSafeOnClickListener {
           // (activity as MainActivity).openDrawer()
        }
    }
    }

    override fun setUpObserver() {

    }

    override fun onBaseBackPressed() {
        findNavController().navigate(R.id.navigation_home)
    }

    private fun setOurPartnerRV()
    {
        val partnerList= mutableListOf<PartnerData>()
        partnerList.add(PartnerData(R.drawable.p_target,"Target"))
        partnerList.add(PartnerData(R.drawable.p_ey,"EY"))
        partnerList.add(PartnerData(R.drawable.p_ups,"Ups"))
        partnerList.add(PartnerData(R.drawable.p_petrochina,"Petrochina"))
        partnerList.add(PartnerData(R.drawable.p_huawei,"Huawei"))
        partnerList.add(PartnerData(R.drawable.p_at,"AT & T"))
        partnerList.add(PartnerData(R.drawable.p_shell,"Shell"))
//        binding.partnerRV.apply {
//            adapter=OurPartnerAdapter(partnerList)
//        }
    }

}