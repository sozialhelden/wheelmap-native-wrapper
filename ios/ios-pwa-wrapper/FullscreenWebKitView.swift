//
//  FullscreenWebKitView.swift
//  ios-pwa-wrapper
//
//  Created by Sebastian Felix Zappe on 19.04.21.
//  Copyright © 2021 Sozialhelden. All rights reserved.
//

import SwiftUI
import WebKit

class FullscreenWebKitView: WKWebView {
    override func willMove(toWindow newWindow: UIWindow?) {
        super.willMove(toWindow: newWindow)
        #if targetEnvironment(macCatalyst)
        if let titlebar = newWindow?.windowScene?.titlebar {
            titlebar.titleVisibility = .hidden
            titlebar.toolbar = nil
        }
        #endif
    }
}
