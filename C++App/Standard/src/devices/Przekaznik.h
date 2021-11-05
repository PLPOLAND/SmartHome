#ifndef Przekaznik_h
#define Przekaznik_h
#include <Arduino.h>
#include "devices/Device.h"
#include "Stale.h"

//Obsługa przekaźnika
class Przekaznik : public Device
{
private:
    byte pin;
    bool stan;

public:
    Przekaznik();
    ~Przekaznik();
    byte getPin();
    void setPin(byte pin); 
    bool getStan();
    void setStan(bool stan);
    void setStan(int stan);
    bool begin(byte pin, bool stan = false);
};

#endif // !Przekaznik_h