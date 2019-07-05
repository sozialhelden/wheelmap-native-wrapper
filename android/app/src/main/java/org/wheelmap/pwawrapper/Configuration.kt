package org.wheelmap.pwawrapper

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

class Configuration {

    companion object {
        private const val DEFAULT_WEBAPP_HOST = "wheelmap.org"

        // User Agent tweaks
        const val POSTFIX_USER_AGENT = true // set to true to append USER_AGENT_POSTFIX to user agent
        const val OVERRIDE_USER_AGENT = false // set to true to use USER_AGENT instead of default one
        const val USER_AGENT_POSTFIX = "AndroidApp" // useful for identifying traffic, e.g. in Google Analytics
        const val USER_AGENT = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Mobile Safari/537.36"

        // Configuration

        // show your app when the page is loaded XX %.
        // lower it, if you've got server-side rendering (e.g. to 35),
        // bump it up to ~98 if you don't have SSR or a loading screen in your web app
        const val PROGRESS_THRESHOLD = 80

        const val TIMEOUT_DURATION = 5000L

        // turn on/off mixed content (both https+http within one page) for API >= 21
        const val ENABLE_MIXED_CONTENT = true

        var baseHost = DEFAULT_WEBAPP_HOST
        var baseUrl = "https://$baseHost"

        fun init(context: Context) {
            baseHost = getMetaData(context, "PROJECT_HOST_NAME") ?: baseHost

            if (BuildConfig.PROJECT_HOST_NAME.isNotBlank() && BuildConfig.PROJECT_HOST_NAME != "null") {
                baseHost = BuildConfig.PROJECT_HOST_NAME
            }

            baseUrl =  "https://$baseHost"
        }

        private fun getMetaData( context: Context, name: String): String? {
            try {
                val ai = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                val bundle = ai.metaData
                return bundle.getString(name)
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e("Configuration", "Unable to load meta-data: " + e.message)
            }

            return null
        }
    }
}
