#include <Arduino.h>
#include <Przekaznik.h>
#include <Wylacznik.h>
#include <I2CAnything.h>
#include <Wire.h>
#include <I2CConverter.h>

I2CConverter* comunication;
void setup()
{
    Serial.begin(115200); // start serial for output
    comunication = I2CConverter::getInstance();
    Wire.onReceive(I2CConverter::onRecieveEvent);
    Wire.onRequest(I2CConverter::onRequestEvent);
}

void loop()
{
    delay(1);
}


