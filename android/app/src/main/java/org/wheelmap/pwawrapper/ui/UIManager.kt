package org.wheelmap.pwawrapper.ui

import android.app.Activity
import android.app.ActivityManager
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import org.wheelmap.pwawrapper.Configuration
import org.wheelmap.pwawrapper.R

class UIManager(private val activity: Activity) {

    private val webView: WebView = activity.findViewById<View>(R.id.webView) as WebView
    private val loadingContainer: FrameLayout = activity.findViewById<View>(R.id.loadingContainer) as FrameLayout
    private val progressBar: ProgressBar = activity.findViewById<View>(R.id.progressBarBottom) as ProgressBar
    private val offlineContainer: LinearLayout = activity.findViewById<View>(R.id.offlineContainer) as LinearLayout
    private var pageLoaded = false

    val isLoaded: Boolean
            get() { return pageLoaded }

    init {
        // set click listener for offline-screen
        offlineContainer.setOnClickListener {
            webView.loadUrl(Configuration.baseUrl)
            setOffline(false)
        }

        webView.alpha = 0.0f
        webView.translationX = -400.0f
    }

    // Set Loading Progress for ProgressBar
    fun setLoadingProgress(progress: Int) {
        // set progress in UI
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBar.setProgress(progress, true)
        } else {
            progressBar.progress = progress
        }

        // hide ProgressBar if not applicable
        if (progress in 0..99) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.INVISIBLE
        }

        // get app screen back if loading is almost complete
        if (progress >= Configuration.PROGRESS_THRESHOLD && !pageLoaded) {
            setLoading(false)
        }
    }

    // Show loading animation screen while app is loading/caching the first time
    fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            loadingContainer.visibility = View.VISIBLE
            webView.animate().translationX(-400.0f).alpha(0.0f).setInterpolator(AccelerateInterpolator()).setDuration(500).start()
        } else {
            webView.visibility = View.VISIBLE
            offlineContainer.visibility = View.INVISIBLE
            webView.animate().translationX(0.0f).alpha(1f).setInterpolator(DecelerateInterpolator()).setDuration(500).start()
            loadingContainer.visibility = View.INVISIBLE
        }
        pageLoaded = !isLoading
    }

    // handle visibility of offline screen
    fun setOffline(offline: Boolean) {
        if (offline) {
            webView.visibility = View.INVISIBLE
            loadingContainer.visibility = View.INVISIBLE
            offlineContainer.visibility = View.VISIBLE
        } else {
            webView.visibility = View.VISIBLE
            offlineContainer.visibility = View.INVISIBLE
        }
    }

    // set icon in recent activity view to a white one to be visible in the app bar
    fun changeRecentAppsIcon() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val iconWhite = BitmapFactory.decodeResource(activity.resources, R.drawable.ic_appbar)

            val typedValue = TypedValue()
            val theme = activity.theme
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
            val color = typedValue.data

            val description = ActivityManager.TaskDescription(
                    activity.resources.getString(R.string.app_name),
                    iconWhite,
                    color
            )
            activity.setTaskDescription(description)
            iconWhite.recycle()
        }
    }
}
