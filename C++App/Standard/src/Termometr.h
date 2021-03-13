#include <Arduino.h>
#include <DallasTemperature.h>
#ifndef TERMOMETR_H
#define TERMOMETR_H
#define ONEWIRE_BUS 8
class Termometr
{
private:
    static OneWire oneWire;
    static byte termometrowWSystemie;
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