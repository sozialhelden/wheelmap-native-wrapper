package org.wheelmap.pwawrapper

import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat

class EdgeToEdgeHandler(private val activity: AppCompatActivity) {

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM) // Android 15
    fun adjustLayout(){
        val rootView = activity.findViewById<RelativeLayout>(R.id.coordinatorLayout)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, windowInsets ->
            // Handle Status Bar Background
            val statusBarHeight = windowInsets.systemWindowInsetTop
            if (statusBarHeight > 0) {
                val statusBarBackgroundView = View(activity).apply {
                    setBackgroundColor(ContextCompat.getColor(activity, R.color.primary))
                }
                rootView.addView(
                    statusBarBackgroundView,
                    RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        statusBarHeight
                    ).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_TOP)
                    }
                )
            }

            // Handle Bottom Bar Background
            val bottomBarHeight = windowInsets.systemWindowInsetBottom
            if (bottomBarHeight > 0) {
                val bottomBarBackgroundView = View(activity).apply {
                    setBackgroundColor(Color.BLACK)
                }
                rootView.addView(
                    bottomBarBackgroundView,
                    RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        bottomBarHeight
                    ).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    }
                )
            }
            windowInsets
        }

        applyMarginsToView(R.id.offlineContainer)
        applyMarginsToView(R.id.loadingContainer)
        applyMarginsToView(R.id.webView)
        applyMarginsToView(R.id.progressBarBottom)
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun applyMarginsToView(viewId: Int) {
        val view = activity.findViewById<View>(viewId)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val layoutParams = view.layoutParams as RelativeLayout.LayoutParams
            layoutParams.setMargins(0, windowInsets.systemWindowInsetTop, 0, windowInsets.systemWindowInsetBottom)
            view.layoutParams = layoutParams
            windowInsets
        }
    }
}
