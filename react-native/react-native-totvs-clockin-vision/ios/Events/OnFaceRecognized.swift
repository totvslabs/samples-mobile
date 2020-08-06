//
//  OnFaceRecognized.swift
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 8/3/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation


fileprivate let FIELD_PERSON_ID = "personid"
fileprivate let FIELD_PERSON_NAME = "name"
fileprivate let FIELD_CONFIDENCE = "confidence"
fileprivate let FIELD_IMAGE_ENCODING = "imageEncoding"
fileprivate let FIELD_IMAGE_PATH = "imageFilePath"
fileprivate let FIELD_RESULTS = "results"

/**
* Event emitted when face recognition is triggered.
*/
public class OnFaceRecognized : Event {
    
    private let emit: RCTDirectEventBlock?
    
    public required init(emit: RCTDirectEventBlock?) {
        self.emit = emit
    }
    
    public func send(data: RecognitionResult) {
        var persons = [[String: Any]]()
        for face in data.faces {
            persons.append([
                FIELD_PERSON_ID: face.personId,
                FIELD_PERSON_NAME: face.name,
                FIELD_CONFIDENCE: face.distance,
                FIELD_IMAGE_ENCODING: face.encoding
            ])
        }
        var event: [String: Any] = [
            FIELD_RESULTS: persons
        ]
        if nil != data.imagePath {
            event[FIELD_IMAGE_PATH] = data.imagePath!.path
        }
        emit?(event)
    }
}
