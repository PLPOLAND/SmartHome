#ifndef TERMOMETR_H
#define TERMOMETR_H
#include <Arduino.h>
#include <DallasTemperature.h>
#include "devices/Device.h"

//pin komunikacji oneWire
#define ONEWIRE_BUS 8 

class Termometr : public Device
{
private:
    static OneWire oneWire;
    //ile termometrów jest podłączonych do systemu. // TODO czy dobry opis?
    static byte termometrowWSystemie;
    //id termometru
    int8_t id;
public:
    static DallasTemperature sensors;
    Termometr();
    ~Termometr();
    float temperatura;
    byte begin();
    float getTemperature();
    byte getID();
    bool isCorrect();
    void updateTemperature();
};

#endif // !TERMOMETR_H