package org.wheelmap.pwawrapper

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.webkit.WebView
import org.wheelmap.pwawrapper.ui.UIManager
import org.wheelmap.pwawrapper.webview.WebViewHelper

class MainActivity : AppCompatActivity() {
    // Globals
    private var uiManager: UIManager? = null
    private var webViewHelper: WebViewHelper? = null
    private var intentHandled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Setup Theme
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // Android 15
            val edgeToEdgeHandler = EdgeToEdgeHandler(this)
            edgeToEdgeHandler.adjustLayout()
        }

        Configuration.init(applicationContext)

        // allow app automation in browserstack
        WebView.setWebContentsDebuggingEnabled(true)

        // Setup Helpers
        uiManager = UIManager(this)
        webViewHelper = WebViewHelper(this, uiManager!!)

        // Setup App
        webViewHelper!!.setupWebView()
        uiManager!!.changeRecentAppsIcon()

        // Check for Intents
        try {
            val i = intent
            val intentAction = i.action
            // Handle URLs opened in Browser
            if (!intentHandled && intentAction != null && intentAction == Intent.ACTION_VIEW) {
                val intentUri = i.data
                var intentText = ""
                if (intentUri != null) {
                    intentText = intentUri.toString()
                }
                // Load up the URL specified in the Intent
                if (intentText != "") {
                    intentHandled = true
                    webViewHelper!!.loadIntentUrl(intentText)
                }
            } else {
                // Load up the Web App
                webViewHelper!!.loadHome()
            }
        } catch (e: Exception) {
            // Load up the Web App
            webViewHelper!!.loadHome()
        }

    }

    override fun onDestroy() {
        webViewHelper!!.onDestroy()
        super.onDestroy()
    }

    override fun onPause() {
        webViewHelper!!.onPause()
        super.onPause()
    }

    override fun onResume() {
        webViewHelper!!.onResume()
        super.onResume()
    }

    // Handle back-press in browser
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!webViewHelper!!.goBack()) {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        webViewHelper!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent);
        webViewHelper!!.onActivityResult(requestCode, resultCode, intent);
    }
}
