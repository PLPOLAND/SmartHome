#ifndef HIGROMETR_H
#define HIGROMETR_H

#include<Arduino.h>
#include<DHT.h>
#include<Timers.h>
#include "devices/Device.h"

#define DHTTYPE DHT11
#define DHTPIN 10

class Higrometr : public Device
{
public:
    Higrometr();
    ~Higrometr();

    int getHumidity();
    float getTemperature();
    void update();
    bool isCorrect();
    ///Returns byte[7]
    byte* getStateAsByteArray();

private:
    DHT* dht;
    Timer* timer;
    int humidity;
    float temperature;
};

#endif