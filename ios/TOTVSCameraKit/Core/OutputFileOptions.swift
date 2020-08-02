//
//  OutputFileOptions.swift
//  totvs-camera-core
//
//  Created by Jansel Rodriguez on 6/28/20.
//  Copyright © 2020 Jansel. All rights reserved.
//
import Foundation
/**
 * Output options for saving the captured images
 */
public struct OutputFileOptions {
    public static let NULL = OutputFileOptions()
    /** output file for a taken picture */
    public let outputDirectory: URL?
    
    public init(outputDirectory: URL? = nil) {
        self.outputDirectory = outputDirectory
    }
}
