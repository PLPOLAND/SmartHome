#include <Arduino.h>
#include <Timers.h>
#include "LinkedList.h"
#ifndef Wylacznik_h
#define Wylacznik_h

enum StanPrzycisku
{
    BRAK_AKCJI,
    PUSZCZONY,
    PRZYCISNIETY,
    PRZYTRZYMANY
};
/**
 * 
 * Klasa obsługująca przycisk/wyłącznik
 * TODO: funkcje dla kliknięć.
 */
class Przycisk
{
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
