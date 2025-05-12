package com.app.yajari.base
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.app.yajari.R
import com.app.yajari.databinding.BottomDialogLocationBinding
import com.app.yajari.databinding.DialogLogoutBinding
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.utils.commonDialog
import com.app.yajari.utils.getStr
import com.app.yajari.utils.setSafeOnClickListener
import com.app.yajari.utils.start
import com.app.yajari.utils.visible
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

abstract class BaseFragment<B : ViewBinding> : Fragment() {
    private var mBottomLocationBinding: BottomDialogLocationBinding? = null
    private val intervalMillis = 10
    private var listener:LocationListener?=null

    abstract fun getViewBinding(): B
    lateinit var binding: B
    abstract fun initObj()
    abstract fun click()
    abstract fun setUpObserver()

    abstract fun onBaseBackPressed()
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            requireActivity()
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (::binding.isInitialized.not()) {
            binding = getViewBinding()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObj()
        click()
        setUpObserver()
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBaseBackPressed()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onDestroy() {
        super.onDestroy()
        Runtime.getRuntime().gc()
        viewModelStore.clear()
        //this.binding
    }
    fun assignLocationListener(locationListener: LocationListener)
    {
        this.listener=locationListener
    }

    interface LocationListener{
        fun onLocationGet(location: Location)
        fun onGetAddressFromPlacePicker(latitude:Double,longitude:Double,address:String)
    }

    private fun showLocationDialog(type: String) {
        val mBottomSheetDialog =
            BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        mBottomLocationBinding = BottomDialogLocationBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomLocationBinding!!.root)
        mBottomSheetDialog.setCanceledOnTouchOutside(false)
        mBottomSheetDialog.setCancelable(false)

        mBottomLocationBinding?.apply {
           // closeIV.visible()
            if (type == "permission") {
                goBTN.text = getString(R.string.go_to_setting)
            } else {
                goBTN.text = getString(R.string.enable_location)
            }
            goBTN.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
                val intent: Intent
                if (type == "permission") {
                    intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
                    intent.data = uri
                } else {
                    intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                }
                settingLauncher.launch(intent)
            }
            closeIV.setSafeOnClickListener {
                mBottomSheetDialog.dismiss()
            }
        }
        mBottomSheetDialog.show()
    }
    private val settingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            runTimePermission()
        }

     fun runTimePermission() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                getLocation()
            } else {
                showLocationDialog("location")
            }
        } else {
            requestPermission()
        }
    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            showLocationDialog("permission")
        }
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            permission.entries.forEach {
                if (it.value) {
                    getLocation()
                    return@registerForActivityResult
                } else {
                    showLocationDialog("permission")
                    return@registerForActivityResult
                }
            }
        }

    private fun getLocation() {
        lifecycleScope.launch {
            fusedLocationProviderClient.locationFlow()
                .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED).collect {
                    listener?.onLocationGet(it)
                }
        }
    }

    @Suppress("DEPRECATION")
    private var locationRequest =
        LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, intervalMillis.toLong())
            .setWaitForAccurateLocation(true).setMinUpdateIntervalMillis(intervalMillis.toLong())
            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            .setMaxUpdateDelayMillis(intervalMillis.toLong()).build()
            .setNumUpdates(1)

    private fun FusedLocationProviderClient.locationFlow() = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                try {
                    result.lastLocation?.let { trySend(it) }?.isSuccess
                } catch (e: Exception) {
                    result.lastLocation?.let { trySend(it) }?.isFailure
                    Toast.makeText(
                        requireContext(),
                        "onLocationResult:- ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            ).addOnFailureListener { e -> close(e) }
        } else {
            Log.e("CallBack", "Again Ask Permission")
            runTimePermission()
        }

        awaitClose {
            close()
            channel.close()
            removeLocationUpdates(callback)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    fun loginContinueDialog()
    {
        requireActivity().commonDialog(R.layout.dialog_logout) {
            run {
                val verifyDialog = DialogLogoutBinding.inflate(layoutInflater)
                setContentView(verifyDialog.root)
                verifyDialog.apply {
                    msgTV.text=requireActivity().getStr(R.string.please_login_continue)
                    yesBTN.setSafeOnClickListener {
                        dismiss()
                        requireActivity().start<LoginActivity>("1")
                    }
                    noBTN.setSafeOnClickListener {
                        dismiss()
                    }
                }
            }
        }
    }
     fun startPlacesActivity() {
        Places.initialize(
            requireContext(), getString(R.string.map_key)
        )
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS
        )
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(requireContext())
        autoCompleteResult.launch(intent)

    }


    private var autoCompleteResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.let {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    println(place.address!!.toString())
                    listener?.onGetAddressFromPlacePicker(latitude = place.latLng!!.latitude, longitude = place.latLng!!.longitude, address = place.address!!)
                }
            }
        }

}