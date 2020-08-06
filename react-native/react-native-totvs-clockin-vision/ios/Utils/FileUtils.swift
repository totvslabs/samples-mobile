//
//  FileUtils.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/27/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation

fileprivate let DEFAULT_MODEL_OUTPUT_DIR_NAME = "carol_offline_face_recognition"
fileprivate let CAPTURES_OUTPUT_DIR_NAME = "images"
fileprivate let PENDING_EMPLOYEES_OUTPUT_DIR_NAME = "pendingEmployeeImages"
fileprivate let MODEL_SHAPE_NAME = "shape_predictor_5_face_landmarks"
fileprivate let MODEL_SHAPE_FILE_NAME = "shape_predictor_5_face_landmarks.dat"
fileprivate let MODEL_DESCRIPTOR_NAME = "dlib_face_recognition_resnet_model_v1"
fileprivate let MODEL_DESCRIPTOR_FILE_NAME = "dlib_face_recognition_resnet_model_v1.dat"
fileprivate let NO_MEDIA = ".nomedia"

/**
* Utility class to save transient state through utility changes
*/
fileprivate class TransientState {
    // the requested and desired model output directory name
    var modelOutputDirName: String? = nil
}

/**
* object representing the state if the lib constants
*/
fileprivate let transientState = TransientState()


func getFileBase64(atPath path: String) -> String? {
    if FileManager.default.fileExists(atPath: path) {
        let url = URL(fileURLWithPath: path)
        return url.dataRepresentation.base64EncodedString()
    }
    return nil
}

func deleteFile(atPath path: String) -> Bool {
    if FileManager.default.fileExists(atPath: path) {
        do {
            try FileManager.default.removeItem(atPath: path)
        } catch let error {
            print("deleteFile: error deleting file \(path): \(error)")
            return false
        }
        return true
    }
    return false
}

/**
* Set the model output directory name
*/
func setModelDirName(name: String) {
    transientState.modelOutputDirName = name
}

/**
* Returns the model output directory path
*/
func getModelOutputDir() -> String {
    return getModelOutputDirectoryURL().path
}

/**
* Returns the model output directory path convenientely as URL object.
*/
fileprivate func getModelOutputDirectoryURL() -> URL {
    var dirName: String
    if (nil == transientState.modelOutputDirName || transientState.modelOutputDirName!.isEmpty) {
        dirName = DEFAULT_MODEL_OUTPUT_DIR_NAME
    } else {
        dirName = transientState.modelOutputDirName!
    }
    return getDocumentsDirectory()
        .appendingPathComponent(dirName)
}

/**
* Returns the .no_media directory path.
*/
fileprivate func getNoMediaDirectoryURL() -> URL {
    return getModelOutputDirectoryURL()
        .appendingPathComponent(NO_MEDIA)
}

/**
* Returns the captures output directory path
*/
func getCapturesDirectoryURL() -> URL {
    return getModelOutputDirectoryURL()
        .appendingPathComponent(CAPTURES_OUTPUT_DIR_NAME)
}

/**
* Returns the pending employees images output directory path
*/
fileprivate func getPendingEmployeesDirectoryURL() -> URL {
    return getModelOutputDirectoryURL()
        .appendingPathComponent(PENDING_EMPLOYEES_OUTPUT_DIR_NAME)
}

/**
* Returns the model shape output directory
*/
fileprivate func getModelShapesFileURL() -> URL {
    return getModelOutputDirectoryURL()
        .appendingPathComponent(MODEL_SHAPE_FILE_NAME)
}

/**
* Returns the model descriptors output directory
*/
fileprivate func getModelDescriptorsURL() -> URL {
    return getModelOutputDirectoryURL()
        .appendingPathComponent(MODEL_DESCRIPTOR_FILE_NAME)
}

/**
* Setup the model output directories
*/
func prepareModelDirectories(onDone: @escaping (Bool) -> Void) {
    DispatchQueue.global().async {
        
        let model = getModelOutputDirectoryURL()
        let media = getNoMediaDirectoryURL()
        let images = getCapturesDirectoryURL()
        let pending = getPendingEmployeesDirectoryURL()
        let shapes = getModelShapesFileURL()
        let descriptor = getModelDescriptorsURL()

        let directories = [model, media, images, pending]
        do {
            try directories.forEach { d in
                try FileManager.default.createDirectory(at: d, withIntermediateDirectories: true, attributes: nil)
            }
            let (shapesResource, descriptorResource) = getModelResourceURLs()

            if !FileManager.default.fileExists(atPath: shapes.path) {
                try! FileManager.default.copyItem(at: shapesResource, to: shapes)
            }
            if !FileManager.default.fileExists(atPath: descriptor.path) {
                try! FileManager.default.copyItem(at: descriptorResource, to: descriptor)
            }

            DELETE_ME_AND_ALL_FILE_NAMED_HERE_AFTER_USING_ME___THIS_IS_FOR_TEST_ONLY()
        } catch let error {
            print("prepareModelDirectories: error configuring model directories: \(error)")
            return onDone(false)
        }
        onDone(true)
    }
}

fileprivate func DELETE_ME_AND_ALL_FILE_NAMED_HERE_AFTER_USING_ME___THIS_IS_FOR_TEST_ONLY() {
    // this function is used only for test purpose of copying the model descriptors file
    // to test the recognition model. it must be deleted after test.
    
    func getResourcesURLs() -> (deploy: URL, descJittered: URL, namesJiterred: URL, caffeModel: URL) {
        let frameworkBundle = Bundle(for: VisionModule.self)
        let moduleBundleURL = frameworkBundle.resourceURL?.appendingPathComponent("ClockInVision.bundle")
        
        guard let bundle = Bundle(url: moduleBundleURL!) else {
            fatalError(
                "ClockInVision.bundle not found, please check the file exists on react-native-totvs-clockin-vision package"
            )
        }
        let deploy = bundle.url(forResource: "deploy_prototxt", withExtension: "txt")!
        let descJittered = bundle.url(forResource: "face_descriptors_jittered", withExtension: "dat")!
        let namesJiterred = bundle.url(forResource: "face_names_jittered", withExtension: "dat")!
        let caffeModel = bundle.url(forResource: "res10_300x300_ssd_iter_140000", withExtension: "caffemodel")!
        return (deploy, descJittered, namesJiterred, caffeModel)
    }
    
    let (deploy, descJittered, namesJiterred, caffeModel) = getResourcesURLs()
    
    let deployDest = getModelOutputDirectoryURL().appendingPathComponent("deploy_prototxt.txt")
    let descJitteredDest = getModelOutputDirectoryURL().appendingPathComponent("face_descriptors_jittered.dat")
    let namesJiterredDest = getModelOutputDirectoryURL().appendingPathComponent("face_names_jittered.dat")
    let caffeModelDest = getModelOutputDirectoryURL().appendingPathComponent("res10_300x300_ssd_iter_140000.caffemodel")
        
    if !FileManager.default.fileExists(atPath: deployDest.path) {
        try! FileManager.default.copyItem(at: deploy, to: deployDest)
    }
    if !FileManager.default.fileExists(atPath: descJitteredDest.path) {
        try! FileManager.default.copyItem(at: descJittered, to: descJitteredDest)
    }
    if !FileManager.default.fileExists(atPath: namesJiterredDest.path) {
        try! FileManager.default.copyItem(at: namesJiterred, to: namesJiterredDest)
    }
    if !FileManager.default.fileExists(atPath: caffeModelDest.path) {
        try! FileManager.default.copyItem(at: caffeModel, to: caffeModelDest)
    }
}

/**
 Get all posible user app-specific document directories and returns the first one.
 */
fileprivate func getDocumentsDirectory() -> URL {
    let path = try! FileManager.default.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
    return path
}

/**
 Returns the local model resources required for the face recognition model
 */
fileprivate func getModelResourceURLs() -> (shapesResource: URL, descriptorResource: URL) {
    let frameworkBundle = Bundle(for: VisionModule.self)
    let moduleBundleURL = frameworkBundle.resourceURL?.appendingPathComponent("ClockInVision.bundle")
    
    guard let bundle = Bundle(url: moduleBundleURL!) else {
        fatalError(
            "ClockInVision.bundle not found, please check the file exists on react-native-totvs-clockin-vision package"
        )
    }
    guard let shapes = bundle.url(forResource: MODEL_SHAPE_NAME, withExtension: "dat") else {
        fatalError(
            "\(MODEL_SHAPE_NAME) not found, please check the file exists on react-native-totvs-clockin-vision package"
        )
    }
    guard let descriptor = bundle.url(forResource: MODEL_DESCRIPTOR_NAME, withExtension: "dat") else {
        fatalError(
            "\(MODEL_SHAPE_NAME) not found, please check the file exists on react-native-totvs-clockin-vision package"
        )
    }
    return (shapes, descriptor)
}
