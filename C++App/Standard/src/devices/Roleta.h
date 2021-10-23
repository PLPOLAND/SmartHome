#ifndef Roleta_h
#define Roleta_h

#include <Arduino.h>
#include <Timers.h>
#include "devices/Przekaznik.h"
#include "devices/Device.h"

#define CZAS_CALKOWITEJ_ZMIANY_POLOZENIA SECS(2) //TODO: kalibracja czasu!


enum StanRolety {
    OPUSZCZONA,
    NIEOKRESLONY,
    PODNIESIONA
};
/**
 * 
 * Klasa obsługująca rolete
 */
class Roleta :public Device{
private:
    Przekaznik p_up;
    Przekaznik p_down;
    StanRolety stan;
    Timer* time;

    void forcePinUpState(bool stan);
    void forcePinDownState(bool stan);
    void setPinUpState(bool stan);
    void setPinDownState(bool stan);

    // LinkedList<> funkcje; // TODO:funkcje
public:
    Roleta();
    ~Roleta();
    Roleta(byte pinUp, byte pinDown);

    byte getPinUp();
    void setPinUp(byte pin);
    byte getPinDown();
    void setPinDown(byte pin);
    StanRolety getStan();



    void podnies();
    void opusc();
    void up();
    void down();
    void stop();

    void tic();
    // Opisuje aktualnie wykonywaną akcje przez "rolete"
    enum Akcja
    {
        POSTOJ,
        PODNOSZENIE_CALKOWITE,
        OPUSZCZANIE_CALKOWITE,
        PODNOSZENIE,
        OPUSZCZANIE
    } akcja;


};

#endif // !Roleta_h
