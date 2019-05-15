package org.wheelmap.pwawrapper

class Constants {
    companion object {

        // Root page
        const val WEBAPP_URL = "https://wheelmap.org/"
        const val WEBAPP_HOST = "wheelmap.org" // used for checking Intent-URLs

        // User Agent tweaks
        const val POSTFIX_USER_AGENT = true // set to true to append USER_AGENT_POSTFIX to user agent
        const val OVERRIDE_USER_AGENT = false // set to true to use USER_AGENT instead of default one
        const val USER_AGENT_POSTFIX = "AndroidApp" // useful for identifying traffic, e.g. in Google Analytics
        const val USER_AGENT = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Mobile Safari/537.36"

        // Constants
        // window transition duration in ms
        const val REVEAL_TRANSITION_DURATION = 1000

        // show your app when the page is loaded XX %.
        // lower it, if you've got server-side rendering (e.g. to 35),
        // bump it up to ~98 if you don't have SSR or a loading screen in your web app
        const val PROGRESS_THRESHOLD = 95

        // turn on/off mixed content (both https+http within one page) for API >= 21
        const val ENABLE_MIXED_CONTENT = true
    }
}
