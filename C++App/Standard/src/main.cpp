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
        przycisk.begin(14);
        przekaznik.begin(11);
        OUT_LN("START");
    }

    void loop()
    {
        przekaznik.setStan(1);
        przycisk.updateStan();
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
    #include "devices/Przycisk.h"
    
    // Przycisk przycisk1;
    // Przycisk przycisk2;
    // Roleta roleta;
    // Przekaznik przekaznik1;
    // Przekaznik przekaznik2;



    System* sys;
    void setup()
    {
        pinMode(7, OUTPUT);
        digitalWrite(7,HIGH);
        Serial.begin(115200);
        // Serial.println(freeMemory());
        OUT_LN(freeMemory());
        OUT_LN("setup()");
        sys = System::getSystem();
        sys->begin();

        Przycisk* p1 = (Przycisk*) sys->addDevice(Device::TYPE::PRZYCISK,A3);
        Przycisk* p2 = (Przycisk*) sys->addDevice(Device::TYPE::PRZYCISK,14);

        Roleta* r =(Roleta*) sys->addDevice(Device::TYPE::ROLETA,16,15);
        Przekaznik* s1 =(Przekaznik*) sys->addDevice(Device::TYPE::PRZEKAZNIK,12);
        Przekaznik* s2 =(Przekaznik*) sys->addDevice(Device::TYPE::PRZEKAZNIK,13);

        Command* tmp = new Command;
        tmp->setDevice(r);
        tmp->setCommandType(Command::KOMENDY::RECEIVE_ZMIEN_STAN_ROLETY);
        byte parametry[8] = {'U', 0, 0, 0, 0, 0, 0, 0}; 
        tmp->setParams(parametry);
        p1->dodajFunkcjeKlikniecia(tmp,1);
        tmp = new Command;
        tmp->setDevice(r);
        tmp->setCommandType(Command::KOMENDY::RECEIVE_ZMIEN_STAN_ROLETY);
        parametry[0] = 'D'; 
        tmp->setParams(parametry);
        p1->dodajFunkcjeKlikniecia(tmp,2);

        tmp = new Command;
        tmp->setDevice(s1);
        tmp->setCommandType(Command::KOMENDY::RECEIVE_ZMIEN_STAN_PRZEKAZNIKA);
        parametry[0] = 0; 
        tmp->setParams(parametry);
        p2->dodajFunkcjeKlikniecia(tmp, 1);
        tmp = new Command;
        tmp->setDevice(s2);
        tmp->setCommandType(Command::KOMENDY::RECEIVE_ZMIEN_STAN_PRZEKAZNIKA);
        parametry[0] = 0; 
        tmp->setParams(parametry);
        p2->dodajFunkcjeKlikniecia(tmp, 2);

        OUT_LN(freeMemory());
        digitalWrite(7,LOW);
    }

    void loop()
    {
        sys->tic();
    }
#endif

