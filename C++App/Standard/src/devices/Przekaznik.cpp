#include <devices/Przekaznik.h>

Przekaznik::~Przekaznik()
{

    digitalWrite(pin, LOW);
}
Przekaznik::Przekaznik(byte pin, bool stan = false)
{
    if (pin >= 2 && pin <= 16)
    {
        this->pin = pin;
        pinMode(pin, OUTPUT);
    }
    this->stan = stan;
    if (stan)
    {
        digitalWrite(pin, HIGH);
    }
    else
    {
        digitalWrite(pin, LOW);
    }
}