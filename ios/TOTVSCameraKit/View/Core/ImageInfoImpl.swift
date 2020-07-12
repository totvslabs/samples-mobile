//
//  ImageProxyImpl.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/9/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import UIKit

/**
 * Concrete implementation of [ImageInfo]
 */
struct ImageInfoImpl : ImageInfo {
    let timestamp: Int64
    let orientation: UIDeviceOrientation
    var sourceFacing: CameraFacing
}
