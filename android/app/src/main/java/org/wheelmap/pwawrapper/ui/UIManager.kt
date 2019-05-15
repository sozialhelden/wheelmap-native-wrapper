package org.wheelmap.pwawrapper.ui

import android.app.Activity
import android.app.ActivityManager
import android.graphics.BitmapFactory
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.ProgressBar
import org.wheelmap.pwawrapper.Constants
import org.wheelmap.pwawrapper.R

class UIManager(// Instance variables
        private val activity: Activity) {
    private val webView: WebView
    private val progressSpinner: ProgressBar
    private val progressBar: ProgressBar
    private val offlineContainer: LinearLayout
    private var pageLoaded = false

    init {
        this.progressBar = activity.findViewById<View>(R.id.progressBarBottom) as ProgressBar
        this.progressSpinner = activity.findViewById<View>(R.id.progressSpinner) as ProgressBar
        this.offlineContainer = activity.findViewById<View>(R.id.offlineContainer) as LinearLayout
        this.webView = activity.findViewById<View>(R.id.webView) as WebView

        // set click listener for offline-screen
        offlineContainer.setOnClickListener {
            webView.loadUrl(Constants.WEBAPP_URL)
            setOffline(false)
        }
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
        if (progress >= 0 && progress < 100) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.INVISIBLE
        }

        // get app screen back if loading is almost complete
        if (progress >= Constants.PROGRESS_THRESHOLD && !pageLoaded) {
            setLoading(false)
        }
    }

    // Show loading animation screen while app is loading/caching the first time
    fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressSpinner.visibility = View.VISIBLE
            webView.animate().translationX(Constants.SLIDE_EFFECT.toFloat()).alpha(0.5f).setInterpolator(AccelerateInterpolator()).start()
        } else {
            webView.translationX = (Constants.SLIDE_EFFECT * -1).toFloat()
            webView.animate().translationX(0f).alpha(1f).setInterpolator(DecelerateInterpolator()).start()
            progressSpinner.visibility = View.INVISIBLE
        }
        pageLoaded = !isLoading
    }

    // handle visibility of offline screen
    fun setOffline(offline: Boolean) {
        if (offline) {
            setLoadingProgress(100)
            webView.visibility = View.INVISIBLE
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
