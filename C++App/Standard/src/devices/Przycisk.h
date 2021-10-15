#ifndef Wylacznik_h
#define Wylacznik_h
#include <Arduino.h>
#include <Timers.h>
#include "LinkedList.h"
#include "devices/Device.h"

/**
 * 
 * Klasa obsługująca przycisk/wyłącznik
 * TODO: funkcje dla kliknięć.
 */
class Przycisk: public Device
{
public:
    enum StanPrzycisku
    {
        BRAK_AKCJI,
        PUSZCZONY,
        PRZYCISNIETY,
        PRZYTRZYMANY
    };

private:
    byte pin;
    StanPrzycisku stan;
    int klikniecia;
    Timer* time;

    // LinkedList<> funkcje; // TODO:funkcje
public:
    Przycisk();
    ~Przycisk();
    Przycisk(byte pin);

    byte getPin()
    {
        return pin;
    };
    void setPin(byte pin)
    {
        this->pin = pin;
        pinMode(this->pin,INPUT_PULLUP);
    };
    StanPrzycisku getStan()
    {
        return stan;
    };
    void updateStan();

    void tic();
};

#endif // !Wylacznik_h
