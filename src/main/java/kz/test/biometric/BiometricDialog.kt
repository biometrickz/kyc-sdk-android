package kz.test.biometric

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import kz.test.biometric.databinding.DialogBiometricBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


//private const val BASE_URL = "https://test.biometric.kz/document?"
private const val BASE_URL = "htpackage kz.test.biometric

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import kz.test.biometric.databinding.DialogBiometricBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


//private const val BASE_URL = "https://test.biometric.kz/document?"
private const val BASE_URL = "https://test.biometric.kz/demo/short?"
private const val REQUEST_KEY = "biometricKey"
private const val KEY_URL_RESULT = "keyUrlResult"
private const val KEY_SESSION_RESULT = "keySessionResult"
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

    private fun getSession(session: String) {
        Thread {
            try {
                val url = URL("https://test.biometric.kz/v1/main/session/$session")
                val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
                conn.connect()
                var br: BufferedReader? = null
                if (conn.responseCode in 100..399) {
                    br = BufferedReader(InputStreamReader(conn.inputStream))
                    val strCurrentLine: String = br.readLines().toString().replace(",", "")

                    if (strCurrentLine.contains("\"result\":false")) {
                        parentFragmentManager.setFragmentResult(
                            REQUEST_KEY,
                            bundleOf(KEY_URL_RESULT to FAILURE, KEY_SESSION_RESULT to strCurrentLine)
                        )
                    } else if (strCurrentLine.contains("\"result\":true")) {
                        parentFragmentManager.setFragmentResult(
                            REQUEST_KEY,
                            bundleOf(KEY_URL_RESULT to SUCCESS, KEY_SESSION_RESULT to strCurrentLine))
                    } else {
                        parentFragmentManager.setFragmentResult(
                            REQUEST_KEY,
                            bundleOf(KEY_URL_RESULT to FAILURE, KEY_SESSION_RESULT to strCurrentLine)
                        )
                    }
                } else {
                    br = BufferedReader(InputStreamReader(conn.errorStream))
                    val strCurrentLine: String = br.readLines().toString().replace(",", "")
                    parentFragmentManager.setFragmentResult(
                        REQUEST_KEY,
                        bundleOf(KEY_URL_RESULT to FAILURE, KEY_SESSION_RESULT to strCurrentLine)
                    )
                }
            } catch (e: Exception) { }
        }.start()
    }

    private fun extractSession(url: String): String {
        val text = url.substring(url.indexOf("session", ignoreCase = true) + 8, url.length)
        return text
    }

    private var resultCount = 0

    @SuppressLint("SetJavaScriptEnabled")
    private fun DialogBiometricBinding.webViewSetup() {
        webView.apply {
            webChromeClient = object : WebChromeClient() {

                override fun onPermissionRequest(request: PermissionRequest) {
                    request.grant(request.resources)
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    view?.let {
                        if (it.url?.contains("test-ok") == true) {
                            getSession(extractSession(it.url.orEmpty()))
                        } else if (it.url?.contains("test-fail") == true) {
                            parentFragmentManager.setFragmentResult(
                                REQUEST_KEY,
                                bundleOf(
                                    KEY_URL_RESULT to FAILURE,
                                    KEY_SESSION_RESULT to FAILURE
                                )
                            )
                        } else {
                        }
                    }
                    super.onProgressChanged(view, newProgress)
                    resultCount++
                }
            }
            settings.javaScriptEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.domStorageEnabled = true
            settings.allowContentAccess = true
            settings.mediaPlaybackRequiresUserGesture = false

            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            clearCache(true)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return super.shouldOverrideUrlLoading(view, request)
                }
            }
            loadUrl("${BASE_URL}api_key=$token&webview=true")
        }
    }

    fun getCameraId(facing: Int): String {
        val manager = requireActivity().getSystemService(CAMERA_SERVICE) as CameraManager

        return manager.cameraIdList.first {
            manager
                .getCameraCharacteristics(it)
                .get(CameraCharacteristics.LENS_FACING) == facing
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
                val sessionResult = bundle.getString(KEY_SESSION_RESULT).orEmpty()
                if (type != null) {
                    when (type) {
                        SUCCESS -> onUrlChanged.onResultSuccess(sessionResult)
                        FAILURE -> onUrlChanged.onResultFailure(sessionResult)
                    }
                }
                instance.dismiss()
            }
        }

        fun dismiss() {
            instance.dismiss()
        }
    }
}