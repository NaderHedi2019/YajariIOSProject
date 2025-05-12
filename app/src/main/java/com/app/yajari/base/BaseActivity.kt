package com.app.yajari.base
import android.Manifest
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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.ViewPumpAppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.app.yajari.R
import com.app.yajari.databinding.BottomDialogLocationBinding
import com.app.yajari.databinding.DialogLogoutBinding
import com.app.yajari.ui.login.LoginActivity
import com.app.yajari.utils.Constant.Companion.sDeviceId
import com.app.yajari.utils.Constant.Companion.sFcmKey
import com.app.yajari.utils.commonDialog
import com.app.yajari.utils.getDeviceID
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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.messaging.FirebaseMessaging
import dev.b3nedikt.app_locale.AppLocale
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

abstract class BaseActivity <B : ViewBinding> : AppCompatActivity() {
    abstract fun getViewBinding(): B
    lateinit var binding: B
    abstract fun initObj()
    abstract fun click()
    abstract fun setUpObserver()
    abstract fun onBaseBackPressed()

    private var mBottomLocationBinding: BottomDialogLocationBinding? = null
    private val intervalMillis = 10
    private var listener: LocationListener?=null
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            this@BaseActivity
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (::binding.isInitialized.not()) {
            binding = getViewBinding()
            setContentView(binding.root)
        }

        initObj()
        click()
        setUpObserver()

        if (sDeviceId == "") {
            sDeviceId = getDeviceID(this)
        }

        if (sFcmKey == "") {
            firebaseToken()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBaseBackPressed()
            }
        })

    }


    override fun onDestroy() {
        super.onDestroy()
        Runtime.getRuntime().gc()
        viewModelStore.clear()
        this.binding
    }


    private fun firebaseToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.i("APP==>", "Token Failed==>", task.exception)
                return@OnCompleteListener
            }
            sFcmKey = task.result.toString()
            Log.i("APP==>", sFcmKey)
        })
    }

    fun loginContinueDialog()
    {
        commonDialog(R.layout.dialog_logout) {
            run {
                val verifyDialog = DialogLogoutBinding.inflate(layoutInflater)
                setContentView(verifyDialog.root)
                verifyDialog.apply {
                    msgTV.text=getStr(R.string.please_login_continue)
                    yesBTN.setSafeOnClickListener {
                        dismiss()
                        start<LoginActivity>("1")
                    }
                    noBTN.setSafeOnClickListener {
                        dismiss()
                    }
                }
            }
        }
    }
    fun assignLocationListener(locationListener: LocationListener)
    {
        this.listener=locationListener
    }

    interface LocationListener{
        fun onLocationGet(location: Location)
    }

    private fun showLocationDialog(type: String) {
        val mBottomSheetDialog =
            BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)
        mBottomLocationBinding = BottomDialogLocationBinding.inflate(layoutInflater)
        mBottomSheetDialog.setContentView(mBottomLocationBinding!!.root)
        mBottomSheetDialog.setCanceledOnTouchOutside(false)
        mBottomSheetDialog.setCancelable(false)

        mBottomLocationBinding?.apply {
            closeIV.visible()
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
                    val uri: Uri = Uri.fromParts("package", packageName, null)
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
                this,
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
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
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
                        this@BaseActivity,
                        "onLocationResult:- ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                this@BaseActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this@BaseActivity,
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
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private val appCompatDelegate: AppCompatDelegate by lazy {
        ViewPumpAppCompatDelegate(
            baseDelegate = super.getDelegate(),
            baseContext = this,
            wrapContext = { baseContext -> AppLocale.wrap(baseContext) }
        )
    }


    override fun getDelegate(): AppCompatDelegate {
        return appCompatDelegate
    }

}
