#ifndef Przekaznik_h
#define Przekaznik_h
#include <Arduino.h>
#include "devices/Device.h"

//Obsługa przekaźnika
class Przekaznik : public Device
{
private:
    byte pin;
    bool stan;

public:
    Przekaznik();
    Przekaznik(byte pin, bool stan);
    ~Przekaznik();
    
    byte getPin();
    void setPin(byte pin); 
    bool getStan();
    void setStan(bool stan);
    void setStan(int stan);
};

#endif // !Przekaznik_h