#include <Arduino.h>
#ifndef Przekaznik_h
#define Przekaznik_h
class Przekaznik
{
private:
    byte pin;
    bool stan;

public:
    Przekaznik(byte pin, bool stan);
    ~Przekaznik();
    byte getPin()
    {
        return pin;
    };
    void setPin(byte pin)
    {
        if (pin >= 2 && pin <= 16)
        {
            digitalWrite(pin, LOW); //resetowanie poprzedniego stanu wyjscia
            this->pin = pin;
            pinMode(pin, OUTPUT);
            digitalWrite(pin, stan == false ? LOW : HIGH);
        }      
    };
    bool getStan(){
        return stan;
    };
    void setStan(bool stan)
    {
        this->stan = stan;
        digitalWrite(pin, stan == false ? LOW : HIGH);
    };
    void setStan(int stan)
    {
        this->stan = stan == 1 ? true : false;

        digitalWrite(pin, stan == false ? LOW : HIGH);
    }
};

#endif // !Przekaznik_h