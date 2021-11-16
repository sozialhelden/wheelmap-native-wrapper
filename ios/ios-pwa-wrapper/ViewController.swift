import UIKit
import WebKit

class ViewController: UIViewController {
    
    // MARK: Outlets
    @IBOutlet weak var webViewContainer: UIView!
    @IBOutlet weak var offlineView: UIView!
    @IBOutlet weak var offlineIcon: UIImageView!
    @IBOutlet weak var offlineButton: UIButton!
    @IBOutlet weak var activityIndicatorView: UIView!
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    
    // MARK: Globals
    var webView: WKWebView!
    var progressBar : UIProgressView!
    var contentController: WKUserContentController!

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        self.title = appTitle
        setupApp()
    }
    
    // Initialize App and start loading
    func setupApp() {
        initAppFocus()
        setupWebView()
        locationInit()
        setupUI()
        loadAppUrl()
    }
     
    // Cleanup
    deinit {
        webView.removeObserver(self, forKeyPath: #keyPath(WKWebView.isLoading))
        webView.removeObserver(self, forKeyPath: #keyPath(WKWebView.estimatedProgress))
        NotificationCenter.default.removeObserver(self, name: UIDevice.orientationDidChangeNotification, object: nil)
    }
    
    // reload page from offline screen
    @IBAction func onOfflineButtonClick(_ sender: Any) {
        offlineView.isHidden = true
        webViewContainer.isHidden = false
        loadAppUrl()
    }
    
    // Observers for updating UI
    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        if (keyPath == #keyPath(WKWebView.estimatedProgress)) {
            progressBar.progress = Float(webView.estimatedProgress)
        }
    }
    
    // Initialize WKWebView
    func setupWebView() {
        // set up webview
        let config = WKWebViewConfiguration()
        contentController = WKUserContentController()
        config.userContentController = contentController
        config.preferences.javaScriptEnabled = true
        if #available(iOS 14.0, *) {
            config.limitsNavigationsToAppBoundDomains = true
        }

        webView = FullscreenWebKitView(frame: webViewContainer.frame, configuration: config)
        webView.navigationDelegate = self
        webView.uiDelegate = self
        webView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        
        
        if #available(iOS 11.0, *) {
            #if targetEnvironment(macCatalyst)
            self.additionalSafeAreaInsets = UIEdgeInsets.init(top: 32 * 4, left: 0, bottom: 0, right: 0)
            #else
            self.additionalSafeAreaInsets = UIEdgeInsets.init(top: 0, left: 0, bottom: 0, right: 0)
            #endif
            webView.insetsLayoutMarginsFromSafeArea = true
            webView.scrollView.insetsLayoutMarginsFromSafeArea = true
            webView.scrollView.contentInsetAdjustmentBehavior = .never
        }
        
        if #available(iOS 10.0, *) {
            webView.configuration.ignoresViewportScaleLimits = false
        }
        
        // user agent
        if #available(iOS 9.0, *) {
            if (useCustomUserAgent) {
                webView.customUserAgent = customUserAgent
            }
            if (useUserAgentPostfix) {
                if (useCustomUserAgent) {
                    webView.customUserAgent = customUserAgent + " " + userAgentPostfix
                } else {
                    webView.evaluateJavaScript("navigator.userAgent", completionHandler: { (result, error) in
                        if let resultObject = result {
                            self.webView.customUserAgent = (String(describing: resultObject) + " " + userAgentPostfix)
                        }
                    })
                }
            }
            webView.configuration.applicationNameForUserAgent = ""
        }
        
        // bounces
        webView.scrollView.bounces = false
        webView.allowsBackForwardNavigationGestures = false
        
        // init observers
        webView.addObserver(self, forKeyPath: #keyPath(WKWebView.isLoading), options: NSKeyValueObservingOptions.new, context: nil)
        webView.addObserver(self, forKeyPath: #keyPath(WKWebView.estimatedProgress), options: NSKeyValueObservingOptions.new, context: nil)
        
        webViewContainer.addSubview(webView)
    }

    // Initialize UI elements
    // call after WebView has been initialized
    func setupUI() {
        // progress bar
        progressBar = UIProgressView(frame: CGRect(x: 0, y: 0, width: webViewContainer.frame.width, height: 40))
        progressBar.autoresizingMask = [.flexibleWidth]
        progressBar.progress = 0.0
        progressBar.tintColor = progressBarColor
        webView.addSubview(progressBar)
        
        // activity indicator
        activityIndicator.color = activityIndicatorColor
        activityIndicator.startAnimating()
        
        // offline container
        offlineIcon.tintColor = offlineIconColor
        offlineButton.tintColor = buttonColor
        offlineView.isHidden = true
        
        // setup navigation bar
        if (forceLargeTitle) {
            if #available(iOS 11.0, *) {
                navigationItem.largeTitleDisplayMode = UINavigationItem.LargeTitleDisplayMode.always
            }
        }
        
        if (useLightStatusBarStyle) {
            self.navigationController?.navigationBar.barStyle = UIBarStyle.black
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
}

extension ViewController {
    // custom data for this extension
    private struct FocusData {
        static var lastFocusLost: Date = Date.init()
        static let reloadDuration = 60.0
    }
    
    func initAppFocus() {
        let notificationCenter = NotificationCenter.default
        
        notificationCenter.addObserver(
            self,
            selector: #selector(appMovedToForeground),
            name: UIApplication.willEnterForegroundNotification,
            object: nil)
        notificationCenter.addObserver(
            self,
            selector: #selector(appMovedToBackground),
            name: UIApplication.didEnterBackgroundNotification,
            object: nil)
    }
    
    @objc func appMovedToBackground() {
        print("App moved to Background!")
        FocusData.lastFocusLost = Date.init()
    }
    
    @objc func appMovedToForeground() {
        let timePassed = abs(FocusData.lastFocusLost.timeIntervalSinceNow)
        
        print("App moved to Foreground! \(timePassed)")
        
        if (timePassed > FocusData.reloadDuration) {
            reloadCurrentUrl()
        }
    }
}


// WebView Event Listeners
extension ViewController: WKNavigationDelegate {
    // custom data for this extension
    private struct WebViewData {
        static var loadTimer: Timer?
        static let timeoutDuration = 15.0
    }
    
    // didFinish
    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        // abort timeout
        WebViewData.loadTimer?.invalidate()
        WebViewData.loadTimer = nil
        
        // set title
        if (changeAppTitleToPageTitle) {
            navigationItem.title = webView.title
        }
        // hide progress bar after initial load
        progressBar.isHidden = true
        // hide activity indicator
        activityIndicatorView.isHidden = true
        activityIndicator.stopAnimating()
    }
    
    // didFailProvisionalNavigation
    // == we are offline / page not available
    func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
        print("didFailProvisionalNavigation: \(error)")
        // abort timeout
        WebViewData.loadTimer?.invalidate()
        WebViewData.loadTimer = nil
        
        // show offline screen
        offlineView.isHidden = false
        webViewContainer.isHidden = true
        
        // hide activity indicator
        activityIndicatorView.isHidden = true
        activityIndicator.stopAnimating()
        progressBar.isHidden = true
    }
    
    // timeout
    // == we are offline / page not available
    @objc func timeout() {
        WebViewData.loadTimer = nil
        
        // show offline screen
        offlineView.isHidden = false
        webViewContainer.isHidden = true

        // hide activity indicator
        activityIndicatorView.isHidden = true
        activityIndicator.stopAnimating()
        progressBar.isHidden = true
    }
    
    func reloadCurrentUrl() {
        loadAppUrl(url: (webView.url != nil ? webView.url : webAppUrl))
    }
    
    // load startpage
    func loadAppUrl(url: URL? = webAppUrl) {
        progressBar.isHidden = false
        activityIndicatorView.isHidden = false
        activityIndicator.startAnimating()
        
        WebViewData.loadTimer = Timer.scheduledTimer(
            timeInterval: WebViewData.timeoutDuration,
            target: self,
            selector: #selector(timeout),
            userInfo: nil,
            repeats: false)

        let urlRequest = URLRequest(url: url!)
        webView.load(urlRequest)
    }
}

// WebView additional handlers
extension ViewController: WKUIDelegate {
    // handle links opening in new tabs
    func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration, for navigationAction: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
        if (navigationAction.targetFrame == nil) {
            webView.load(navigationAction.request)
        }
        return nil
    }
    
    // restrict navigation to target host, open external links in 3rd party apps
    func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        print("decidePolicyFor: \(navigationAction)")
        
        if var requestUrl = navigationAction.request.url {
            var openInExternalApp = false
            if ["mailto", "twitter", "fb"].contains(requestUrl.scheme) {
                openInExternalApp = true
            }
            
            if let requestHost = requestUrl.host {
                // requestHost.range(of: webAppHost) != nil would allow links like news.wheelmap.org for wheelmap.org
                if (requestHost == webAppHost || requestHost == "localhost") {
                    decisionHandler(.allow)
                } else {
                    openInExternalApp = true;
                    decisionHandler(.cancel)
                }
            } else {
                decisionHandler(.cancel)
            }
            
            if openInExternalApp {
                // redirect to settings on mobile app
                if (requestUrl.absoluteString == "https://support.apple.com/en-us/ht203033") {
                    requestUrl = URL(string: UIApplication.openSettingsURLString)!
                }
                
                if (UIApplication.shared.canOpenURL(requestUrl)) {
                    if #available(iOS 10.0, *) {
                        UIApplication.shared.open(requestUrl)
                    } else {
                        // Fallback on earlier versions
                        UIApplication.shared.openURL(requestUrl)
                    }
                } else {
                    print("Error: Failed opening in external app!: \(requestUrl)")
                }
            }
        }
    }
}

import CoreLocation

extension ViewController: CLLocationManagerDelegate, WKScriptMessageHandler {
    
    private struct LocationData {
        static var locationManger: CLLocationManager!
        static var currentLocation: CLLocation!
    }
    
    func locationInit() {
        LocationData.locationManger = CLLocationManager()
        LocationData.locationManger.delegate = self
        LocationData.locationManger.desiredAccuracy = kCLLocationAccuracyBest
        LocationData.locationManger.distanceFilter = kCLDistanceFilterNone
        
        contentController.add(self, name: "locationHandler")
        let fileName = "InjectLocation.js"
        if let filepath = Bundle.main.path(forResource: fileName, ofType: nil) {
            do {
                let contents = try String(contentsOfFile: filepath)
                
                // print(contents)
                let userScript = WKUserScript(source: contents, injectionTime: WKUserScriptInjectionTime.atDocumentStart, forMainFrameOnly: true)
                contentController.addUserScript(userScript)
            } catch {
                print("Failed loading \(fileName)")
            }
        } else {
            print("Could not find \(fileName)")
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        // print("didChangeAuthorization: \(String(describing: status))")
        
        if status == .authorizedWhenInUse {
            LocationData.locationManger.startUpdatingLocation()
        }
        
        if status == .denied {
            webView.evaluateJavaScript("window._nativePositionDenied();")
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        // print("didFailWithError: \(error)")
        webView.evaluateJavaScript("window._nativePositionDenied();")
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        // print("didUpdateToLocation: \(String(describing: locations.last))")
        let lastLoc = locations.last;
        LocationData.currentLocation = lastLoc
        
        if (lastLoc != nil) {
            let timestamp = Int64(lastLoc!.timestamp.timeIntervalSince1970 * 1000)
            let coord = lastLoc!.coordinate
            let altitude = lastLoc!.altitude
            webView.evaluateJavaScript("window._nativePositionChanged({coords: { latitude: \(coord.latitude), longitude: \(coord.longitude), altitude: \(altitude), accuracy : \(lastLoc!.horizontalAccuracy), altitudeAccuracy: \(lastLoc!.verticalAccuracy), heading:  \(lastLoc!.course), speed: \(lastLoc!.speed) }, timestamp: \(timestamp) })")
        }
    }
    
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        if (message.name == "locationHandler"){
            
            let payload = "\(message.body)";
            
            // print("userContentController: \(payload)")
            
            if (payload == "watchPosition") {
                if LocationData.locationManger.responds(to: #selector(CLLocationManager.requestWhenInUseAuthorization)) {
                    LocationData.locationManger.requestWhenInUseAuthorization()
                }
                
                if CLLocationManager.locationServicesEnabled() {
                    LocationData.locationManger.startUpdatingLocation()
                }
            }
            
            if (payload == "stopPosition") {
                LocationData.locationManger.stopUpdatingLocation()
            }
        }
    }
}
