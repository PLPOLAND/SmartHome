#include <Arduino.h>
#include <Timers.h>
#ifndef Roleta_h
#define Roleta_h

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
class Roleta {
private:
    byte pinUp;
    byte pinDown;
    StanRolety stan;
    Timer* time;

    void forceSetPinUpState(bool stan);
    void forceSetPinDownState(bool stan);
    void setPinUpState(bool stan);
    void setPinDownState(bool stan);

    // LinkedList<> funkcje; // TODO:funkcje
public:
    Roleta();
    ~Roleta();
    Roleta(byte pinUp, byte pinDown);

    byte getPinUp() { return pinUp; };
    void setPinUp(byte pin)
    {
        this->pinUp = pin;
        pinMode(this->pinUp, OUTPUT);
        digitalWrite(this->pinUp, LOW); //TODO: Sprawdzić czy LOW!
    };
    byte getPinDown() { return pinDown; };
    void setPinDown(byte pin)
    {
        this->pinDown = pin;
        pinMode(this->pinDown, OUTPUT);
        digitalWrite(this->pinDown, LOW); //TODO: Sprawdzić czy LOW!
    };
    StanRolety getStan() { return stan; };



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
