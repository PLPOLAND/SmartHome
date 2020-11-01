#include <Arduino.h>
#include <DallasTemperature.h>
#ifndef TERMOMETR_H
#define TERMOMETR_H
#define ONEWIRE_BUS 8
class Termometr
{
private:
    static DallasTemperature* sensors;
    static byte termometrowWSystemie;
    byte id;
public:
    Termometr();
    ~Termometr();

    float getTemperature();
    byte getID();
};

#endif // !TERMOMETR_H