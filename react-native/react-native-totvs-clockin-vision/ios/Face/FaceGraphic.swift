//
//  FaceGraphic.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 8/1/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import UIKit
import TOTVSCameraKit

public class FaceGraphic : VisionReceiver<FaceObject> {
    
    private weak var cameraView: CameraView? = nil
    
    public lazy var view: UIView = {
        let view = UIView()
        view.layer.cornerRadius = 10.0
        view.alpha = 0.3
        view.backgroundColor = .green
        return view
    }()
    
    public var drawEyes: Bool = false
    public var drawNose: Bool = false
    
    public init(cameraView: CameraView) {
        self.cameraView = cameraView
    }
    
    public override func send(value: FaceObject) {
        guard value != NullFaceObject else {
            removeView()
            return
        }
        if view.superview == nil {
            addView()
        }
        
        // this step is needed because we need a unit rectangle for the next step.
        let normalizedFrame = CGRect(
            x: value.boundingBox.origin.x / value.sourceSize.width,
            y: value.boundingBox.origin.y / value.sourceSize.height,
            width: value.boundingBox.width / value.sourceSize.width,
            height: value.boundingBox.height / value.sourceSize.height
        )
        let standardizedRect = cameraView?.previewRect(
            fromCaptureDeviceRect: normalizedFrame
        ).standardized
        
        UIView.animate(withDuration: 0.2) {
            self.view.frame = standardizedRect!
        }
    }
}

private extension FaceGraphic {
    private func addView() {
        cameraView?.addOverlayGraphic(view)
    }
    
    private func removeView() {
        view.removeFromSuperview()
    }
}

public extension FaceGraphic {
    func clear() {
        for child in view.subviews {
            child.removeFromSuperview()
        }
        removeView()
    }
}

/// MARK: Landmark Decoration
public extension FaceGraphic {
    func setLandmarksColor(rgb: String) {
        guard let color = UIColor(rgba: rgb) else {
            return
        }
        view.backgroundColor = color
    }
}


/// MARK: UIColor + RGBA
fileprivate extension UIColor {
    convenience init?(rgba color: String) {
        let r, g, b, a: CGFloat
        var rgba = color
        
        if color.count == 7 { // of #FFFFFF
            rgba = "\(rgba)FF"
        }
        
        if rgba.hasPrefix("#") {
            let start = rgba.index(rgba.startIndex, offsetBy: 1)
            let hexColor = String(rgba[start...])
            
            if hexColor.count == 8 {
                let scanner = Scanner(string: hexColor)
                var hexNumber: UInt64 = 0

                if scanner.scanHexInt64(&hexNumber) {
                    r = CGFloat((hexNumber & 0xff000000) >> 24) / 255
                    g = CGFloat((hexNumber & 0x00ff0000) >> 16) / 255
                    b = CGFloat((hexNumber & 0x0000ff00) >> 8) / 255
                    a = CGFloat(hexNumber & 0x000000ff) / 255

                    self.init(red: r, green: g, blue: b, alpha: a)
                    return
                }
            }
        }

        return nil
    }
}
