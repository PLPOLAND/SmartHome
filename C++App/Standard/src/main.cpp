#include <Arduino.h>

// #define TEST

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

    #include "System.h";
    System* system;
    void setup()
    {
        system = System::getSystem();
    }

    void loop()
    {
        system->tic();
    }
#endif

