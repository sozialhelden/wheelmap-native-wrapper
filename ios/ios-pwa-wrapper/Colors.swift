import UIKit

func fromHex(hex: String?) -> UIColor {
    let normalizedHexString: String = normalize(hex)
    var c: CUnsignedInt = 0
    Scanner(string: normalizedHexString).scanHexInt32(&c)
    return UIColor(red:UIColorMasks.redValue(c), green:UIColorMasks.greenValue(c), blue:UIColorMasks.blueValue(c), alpha:UIColorMasks.alphaValue(c))
}

private func normalize(_ hex: String?) -> String {
    guard var hexString = hex else {
        return "00000000"
    }
    if hexString.hasPrefix("#") {
        hexString = String(hexString.dropFirst())
    }
    if hexString.count == 3 || hexString.count == 4 {
        hexString = hexString.map { "\($0)\($0)" } .joined()
    }
    let hasAlpha = hexString.count > 7
    if !hasAlpha {
        hexString += "ff"
    }
    return hexString
}

private enum UIColorMasks: CUnsignedInt {
    case redMask    = 0xff000000
    case greenMask  = 0x00ff0000
    case blueMask   = 0x0000ff00
    case alphaMask  = 0x000000ff
    
    static func redValue(_ value: CUnsignedInt) -> CGFloat {
        return CGFloat((value & redMask.rawValue) >> 24) / 255.0
    }
    
    static func greenValue(_ value: CUnsignedInt) -> CGFloat {
        return CGFloat((value & greenMask.rawValue) >> 16) / 255.0
    }
    
    static func blueValue(_ value: CUnsignedInt) -> CGFloat {
        return CGFloat((value & blueMask.rawValue) >> 8) / 255.0
    }
    
    static func alphaValue(_ value: CUnsignedInt) -> CGFloat {
        return CGFloat(value & alphaMask.rawValue) / 255.0
    }
}

extension UIColor {
    /**
     Create a ligher color
     */
    func lighter(by percentage: CGFloat = 30.0) -> UIColor {
        return self.adjustBrightness(by: abs(percentage))
    }
    
    /**
     Create a darker color
     */
    func darker(by percentage: CGFloat = 30.0) -> UIColor {
        return self.adjustBrightness(by: -abs(percentage))
    }
    
    /**
     Try to increase brightness or decrease saturation
     */
    func adjustBrightness(by percentage: CGFloat = 30.0) -> UIColor {
        var h: CGFloat = 0, s: CGFloat = 0, b: CGFloat = 0, a: CGFloat = 0
        if self.getHue(&h, saturation: &s, brightness: &b, alpha: &a) {
            if b < 1.0 {
                let newB: CGFloat = max(min(b + (percentage/100.0)*b, 1.0), 0.0)
                return UIColor(hue: h, saturation: s, brightness: newB, alpha: a)
            } else {
                let newS: CGFloat = min(max(s - (percentage/100.0)*s, 0.0), 1.0)
                return UIColor(hue: h, saturation: newS, brightness: b, alpha: a)
            }
        }
        return self
    }
}
