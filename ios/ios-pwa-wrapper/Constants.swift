import UIKit

// Basic App-/WebView-configuration
let appTitle = "iOS PWA Wrapper"

let webAppHost = "wheelmap.org"
let webAppUrl = URL(string: "https://\(webAppHost)")

let useUserAgentPostfix = true
let userAgentPostfix = "iOSApp"
let useCustomUserAgent = false
let customUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0_1 like Mac OS X) AppleWebKit/604.2.10 (KHTML, like Gecko) Mobile/15A8401"

// UI Settings
let changeAppTitleToPageTitle = false
let forceLargeTitle = false

// Colors & Styles
let useLightStatusBarStyle = true
let navigationBarColor = getColorFromHex(hex: 0xF44336, alpha: 1.0)
let navigationTitleColor = getColorFromHex(hex: 0x000000, alpha: 1.0)
let navigationButtonColor = navigationTitleColor
let progressBarColor = getColorFromHex(hex: 0x4CAF50, alpha: 1.0)
let offlineIconColor = UIColor.darkGray
let buttonColor = navigationBarColor
let activityIndicatorColor = navigationBarColor

// Color Helper function
func getColorFromHex(hex: UInt, alpha: CGFloat) -> UIColor {
    return UIColor(
        red: CGFloat((hex & 0xFF0000) >> 16) / 255.0,
        green: CGFloat((hex & 0x00FF00) >> 8) / 255.0,
        blue: CGFloat(hex & 0x0000FF) / 255.0,
        alpha: CGFloat(alpha)
    )
}
