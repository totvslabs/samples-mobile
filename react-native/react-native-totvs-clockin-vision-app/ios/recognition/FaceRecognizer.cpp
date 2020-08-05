#include <math.h>
#include <sstream>
#include <string>

#include <dlib/base64.h>
#include <dlib/compress_stream.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_io.h>
#include <dlib/opencv/cv_image.h>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/opencv.hpp>

#include "FaceRecognizer.h"
#include "FaceUtils.h"

string SHAPE_PREDICTOR_FN = "shape_predictor_5_face_landmarks.dat";
string DLIB_FACEREC_FN = "dlib_face_recognition_resnet_model_v1.dat";
string CV_FACEDETECTOR_FN = "res10_300x300_ssd_iter_140000.caffeemodel";
string CV_FACEDETECTOR_PROTO_FN = "deploy_prototxt.txt";

class FaceNotDetectedException {};
class MultipleFacesDetectedException {};

// public
FaceRecognizer::FaceRecognizer(string resources_path)
{
    loadModels(resources_path);
    embeddings_manager = EmbeddingsManager();
}

string FaceRecognizer::faceRecognition(string image_str)
{
    string json_payload;
    cv::Mat image = FaceUtils::decodeImage(image_str);
    cv::Mat resized_image = FaceUtils::adjustSize(image, 600);
    cv_image_t cv_image(resized_image);
    try
    {
        dlib::rectangle face_coords = detectFace(cv_image);
        dlib::full_object_detection shape = predictShape(cv_image, face_coords);
        auto embedding = computeFaceEmbedding(cv_image, shape);
        auto recognized_employees = embeddings_manager.search(embedding);
        return jsonify(recognized_employees);
    }
    catch (FaceNotDetectedException& e)
    {
        return string("{\"status\": \"FaceNotDetected\"}");
    }
    catch (MultipleFacesDetectedException& e)
    {
        return string("{\"status\": \"MultipleFacesDetected\"}");
    };
}

void FaceRecognizer::loadEmbeddings(string embeddings_path)
{
    embeddings_manager = EmbeddingsManager(embeddings_path);
}

void FaceRecognizer::updateThreshold(float threshold)
{
    embeddings_manager.setThreshold(threshold);
}

// private
dlib::rectangle FaceRecognizer::detectFace(cv_image_t image)
{
    std::vector<dlib::rectangle> dets = detector(image);
    int num_faces = dets.size();
    if (num_faces > 1) throw FaceNotDetectedException();
    if (num_faces == 0) throw MultipleFacesDetectedException();
    return dets[0];
}

dlib::full_object_detection FaceRecognizer::predictShape(
        cv_image_t image,
        dlib::rectangle face_coords) 
{
    return shape_predictor(image, face_coords);
}

dlib::matrix<float,0,1> FaceRecognizer::computeFaceEmbedding(
        cv_image_t image,
        dlib::full_object_detection shape)
{
    dlib::matrix<rgb_pixel> face_chip;
    extract_image_chip(image, get_face_chip_details(shape, 150, 0.25), face_chip);
    std::vector<dlib::matrix<rgb_pixel>> faces(1, face_chip);
    std::vector<dlib::matrix<float,0,1>> embeddings = feature_extractor(faces);
    return embeddings[0];
}

void FaceRecognizer::loadModels(string resources_path)
{
    detector = dlib::get_frontal_face_detector();
    string filepath = resources_path + "/" + SHAPE_PREDICTOR_FN;
    dlib::deserialize(filepath) >> shape_predictor;
    filepath = resources_path + "/" + DLIB_FACEREC_FN;
    dlib::deserialize(filepath) >> feature_extractor;
}

string FaceRecognizer::jsonify(std::vector<RecognitionInfo> infos)
{
    stringstream document;
    document << "{" << endl;
    document << "\"status\": \"FaceDetected\"" << endl;
    document << "\"results\": [" << endl;
    for (auto info: infos)
    {
        document << info.jsonify() << "," << endl;
    }
    document.seekp(-1, document.cur); // removes the last comma
    document << "]" << endl;
    document << "}" << endl;
    return document.str();
}