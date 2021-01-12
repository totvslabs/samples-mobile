#ifndef FACERECOGNIZER_H
#define FACERECOGNIZER_H

#include <string>
#include <vector>
#include <dlib/base64.h>
#include <dlib/compress_stream.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_io.h>
#include <dlib/opencv/cv_image.h>
#include <dlib/image_io.h>
#include <dlib/image_processing.h>
#include <dlib/image_processing/generic_image.h>
#include <opencv2/core/core.hpp>

#include "EmbeddingsManager.h"
#include "DLibModelType.h"

typedef dlib::cv_image<dlib::bgr_pixel> cv_image_t;

using namespace std;

class FaceRecognizer
{
public:
    FaceRecognizer(string resources_path);
    /**
     * Returns JSON string with the schema:
     * {
     *    "status": "FaceDetected"|"FaceNotDetected"|"MultipleFacesDetected"|"PersonNotRecognized",
     *    "embedding": string,
     *    "results": [
     *        {
     *            "name": string,
     *            "person_id": string,
     *            "distance": float
     *        }
     *    ]
     * }
     */
    string faceRecognition(string image_str, bool skip_detection);
    string faceRecognition(const cv::Mat image, bool skip_detection);
    void loadEmbeddings(string embeddings_path);
    void updateThreshold(float threshold);
    int getNNInputSize();
    void setNNInputSize(int new_size);

private:
    EmbeddingsManager embeddings_manager;
    dlib::frontal_face_detector detector;
    dlib::shape_predictor shape_predictor;
    anet_type feature_extractor;
    float threshold = 0.6;
    int nn_input_size = 400;

    void loadModels(string resources_path);
    dlib::rectangle detectFace(cv_image_t image);
    dlib::full_object_detection predictShape(
        cv_image_t image,
        dlib::rectangle face_coords
    );
    dlib::matrix<float,0,1> computeFaceEmbedding(
        cv_image_t image,
        dlib::full_object_detection shape
    );

    string jsonify(
        std::vector<RecognitionInfo> infos,
        dlib::matrix<float, 0, 1> embedding,
        std::string status
    );
    string faceRecognition(const cv_image_t image, bool skip_detection);
};

#endif
