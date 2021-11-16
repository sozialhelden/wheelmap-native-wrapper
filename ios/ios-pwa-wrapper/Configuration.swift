import UIKit

let filepath = Bundle.main.path(forResource: "Configuration.plist", ofType: nil)
let config : [String:String] = filepath != nil ? ConfigLoader.load(plistPath: filepath!) : [:]

// Basic App-/WebView-configuration
let appTitle = config["PROJECT_APP_NAME", default: "Wheelmap"]
let webAppHost = config["PROJECT_HOST_NAME", default: "wheelmap.org"]
let primaryColor = config["PROJECT_PRIMARY_COLOR", default: "9DF359"]

let webAppUrl = URL(string: "https://\(webAppHost)")

let useUserAgentPostfix = true
let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String
let userAgentPostfix = "AllyApp/\(version ?? "Unknown") (iOS)"
let useCustomUserAgent = false
let customUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A356 Safari/604.1"

// UI Settings
let changeAppTitleToPageTitle = false
let forceLargeTitle = false

// Colors & Styles
let useLightStatusBarStyle = true

let navigationBarColor = fromHex(hex: primaryColor)

let offlineIconColor = UIColor.darkGray

let darkerColor = navigationBarColor.darker()

let progressBarColor = darkerColor
let navigationTitleColor = darkerColor
let navigationButtonColor = darkerColor
let buttonColor = darkerColor
let activityIndicatorColor = darkerColor
