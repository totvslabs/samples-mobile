//
//  GraphicOverlay.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/9/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import UIKit

public class GraphicOverlay : UIView {
    public func clear() {
        for child in subviews {
            child.removeFromSuperview()
        }
    }
    
    public func add(_ view: UIView) {
        addSubview(view)
    }
}
