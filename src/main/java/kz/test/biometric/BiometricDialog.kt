package kz.test.biometric

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import kz.test.biometric.databinding.DialogBiometricBinding

private const val BASE_URL = "https://test.biometric.kz/?"
private const val RESULT_SUCCESS_URL = "https://test.biometric.kz/test-ok.html"
private const val RESULT_FAILURE_URL = "https://test.biometric.kz/test-fail.html"
private const val REQUEST_KEY = "biometricKey"
private const val KEY_URL_RESULT = "keyUrlResult"
private const val SUCCESS = "success"
private const val FAILURE = "failure"

class BiometricDialog : DialogFragment(R.layout.dialog_biometric) {

    private val permission = arrayOf(
        Manifest.permission.CAMERA
    )
    private lateinit var binding: DialogBiometricBinding
    private val token by lazy { arguments?.getString("keyToken").orEmpty() }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogBiometricBinding.inflate(layoutInflater)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (!isPermissionGranted()) {
            askPermissions()
        } else {
            binding.webViewSetup()
        }
        return binding.root
    }

    private fun askPermissions() {
        register.launch(Manifest.permission.CAMERA)
    }

    private fun isPermissionGranted(): Boolean {
        permission.forEach {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    it
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private val register = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            binding.webViewSetup()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun DialogBiometricBinding.webViewSetup() {
        webView.apply {
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    request.grant(request.resources)
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    view?.let {
                        when (it.url) {
                            RESULT_SUCCESS_URL -> parentFragmentManager.setFragmentResult(
                                REQUEST_KEY,
                                bundleOf(KEY_URL_RESULT to SUCCESS)
                            )
                            RESULT_FAILURE_URL -> parentFragmentManager.setFragmentResult(
                                REQUEST_KEY,
                                bundleOf(KEY_URL_RESULT to FAILURE)
                            )
                        }
                    }
                    super.onProgressChanged(view, newProgress)
                }
            }
            loadUrl("${BASE_URL}api_key=$token")
            settings.javaScriptEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.domStorageEnabled = true
            settings.allowContentAccess = true
            settings.mediaPlaybackRequiresUserGesture = false
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return super.shouldOverrideUrlLoading(view, request)
                }
            }
        }
    }

    companion object {

        private val instance: BiometricDialog = BiometricDialog()

        fun show(
            token: String,
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            onUrlChanged: OnUrlChangeListener
        ) {
            instance.apply {
                arguments = bundleOf("keyToken" to token)
                show(fragmentManager, null)
            }
            fragmentManager.setFragmentResultListener(REQUEST_KEY, lifecycleOwner) { _, bundle ->
                val type = bundle.getString(KEY_URL_RESULT)
                if (type != null) {
                    when (type) {
                        SUCCESS -> onUrlChanged.onResultSuccess(SUCCESS)
                        FAILURE -> onUrlChanged.onResultFailure(FAILURE)
                    }
                }
            }
        }

        fun dismiss() {
            instance.dismiss()
        }
    }
}