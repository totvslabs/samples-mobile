#include <math.h>
#include <sstream>
#include <string>
#include <exception>
#include <stdexcept>

/* In case logging is needed:
#include <android/log.h>
__android_log_write(ANDROID_LOG_DEBUG, "AICore", document.str().c_str());
*/
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

class FaceNotDetectedException
{
};
class MultipleFacesDetectedException
{
};

// public
FaceRecognizer::FaceRecognizer(string resources_path)
{
    loadModels(resources_path);
    embeddings_manager = EmbeddingsManager(resources_path);
}

string FaceRecognizer::faceRecognition(string image_str, bool skip_detection)
{
    cv::Mat image = FaceUtils::decodeImage(image_str);
    cv::Mat resized_image = FaceUtils::adjustSize(image, nn_input_size);
    cv_image_t cv_image(resized_image);
    return faceRecognition(cv_image, skip_detection);
}

string FaceRecognizer::faceRecognition(const cv::Mat image, bool skip_detection)
{
    cv::Mat bgr_image;
    cv::cvtColor(image.t(), bgr_image, cv::COLOR_BGRA2BGR);
    cv::Mat resized_image = FaceUtils::adjustSize(bgr_image, nn_input_size);
    cv_image_t cv_image(resized_image);
    return faceRecognition(cv_image, skip_detection);
}

string FaceRecognizer::faceRecognition(const cv_image_t cv_image, bool skip_detection)
{
    try
    {
        dlib::rectangle face_coords;
        if (skip_detection) {
            face_coords = dlib::rectangle(0, 0, cv_image.nc(), cv_image.nr());
        } else {
            face_coords = detectFace(cv_image);
        }
        dlib::full_object_detection shape = predictShape(cv_image, face_coords);
        auto embedding = computeFaceEmbedding(cv_image, shape);
        auto recognized_employees = embeddings_manager.search(embedding, threshold);
        return jsonify(recognized_employees, embedding);
    }
    catch (FaceNotDetectedException &e)
    {
        return string("{\"status\": \"FaceNotDetected\"}");
    }
    catch (MultipleFacesDetectedException &e)
    {
        return string("{\"status\": \"MultipleFacesDetected\"}");
    }
    catch (PersonNotRecognizedException &e)
    {
        return string("{\"status\": \"PersonNotRecognized\"}");
    };
}

void FaceRecognizer::loadEmbeddings(string embeddings_path)
{
    embeddings_manager = EmbeddingsManager(embeddings_path);
}

void FaceRecognizer::updateThreshold(float threshold)
{
    this->threshold = threshold;
}

int FaceRecognizer::getNNInputSize()
{
    return nn_input_size;
}

void FaceRecognizer::setNNInputSize(int new_size)
{
    this->nn_input_size = new_size;
}

// private
dlib::rectangle FaceRecognizer::detectFace(cv_image_t image)
{
    std::vector<dlib::rectangle> dets = detector(image);
    int num_faces = dets.size();
    if (num_faces > 1)
        throw MultipleFacesDetectedException();
    if (num_faces == 0)
        throw FaceNotDetectedException();
    return dets[0];
}

dlib::full_object_detection FaceRecognizer::predictShape(
    cv_image_t image,
    dlib::rectangle face_coords)
{
    return shape_predictor(image, face_coords);
}

dlib::matrix<float, 0, 1> FaceRecognizer::computeFaceEmbedding(
    cv_image_t image,
    dlib::full_object_detection shape)
{
    dlib::matrix<rgb_pixel> face_chip;
    extract_image_chip(image, get_face_chip_details(shape, 150, 0.25), face_chip);
    std::vector<dlib::matrix<rgb_pixel>> faces(1, face_chip);
    std::vector<dlib::matrix<float, 0, 1>> embeddings = feature_extractor(faces);
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

string FaceRecognizer::jsonify(
    std::vector<RecognitionInfo> infos,
    dlib::matrix<float, 0, 1> embedding)
{
    stringstream document;
    document << "{" << endl;
    document << "  \"status\": \"FaceDetected\"," << endl;
    document << "  \"embedding\": \"";
    for (auto element : embedding)
    {
        document << element << ", ";
    }
    document.seekp(-2, document.cur); // removes the last comma
    document << "\"," << endl;
    document << "  \"results\": [" << endl;
    for (auto info : infos)
    {
        document << "    " << info.jsonify() << "," << endl;
    }
    document.seekp(-2, document.cur); // removes the last comma
    document << endl << "  ]" << endl;
    document << "}" << endl;

    return document.str();
}
