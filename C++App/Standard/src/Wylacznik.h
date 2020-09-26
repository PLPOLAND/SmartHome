#include <Arduino.h>
#include <Timers.h>
#ifndef Wylacznik_h
#define Wylacznik_h

enum StanWylacznika
{
    PUSZCZONY,
    PRZYCISNIETY,
    PRZYTRZYMANY
};

class Wylacznik
{
private:
    byte pin;
    StanWylacznika stan;
    int klikniecia;
    Timer time;
public:
    Wylacznik();
    ~Wylacznik();

    byte getPin()
    {
        return pin;
    };
    void setPin(byte pin)
    {
        this->pin = pin;
        digitalWrite(pin, stan == false ? LOW : HIGH);
    };
    StanWylacznika getStan()
    {
        return stan;
    };
    void updateStan();

    void tic();
};


#endif // !Wylacznik_h
