#ifndef FACEUTILS_H
#define FACEUTILS_H

#include <string>
#include <dlib/image_processing/frontal_face_detector.h>
#include <opencv2/core/core.hpp>

class FaceUtils
{
public:
    static cv::Mat decodeImage(const std::string &image_str);
    static cv::Mat adjustSize(const cv::Mat &image, int max_dimension);
    static cv::Mat resizeImage(const cv::Mat &image, float scale);
};

#endif
