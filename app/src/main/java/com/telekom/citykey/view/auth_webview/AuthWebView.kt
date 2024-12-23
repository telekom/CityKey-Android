package com.telekom.citykey.view.auth_webview

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.location.LocationManager
import android.net.Uri
import android.nfc.NfcManager
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.webkit.GeolocationPermissions
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.location.LocationManagerCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WebviewFragmentBinding
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.getDrawable
import com.telekom.citykey.utils.extensions.hasPermission
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.main.MainActivity
import java.io.File
import java.io.IOException

class AuthWebView : MainFragment(R.layout.webview_fragment) {

    private val locationManager: LocationManager by lazy { requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission ->
            onLocationPermissionResult(hasPermission)
        }

    private val getMediaContentContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onMediaContentResult(result)
        }

    private val getLocationServiceContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            geoLocationCallback?.invoke(
                geoLocationRequestOrigin,
                LocationManagerCompat.isLocationEnabled(locationManager),
                false
            )
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission ->
            onCameraPermissionResult(hasPermission)
        }

    private val args: AuthWebViewArgs by navArgs()
    private val binding by viewBinding(WebviewFragmentBinding::bind)
    private val imageFile by lazy { File.createTempFile("Egov", ".jpg", storageDir) }
    private val storageDir by lazy { File(requireContext().filesDir, "images") }
    private lateinit var photoUri: Uri
    private var tcTokenUrl = ""
    private var mediaContentCalback: ValueCallback<Array<Uri>>? = null
    private var geoLocationCallback: GeolocationPermissions.Callback? = null
    private var geoLocationRequestOrigin: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as? MainActivity)?.hideBottomNavBar()
        setupWebView()

        binding.webviewToolbar.title = args.service
        setupToolbar(binding.webviewToolbar)
        (requireActivity() as? MainActivity)?.supportActionBar?.setHomeAsUpIndicator(
            getDrawable(R.drawable.ic_profile_close)
                ?.apply {
                    colorFilter = PorterDuffColorFilter(getColor(R.color.onSurface), PorterDuff.Mode.SRC_ATOP)
                }
        )
        (requireActivity() as? MainActivity)?.supportActionBar?.setHomeActionContentDescription(R.string.accessibility_btn_close)

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack()
                    } else {
                        navigateUp()
                    }
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        if (args.hasSensitiveInfo)
            setSecureFlag()
    }

    private fun setupWebView() {
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val tokenUrl = request?.url?.getQueryParameter("tcTokenURL")
                return if (tokenUrl.isNullOrBlank()) {
                    return super.shouldOverrideUrlLoading(view, request)
                } else {
                    tcTokenUrl = tokenUrl
                    launchAuth()
                    true
                }
            }
        }
        binding.webView.settings.domStorageEnabled = true
        binding.webView.loadUrl(args.link.trim())

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                mediaContentCalback = filePathCallback
                if (requireContext().hasPermission(Manifest.permission.CAMERA)) onCameraPermissionResult(true)
                else requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                return true
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String,
                callback: GeolocationPermissions.Callback
            ) {
                geoLocationRequestOrigin = null
                geoLocationCallback = null
                if (requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
                        DialogUtil.showNoLocationServiceDialog(
                            context = requireContext(),
                            positiveClickListener = {
                                geoLocationRequestOrigin = origin
                                geoLocationCallback = callback
                                getLocationServiceContract.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            },
                            cancelClickListener = {
                                callback.invoke(origin, false, false)
                            }
                        )
                        return
                    }

                    callback.invoke(origin, true, false)
                } else {
                    geoLocationRequestOrigin = origin
                    geoLocationCallback = callback
                    requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
    }

    private fun launchAuth() {
        val adapter = (requireContext().getSystemService(Context.NFC_SERVICE) as? NfcManager)?.defaultAdapter
        if (adapter != null) {
            AuthBottomSheetDialog(tcTokenUrl, this@AuthWebView::onAuthResult)
                .show(childFragmentManager, null)
        } else {
            DialogUtil.showInfoDialog(
                context = requireContext(),
                title = R.string.egov_nfc_not_avaiable_title,
                message = R.string.egov_nfc_not_available_description
            )
        }
    }

    private fun onAuthResult(result: String?) {
        if (result == null) launchAuth()
        else binding.webView.loadUrl(result.trim())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigateUp()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateUp() {
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogMaterialTheme)
            .setTitle(R.string.egovs_003_close_dialog_title)
            .setMessage(R.string.egovs_003_close_dialog_description)
            .setNegativeButton(R.string.egovs_003_close_dialog_cancel_btn) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.egovs_003_close_dialog_close_btn) { dialog, _ ->
                binding.webView.destroy()
                findNavController().navigateUp()
                dialog.dismiss()
            }
            .show()
    }

    private fun onCameraPermissionResult(isPermissionAccepted: Boolean = false) {
        val intentArray = mutableListOf<Intent>()
        val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
        chooseFile.type = "*/*"
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intentArray.add(chooseFile)
        if (isPermissionAccepted) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                    lateinit var photoFile: File
                    try {
                        photoFile = createImageFile()
                    } catch (ex: IOException) {
                        DialogUtil.showTechnicalError(requireContext())
                    }
                    photoUri =
                        FileProvider.getUriForFile(
                            requireContext(),
                            "${BuildConfig.APPLICATION_ID}.fileprovider",
                            photoFile
                        )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                }
            }
            intentArray.add(cameraIntent)
        }
        val chooser = Intent.createChooser(galleryIntent, getString(R.string.dr_003_defect_image_option_title))
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray.toTypedArray())
        getMediaContentContract.launch(chooser)
    }

    private fun onMediaContentResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK && mediaContentCalback != null) {
            val uri: Uri? = if (result.data == null) {
                photoUri
            } else {
                result.data?.data
            }
            uri?.let { fileUri ->
                mediaContentCalback?.onReceiveValue(arrayOf(fileUri))
                mediaContentCalback = null
            }
        } else {
            mediaContentCalback?.onReceiveValue(null)
            mediaContentCalback = null
        }
    }

    private fun onLocationPermissionResult(hasPermission: Boolean) {
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            DialogUtil.showNoLocationServiceDialog(
                context = requireContext(),
                positiveClickListener = {
                    getLocationServiceContract.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            )
            return
        }

        geoLocationCallback?.invoke(geoLocationRequestOrigin, hasPermission, false)
        geoLocationCallback = null
        geoLocationRequestOrigin = null
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
        return imageFile
    }

    override fun onDestroy() {
        (requireActivity() as? MainActivity)?.revealBottomNavBar()
        if (storageDir.exists()) {
            storageDir.deleteRecursively()
        }

        if (args.hasSensitiveInfo)
            removeSecureFlag()
        super.onDestroy()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
    }
}
