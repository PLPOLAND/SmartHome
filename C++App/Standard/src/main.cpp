#include <Arduino.h>

#define TEST

#ifdef TEST

    #include <devices/Przycisk.h>
    #include <devices/Roleta.h>
    Przycisk przycisk(2);
    Roleta roleta(3,4);

    void setup()
    {
        Serial.begin(9600); // start serial for output
        
    }

    void loop()
    {
        przycisk.updateStan();
        roleta.tic();

        while (Serial.available())
        {
            char ch = Serial.read();
            if (ch == '1')
            {
                roleta.podnies();
                Serial.println("Podnies");
            }
            else if (ch == '2')
            {
                roleta.opusc();
                Serial.println("Opusc");
            }
            else if (ch == '3')
            {
                roleta.up();
                Serial.println("up");
                delay(500);
                roleta.stop();
                Serial.println("stop");
            }
            else if(ch == '4')
            {
                roleta.down();
                Serial.println("down");
                delay(500);
                roleta.stop();
                Serial.println("stop");
            }
            
        }
        

        // Serial.println(przycisk.getStan());
        // Serial.println(digitalRead(2));
    }

#endif // TEST
#ifndef TEST

    #include <Stale.h>
    #include <devices/Przekaznik.h>
    #include <devices/Wylacznik.h>
    #include <I2C/I2CAnything.h>
    #include <Wire.h>
    #include <I2C/I2CConverter.h>
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
#endif

