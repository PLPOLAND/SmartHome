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
    int8_t id;
public:
    Termometr();
    ~Termometr();

    float getTemperature();
    byte getID();
    bool isCorrect();
};

#endif // !TERMOMETR_H