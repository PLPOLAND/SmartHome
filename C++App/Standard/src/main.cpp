#include <Arduino.h>

// #define TEST

#ifdef TEST

    #include "System.h"
    #include "Stale.h"
    #include "devices/Przycisk.h"
    #include "devices/Roleta.h"
    Przycisk przycisk;
    // Roleta roleta(11,12);
    Przekaznik przekaznik;
    void setup()
    {
        Serial.begin(115200); // start serial for output
        // przycisk.begin(10);
        przekaznik.begin(11);
        OUT_LN("START");
    }

    void loop()
    {
        przekaznik.setStan(1);
        // przycisk.updateStan();
        // roleta.tic();

        // while (Serial.available())
        // {
        //     char ch = Serial.read();
        //     if (ch == '1')
        //     {
        //         roleta.podnies();
        //         OUT_LN("Podnies");
        //     }
        //     else if (ch == '2')
        //     {
        //         roleta.opusc();
        //         OUT_LN("Opusc");
        //     }
        //     else if (ch == '3')
        //     {
        //         roleta.up();
        //         OUT_LN("up");
        //         delay(500);
        //         roleta.stop();
        //         OUT_LN("stop");
        //     }
        //     else if(ch == '4')
        //     {
        //         roleta.down();
        //         OUT_LN("down");
        //         delay(500);
        //         roleta.stop();
        //         OUT_LN("stop");
        //     }
            
        // }

        // OUT_LN(przycisk.getStan());
        // OUT_LN(digitalRead(2));
    }

#endif // TEST
#ifndef TEST

    #include "System.h"
    
    System* sys;
    void setup()
    {
        Serial.begin(115200);
        // Serial.println(freeMemory());
        OUT_LN(freeMemory());
        OUT_LN("setup()");
        sys = System::getSystem();
        sys->begin();
        OUT_LN(freeMemory());
    }

    void loop()
    {
        sys->tic();
    }
#endif

