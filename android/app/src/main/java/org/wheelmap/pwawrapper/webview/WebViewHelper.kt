package org.wheelmap.pwawrapper.webview

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import android.view.View
import android.webkit.*
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.wheelmap.pwawrapper.BuildConfig
import org.wheelmap.pwawrapper.Configuration
import org.wheelmap.pwawrapper.R
import org.wheelmap.pwawrapper.ui.UIManager
import java.io.File
import java.io.IOException
import java.security.AccessController.getContext


const val REQUEST_TAKE_PHOTO = 1
const val PERMISSION_REQUEST_FINE_LOCATION = 1
const val PERMISSION_REQUEST_CAMERA = 2

class WebViewHelper(private val activity: Activity, private val uiManager: UIManager) {
    private val webView: WebView
    private val webSettings: WebSettings

    private var lastPhotoFileProviderContentSchemeUri: Uri? = null
    private var lastFilePathCallback : ValueCallback<Array<Uri>>? = null

    private var geolocationOrigin: String? = null
    private var geolocationCallback: GeolocationPermissions.Callback? = null

    private var lastFocusLost: Long = System.currentTimeMillis()

    /**
     * Simple helper method checking if connected to Network.
     * Doesn't check for actual Internet connection!
     * @return {boolean} True if connected to Network.
     */
    private// Wifi or Mobile Network is present and connected
    val isNetworkAvailable: Boolean
        get() {
            val manager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = manager.activeNetworkInfo

            var isAvailable = false
            if (networkInfo != null && networkInfo.isConnected) {
                isAvailable = true
            }
            return isAvailable
        }

    init {
        val view = activity.findViewById<View>(org.wheelmap.pwawrapper.R.id.webView) as WebView
        this.webView = view
        this.webSettings = webView.settings
    }

    // manipulate cache settings to make sure our PWA gets updated
    private fun useCache(use: Boolean) {
        if (use) {
            webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        } else {
            webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        }
    }

    // public method changing cache settings according to network availability.
    // retrieve content from cache primarily if not connected,
    // allow fetching from web too otherwise to get updates.
    fun forceCacheIfOffline() {
        useCache(!isNetworkAvailable)
    }

    fun locationAccessGranted(allow: Boolean) {
        geolocationCallback?.invoke(geolocationOrigin, allow, false)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_FINE_LOCATION -> {
                var allow = false
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // user has allowed this permission
                    allow = true
                }

                locationAccessGranted(allow)
                return
            }
            PERMISSION_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay!
                    dispatchTakePictureIntent()
                } else {
                    // permission denied, boo!
                    Toast.makeText(activity.applicationContext, R.string.camera_access_required, Toast.LENGTH_LONG).show()
                    lastFilePathCallback!!.onReceiveValue(null)
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private fun requestCameraPermissionWithExplanationIfNecessary() {
        val doRequest = { ->
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA);
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            val snackbar = Snackbar.make(activity.webView, R.string.camera_access_required, Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction(R.string.ok) {
                doRequest()
            }
            snackbar.show()
        } else {
            doRequest()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val storageDir: File = activity.applicationContext.cacheDir.resolve("images")
        storageDir.mkdir()
        return File.createTempFile("photo", ".jpg", storageDir)
    }

    private fun dispatchTakePictureIntent() {
        if (!activity.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(activity.applicationContext, R.string.device_has_no_camera, Toast.LENGTH_LONG).show()
            return
        }

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            // Create the File where the photo should go
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                Toast.makeText(activity.applicationContext, R.string.cannot_generate_image_tempfile, Toast.LENGTH_LONG).show()
                lastFilePathCallback!!.onReceiveValue(null)
                null
            }
            // Continue only if the File was successfully created
            photoFile?.also {
                lastPhotoFileProviderContentSchemeUri = FileProvider.getUriForFile(
                        activity,
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, lastPhotoFileProviderContentSchemeUri)
                activity.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        when (requestCode) {
            REQUEST_TAKE_PHOTO -> {
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(activity.applicationContext, R.string.no_image_uploaded, Toast.LENGTH_SHORT).show()
                    lastFilePathCallback!!.onReceiveValue(arrayOf())
                    return;
                }
                lastFilePathCallback!!.onReceiveValue(arrayOf(lastPhotoFileProviderContentSchemeUri!!))
                return;
            }
        }
    }

    // handles initial setup of webview
    fun setupWebView() {
        // accept cookies
        CookieManager.getInstance().setAcceptCookie(true)
        // enable JS
        @SuppressLint("SetJavaScriptEnabled")
        webSettings.javaScriptEnabled = true

        webSettings.setGeolocationEnabled(true)
        webSettings.setGeolocationDatabasePath(activity.applicationContext.filesDir.absolutePath)

        // must be set for our js-popup-blocker:
        webSettings.setSupportMultipleWindows(true)

        // PWA settings
        webSettings.domStorageEnabled = true
        webSettings.setAllowFileAccess(true)
        webSettings.setAllowContentAccess(true)
        webSettings.databaseEnabled = true

        // enable mixed content mode conditionally
        if (Configuration.ENABLE_MIXED_CONTENT) {
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        // retrieve content from cache primarily if not connected
        forceCacheIfOffline()

        // set User Agent
        if (Configuration.OVERRIDE_USER_AGENT || Configuration.POSTFIX_USER_AGENT) {
            var userAgent = webSettings.userAgentString
            if (Configuration.OVERRIDE_USER_AGENT) {
                userAgent = Configuration.USER_AGENT
            }
            if (Configuration.POSTFIX_USER_AGENT) {
                val pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                val version = pInfo.versionName;
                userAgent = userAgent + " " + Configuration.USER_AGENT_POSTFIX_PREFIX + "/" + version + " (Android)"
            }
            webSettings.userAgentString = userAgent
        }

        // enable HTML5-support
        webView.webChromeClient = object : WebChromeClient() {

            override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
                // create a temporary webview to evaluate the url from window.open()
                val newWebView = WebView(activity)
                newWebView.webViewClient = object: WebViewClient() {
                    override fun onPageStarted(tempView: WebView, url: String, favicon: Bitmap?) {
                        // treat as an external url
                        handleExternalLinks(url, tempView.context)
                        // remove temp view
                        newWebView.stopLoading()
                        newWebView.destroy()
                    }
                }

                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()

                return true
            }

            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                // Geolocation permissions coming from this app's Manifest will only be valid for devices with
                // API_VERSION < 23. On API 23 and above, we must check for permissions, and possibly
                // ask for them.
                val perm = Manifest.permission.ACCESS_FINE_LOCATION
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ContextCompat.checkSelfPermission(activity, perm) == PackageManager.PERMISSION_GRANTED) {
                    // we're on SDK < 23 OR user has already granted permission
                    callback.invoke(origin, true, false)
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
                        // ask the user for permission
                        ActivityCompat.requestPermissions(activity, arrayOf(perm), PERMISSION_REQUEST_FINE_LOCATION)

                        // we will use these when user responds

                    } else {
                        val isPermitted = ActivityCompat.checkSelfPermission(activity, perm) == PackageManager.PERMISSION_GRANTED ||
                                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        callback(origin, isPermitted, false)
                    }

                    geolocationOrigin = origin
                    geolocationCallback = callback
                }
            }

            // update ProgressBar
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                uiManager.setLoadingProgress(newProgress)
                super.onProgressChanged(view, newProgress)
            }

            // For Lollipop 5.0+ Devices
            override fun onShowFileChooser(
                    mWebView: WebView,
                    filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: WebChromeClient.FileChooserParams
            ): Boolean {
                lastFilePathCallback = filePathCallback
                if (!hasCameraPermission()) {
                    requestCameraPermissionWithExplanationIfNecessary()
                } else {
                    dispatchTakePictureIntent()
                }
                return true
            }
        }

        // Set up Webview client
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Handler().postDelayed({ checkTimeout() }, Configuration.TIMEOUT_DURATION)

                handleUrlLoad(view, url)
            }

            private fun checkTimeout() {
                if (!uiManager.isLoaded) {
                    handleLoadError(408)
                }
            }

            // handle loading error by showing the offline screen
            @Deprecated("")
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    handleLoadError(errorCode)
                }
            }

            @TargetApi(Build.VERSION_CODES.M)
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // new API method calls this on every error for each resource.
                    // we only want to interfere if the page itself got problems.
                    val url = request.url.toString()
                    if (view.url == url) {
                        handleLoadError(error.errorCode)
                    }
                }
            }
        }
    }


    // Lifecycle callbacks
    fun onDestroy() {
        webView.destroy()
    }

    fun onPause() {
        webView.onPause()

        lastFocusLost = System.currentTimeMillis()
    }

    fun onResume() {
        webView.onResume()
        val timePassed = System.currentTimeMillis() - lastFocusLost
        if (timePassed > Configuration.RELOAD_AFTER_FOCUS_LOST_DURATION) {
            webView.reload()
        }
    }

    // show "no app found" dialog
    private fun showNoAppDialog(thisActivity: Activity) {
        AlertDialog.Builder(thisActivity)
                .setTitle(R.string.noapp_heading)
                .setMessage(R.string.noapp_description)
                .show()
    }

    // handle load errors
    private fun handleLoadError(errorCode: Int) {
        if (errorCode != WebViewClient.ERROR_UNSUPPORTED_SCHEME) {
            uiManager.setOffline(true)
        } else {
            // Unsupported Scheme, recover
            Handler().postDelayed({ goBack() }, 100)
        }
    }

    // handle external urls
    private fun handleUrlLoad(view: WebView, url: String): Boolean {
        // prevent loading content that isn't ours
        if (!url.startsWith(Configuration.baseUrl)) {
            // stop loading
            view.stopLoading()

            // open external URL in Browser/3rd party apps instead
            handleExternalLinks(url, view.context)

            // return value for shouldOverrideUrlLoading
            return true
        } else {
            // let WebView load the page!
            // activate loading animation screen
            uiManager.setLoading(true)
            // return value for shouldOverrideUrlLoading
            return false
        }
    }

    private fun handleExternalLinks(url: String, context: Context) {
        try {
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            // handle location settings
            if (url == "https://support.google.com/android/answer/6179507") {
                // remove cached geo location settings
                webView.clearCache(true)
                webView.clearHistory()

                intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)

                val uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            showNoAppDialog(activity)
        }
    }

    // handle back button press
    fun goBack(): Boolean {
        if (webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return false
    }

    // load app startpage
    fun loadHome() {
        webView.loadUrl(Configuration.baseUrl)
    }

    // load URL from intent
    fun loadIntentUrl(url: String) {
        if (url != "" && url.contains(Configuration.baseHost)) {
            webView.loadUrl(url)
        } else {
            // Fallback
            loadHome()
        }
    }

}
