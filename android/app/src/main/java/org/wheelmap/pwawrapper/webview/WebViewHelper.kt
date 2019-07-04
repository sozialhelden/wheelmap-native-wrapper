package org.wheelmap.pwawrapper.webview

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.webkit.*
import org.wheelmap.pwawrapper.Configuration
import org.wheelmap.pwawrapper.R
import org.wheelmap.pwawrapper.ui.UIManager


class WebViewHelper(private val activity: Activity, private val uiManager: UIManager) {
    private val webView: WebView
    private val webSettings: WebSettings

    private var geolocationOrigin: String? = null
    private var geolocationCallback: GeolocationPermissions.Callback? = null

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
        geolocationCallback?.invoke(geolocationOrigin, allow, allow)
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            webSettings.databasePath = activity.applicationContext.filesDir.absolutePath
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            webSettings.setAppCacheMaxSize(java.lang.Long.MAX_VALUE)
        }
        webSettings.domStorageEnabled = true
        webSettings.setAppCachePath(activity.applicationContext.cacheDir.absolutePath)
        webSettings.setAppCacheEnabled(true)
        webSettings.databaseEnabled = true

        // enable mixed content mode conditionally
        if (Configuration.ENABLE_MIXED_CONTENT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                userAgent = userAgent + " " + Configuration.USER_AGENT_POSTFIX
            }
            webSettings.userAgentString = userAgent
        }

        // enable HTML5-support
        webView.webChromeClient = object : WebChromeClient() {


            //simple yet effective redirect/popup blocker
            override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
                val href = view.handler.obtainMessage()
                view.requestFocusNodeHref(href)
                val popupUrl = href.data.getString("url")
                if (popupUrl != null) {
                    //it's null for most rogue browser hijack ads
                    webView.loadUrl(popupUrl)
                    return true
                }
                return false
            }

            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                // Geolocation permissions coming from this app's Manifest will only be valid for devices with
                // API_VERSION < 23. On API 23 and above, we must check for permissions, and possibly
                // ask for them.
                val perm = Manifest.permission.ACCESS_FINE_LOCATION
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ContextCompat.checkSelfPermission(activity, perm) == PackageManager.PERMISSION_GRANTED) {
                    // we're on SDK < 23 OR user has already granted permission
                    callback.invoke(origin, true, true)
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
                        // ask the user for permission
                        val requestFineLocationCode = 1
                        ActivityCompat.requestPermissions(activity, arrayOf(perm), requestFineLocationCode)

                        // we will use these when user responds
                        geolocationOrigin = origin
                        geolocationCallback = callback
                    }
                }
            }

            // update ProgressBar
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                uiManager.setLoadingProgress(newProgress)
                super.onProgressChanged(view, newProgress)
            }
        }

        // Set up Webview client
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                handleUrlLoad(view, url)
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
    fun onPause() {
        webView.onPause()
    }

    fun onResume() {
        webView.onResume()
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
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                if (intent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(intent)
                } else {
                    showNoAppDialog(activity)
                }
            } catch (e: Exception) {
                showNoAppDialog(activity)
            }

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
