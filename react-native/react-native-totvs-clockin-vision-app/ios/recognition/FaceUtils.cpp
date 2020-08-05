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

#include "FaceUtils.h"


cv::Mat FaceUtils::adjustSize(const cv::Mat &image, int max_dimension)
{
    int height = image.rows;
    int width = image.cols;
    int max_img_dimension = (height > width) ? height : width;
    if (max_img_dimension > max_dimension)
    {
        float scale = (float) max_dimension / (float) max_img_dimension;
        return resizeImage(image, scale);
    }
    return image;
}

cv::Mat FaceUtils::resizeImage(const cv::Mat &image, float scale)
{
    int height = image.rows;
    int width = image.cols;
    int new_height = round(height * scale);
    int new_width = round(width * scale);
    cv::Mat resized_image;
    cv::resize(image, resized_image, cv::Size(new_width, new_height));
    return resized_image;
}

cv::Mat FaceUtils::decodeImage(const std::string &image_str)
{
    dlib::base64 base64_coder;
    std::ostringstream sout;
    std::istringstream sin;

    sin.str(image_str);
    sout.str("");

    base64_coder.decode(sin, sout);
    sin.clear();
    sin.str(sout.str());
    sout.str("");

    std::string dec_jpg = sin.str();
    std::vector<uchar> data(dec_jpg.begin(), dec_jpg.end());
    cv::Mat img = cv::imdecode(cv::Mat(data), 0);
    if (img.empty())
    {
        throw "empty image";
    }
    if (img.channels() == 1)
    {
        cv::cvtColor(img, img, cv::COLOR_GRAY2BGR);
    }
    return img;
}
