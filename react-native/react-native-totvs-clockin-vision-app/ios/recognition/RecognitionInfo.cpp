#include <sstream>

#include "RecognitionInfo.h"

RecognitionInfo::RecognitionInfo(string name, string person_id, float distance)
{
    this->name = name;
    this->person_id = person_id;
    this->distance = distance;
}

string RecognitionInfo::jsonify()
{
    stringstream document;
    document << "{";
    document << "\"name\": " << name << "," << endl;
    document << "\"person_id\": " << person_id << "," << endl;
    document << "\"distance\": " << distance << endl;
    document << "}";
    return document.str();
}