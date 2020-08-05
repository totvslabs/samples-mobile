#ifndef EMBEDDINGSMANAGER_H
#define EMBEDDINGSMANAGER_H

#include <string>
#include <tuple>
#include <vector>
#include <dlib/matrix.h>

#include "RecognitionInfo.h"

using namespace std;

class PersonNotRecognizedException {};

class EmbeddingsManager
{
public:
    EmbeddingsManager();
    EmbeddingsManager(string embeddings_path);

    vector<RecognitionInfo> search(dlib::matrix<float,0,1> embedding);
    void setThreshold(float threshold);

private:
  vector<string> employees_info;
  vector<dlib::matrix<float,0,1>> embeddings;
  float threshold = 0.6;
};

#endif
