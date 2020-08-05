#include <math.h>
#include <sstream>
#include <string>
#include <algorithm>
#include <iterator>

#include <dlib/base64.h>
#include <dlib/compress_stream.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_io.h>
#include <dlib/opencv/cv_image.h>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/opencv.hpp>

#include "EmbeddingsManager.h"

string DAT_EMBEDDINGS_FN = "face_descriptors_jittered.dat";
string DAT_EMPLOYEES_INFO_FN = "face_names_jittered.dat";

EmbeddingsManager::EmbeddingsManager()
{
}

EmbeddingsManager::EmbeddingsManager(string embeddings_path)
{
    string filepath = embeddings_path + "/" + DAT_EMBEDDINGS_FN;
    dlib::deserialize(filepath) >> embeddings;
    filepath = embeddings_path + "/" + DAT_EMPLOYEES_INFO_FN;
    dlib::deserialize(filepath) >> employees_info;
}

vector<RecognitionInfo> EmbeddingsManager::search(
    dlib::matrix<float, 0, 1> embedding)
{
    vector<RecognitionInfo> recognized_employees;
    vector<string> tokens;
    string token;
    char delim = '_';
    for (size_t j = 0; j < embeddings.size(); ++j)
    {
        if (length(embedding - embeddings[j]) < 0.6)
        {
            stringstream ss(employees_info[j]);
            while (std::getline(ss, token, delim)) tokens.push_back(token);
            if (tokens.size() != 2) 
                continue;
            RecognitionInfo rec(tokens[1], tokens[0], 0);
            recognized_employees.push_back(rec);
        }
    }
    if (recognized_employees.size() == 0) throw PersonNotRecognizedException();
    return recognized_employees;
}

void EmbeddingsManager::setThreshold(float threshold)
{
    this->threshold = threshold;
}
