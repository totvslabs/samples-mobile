//
//  FaceGraphic.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 8/1/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import UIKit
import TOTVSCameraKit

fileprivate let smallRadius: CGFloat = 6.0

public class FaceGraphic : VisionReceiver<FaceObject> {
    
    private weak var cameraView: CameraView? = nil
    
    // holder view
    public lazy var view: UIView = {
        let view = UIView()
        view.backgroundColor = .clear
        return view
    }()
    
    private var color: UIColor = .green {
        didSet {
            leftEye?.backgroundColor = color
            rightEye?.backgroundColor = color
            nose?.backgroundColor = color
        }
    }
    
    private var leftEye: UIView? = nil
    private var rightEye: UIView? = nil
    private var nose: UIView? = nil
    
    public var drawEyes: Bool = false
    public var drawNose: Bool = false
    
    public init(cameraView: CameraView) {
        self.cameraView = cameraView
    }
    
    public override func send(value: FaceObject) {
        guard value != NullFaceObject else {
            clear()
            return
        }
        adjustFaceBounds(for: value)
        adjustLandmarks(for: value)
    }
}

// MARK:  Camera Device Normalization
private extension FaceGraphic {
    // normalize points comming from camera device coordinate system.
    private func normalize(point: CGPoint, sourceSize: CGSize) -> CGPoint {
        // this step is required to create a unit point
        let normalized = CGPoint(x: point.x / sourceSize.width, y: point.y / sourceSize.height)
        return cameraView!.previewPoint(fromCaptureDevicePoint: normalized)
    }
    
    private func normalize(rect: CGRect, sourceSize: CGSize) -> CGRect {
        // this step is needed because we need a unit rectangle for the next step.
        let normalizedFrame = CGRect(
            x: rect.origin.x / sourceSize.width,
            y: rect.origin.y / sourceSize.height,
            width: rect.width / sourceSize.width,
            height: rect.height / sourceSize.height
        )
        return cameraView!
            .previewRect(fromCaptureDeviceRect: normalizedFrame)
            .standardized
    }
}

// MARK: - Face Bounding & Landmarks
private extension FaceGraphic {
    
    private func adjustFaceBounds(for face: FaceObject) {
        view.frame = cameraView!.graphicOverlay.frame
    }
    
    private func adjustLandmarks(for face: FaceObject) {
        // face[.leftEye] works.
        for l in face.landmarks {
            let center = normalize(point: l.position, sourceSize: face.sourceSize)
            var view: UIView? = nil
                    
            if drawEyes && (l.name == .leftEye || l.name == .rightEye) {
                view = l.name == .leftEye ? addLeftEye(at: center) : addRightEye(at: center)
            }
            if drawNose && l.name == .nose {
                view = addNose(at: center)
            }
            if nil != view {
                UIView.animate(withDuration: 0.185) {
                    view!.center = center
                }
            }
        }
    }
}
// MARK: - Face Eyes Views
private extension FaceGraphic {
    func addLeftEye(at point: CGPoint) -> UIView {
        // if is first request, create the eye and add it
        if nil == leftEye {
            leftEye = addCircle(to: view, at: point, color: color, radius: smallRadius)
        }
        // otherwise re-attach it to the parent
        if nil == leftEye?.superview {
            view.addSubview(leftEye!)
        }
        return leftEye!
    }
    func addRightEye(at point: CGPoint) -> UIView {
        // if is first request, create the eye and add it
        if nil == rightEye {
            rightEye = addCircle(to: view, at: point, color: color, radius: smallRadius)
        }
        // otherwise re-attach it to the parent
        if nil == rightEye?.superview {
            view.addSubview(rightEye!)
        }
        return rightEye!
    }
}

// MARK: - Face Nose Views
private extension FaceGraphic {
    func addNose(at point: CGPoint) -> UIView {
        // if is first request, create the nose and add it
        if nil == nose {
            nose = addCircle(to: view, at: point, color: color, radius: smallRadius)
        }
        // otherwise re-attach it to the parent
        if nil == nose?.superview {
            view.addSubview(nose!)
        }
        return nose!
    }
}

// MARK: - Face Views Clean
public extension FaceGraphic {
    func clear() {
        for child in view.subviews {
            child.removeFromSuperview()
        }
    }
}

/// MARK: Landmark Decoration
public extension FaceGraphic {
    func setLandmarksColor(rgb: String) {
        guard let color = UIColor(rgba: rgb) else {
            return
        }
        self.color = color
    }
}

// MARK: - UI Utility
private extension FaceGraphic {
    func addCircle(to view: UIView, at point: CGPoint, color: UIColor, radius: CGFloat) -> UIView {
        let divisor: CGFloat = 2.0
        let xCoord = point.x - radius / divisor
        let yCoord = point.y - radius / divisor
        let circleView = UIView(frame: CGRect(x: xCoord, y: yCoord, width: radius, height: radius))
        circleView.layer.cornerRadius = radius / divisor
        circleView.alpha = 0.7
        circleView.backgroundColor = color
        view.addSubview(circleView)
        return circleView
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
