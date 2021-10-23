#include <devices/Przekaznik.h>
Przekaznik::Przekaznik(){
    Device(TYPE::PRZEKAZNIK);
}
Przekaznik::~Przekaznik()
{
    digitalWrite(pin, LOW);
}
Przekaznik::Przekaznik(byte id, byte pin, bool stan = false)
{
    Device(TYPE::PRZEKAZNIK,id);
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

byte Przekaznik::getPin()
{
    return pin;
};

//Ustawianie pinu uzywanego do sterowania przekaźnikiem
void Przekaznik::setPin(byte pin)
{
    if (pin >= 2 && pin <= 16)
    {
        digitalWrite(pin, LOW); //resetowanie poprzedniego stanu wyjscia
        this->pin = pin;
        pinMode(pin, OUTPUT);
        digitalWrite(pin, stan == false ? LOW : HIGH);
    }
};

bool Przekaznik::getStan()
{
    return stan;
};
//włącz/wyłącz przekaźnik (true/false)
void Przekaznik::setStan(bool stan)
{
    this->stan = stan;
    digitalWrite(pin, stan == false ? LOW : HIGH);
};
//włącz/wyłącz przekaźnik (1 = włącz; inna wartość = wyłącz)
void Przekaznik::setStan(int stan)
{
    this->stan = stan == 1 ? true : false;

    digitalWrite(pin, this->stan == false ? LOW : HIGH);
}