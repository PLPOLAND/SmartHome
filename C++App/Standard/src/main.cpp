#include <Arduino.h>

// #define TEST

#ifdef TEST

    #include <devices/Przycisk.h>
    #include <devices/Roleta.h>
    Przycisk przycisk(10);
    Roleta roleta(11,12);

    void setup()
    {
        Serial.begin(115200); // start serial for output
        OUTPUT_LN("START");
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
                OUTPUT_LN("Podnies");
            }
            else if (ch == '2')
            {
                roleta.opusc();
                OUTPUT_LN("Opusc");
            }
            else if (ch == '3')
            {
                roleta.up();
                OUTPUT_LN("up");
                delay(500);
                roleta.stop();
                OUTPUT_LN("stop");
            }
            else if(ch == '4')
            {
                roleta.down();
                OUTPUT_LN("down");
                delay(500);
                roleta.stop();
                OUTPUT_LN("stop");
            }
            
        }

        // OUTPUT_LN(przycisk.getStan());
        // OUTPUT_LN(digitalRead(2));
    }

#endif // TEST
#ifndef TEST

    #include "System.h"
    
    System* sys;
    void setup()
    {
        Serial.begin(115200);
        // Serial.println(freeMemory());
        OUTPUT_LN(freeMemory());
        OUTPUT_LN("setup()");
        sys = System::getSystem();
        sys->begin();
        OUTPUT_LN(freeMemory());
    }

    void loop()
    {
        sys->tic();
    }
#endif

