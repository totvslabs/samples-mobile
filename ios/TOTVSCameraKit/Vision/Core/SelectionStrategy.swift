//
//  SelectionStrategy.swift
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/10/20.
//  Copyright © 2020 Jansel. All rights reserved.
//

import Foundation

public protocol SelectionStrategy {
    associatedtype T
    
    func select(from: Array<T>) -> T?
}
