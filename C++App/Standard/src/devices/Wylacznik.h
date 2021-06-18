#include <Arduino.h>
#include <Timers.h>
#ifndef Wylacznik_h
#define Wylacznik_h

enum StanWylacznika
{
    BRAK_AKCJI,
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
    Timer* time;
public:
    Wylacznik();
    ~Wylacznik();
    Wylacznik(byte pin);

    byte getPin()
    {
        return pin;
    };
    void setPin(byte pin)
    {
        this->pin = pin;
        pinMode(this->pin,INPUT_PULLUP);
    };
    StanWylacznika getStan()
    {
        return stan;
    };
    void updateStan();

    void tic();
};


#endif // !Wylacznik_h
