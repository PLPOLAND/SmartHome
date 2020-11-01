#include "Arduino.h"
#include "Wire.h"
#include <Kontener.h>
#include <Termometr.h>
#include <Przekaznik.h>
#ifndef I2CCONVERTER_H
#define I2CCONVERTER_H
#define PINOW_NA_ADRES 6

class I2CConverter
{
public:
    I2CConverter();
    I2CConverter(I2CConverter &&) = default;
    I2CConverter(const I2CConverter &) = default;
    I2CConverter &operator=(I2CConverter &&) = default;
    I2CConverter &operator=(const I2CConverter &) = default;
    ~I2CConverter();

    void static onRecieveEvent(int howManyBytes);
    void static onRequestEvent();

private:
    bool isWorkToDo = false;
    Kontener<Termometr*> termometry;
    Kontener<Przekaznik*> przekazniki;
    void printTemperature(byte id);
    void addTermometr();
};

#endif // !I2CCONVERTER_H