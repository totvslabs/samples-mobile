#ifndef RECOGNITIONINFO_H
#define RECOGNITIONINFO_H

#include <string>

using namespace std;

class RecognitionInfo
{
public:
    string name;
    string person_id;
    float distance;

    RecognitionInfo(string name, string person_id, float distance);
    string jsonify();
};

#endif
