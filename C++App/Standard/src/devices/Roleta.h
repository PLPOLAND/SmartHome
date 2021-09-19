#ifndef Roleta_h
#define Roleta_h

#include <Arduino.h>
#include <Timers.h>
#include <devices/Przekaznik.h>


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

    byte getPinUp() { return this->p_up.getPin(); };
    void setPinUp(byte pin)
    {
        this->p_up.setPin(pin);
        this->p_up.setStan(false);
    };
    byte getPinDown() { return this->p_down.getPin(); };
    void setPinDown(byte pin)
    {
        this->p_down.setPin(pin);
        this->p_down.setStan(false);
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
