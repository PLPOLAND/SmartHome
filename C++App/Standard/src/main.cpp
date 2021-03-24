#include <Arduino.h>
#include <Stale.h>
#include <Przekaznik.h>
#include <Wylacznik.h>
#include <I2CAnything.h>
#include <Wire.h>
#include <I2CConverter.h>
#include <Timers.h>

I2CConverter* comunication;
Timer timer;
void setup()
{
    Serial.begin(115200); // start serial for output
    comunication = I2CConverter::getInstance();
    Wire.onReceive(I2CConverter::onRecieveEvent);
    Wire.onRequest(I2CConverter::onRequestEvent);
    timer.begin(MINS(CZAS_ODSWIERZANIA_TEMPERATURY));
}

void loop()
{
    if (comunication->termometry.size()>0 && timer.available())//TODO ustawić częstotliwość sprawdzania!
    {
        for (byte i = 0; i < comunication->termometry.size(); i++) {
        comunication->termometry.get(i)->updateTemperature();
        }
        Serial.println("timer");
        timer.begin(MINS(CZAS_ODSWIERZANIA_TEMPERATURY));
    }
    
}


