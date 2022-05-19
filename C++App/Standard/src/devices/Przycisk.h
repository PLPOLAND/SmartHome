#ifndef Wylacznik_h
#define Wylacznik_h
#include <Arduino.h>
#include <Timers.h>
#include <Stale.h>
#include "LinkedList.h"
#include "devices/Device.h"
#include "I2C/Command.h"
#include "Przekaznik.h"
#include "Roleta.h"
#include "Dioda.h"
// #include "System.h" NIEWOLNO!

/**
 * 
 * Klasa obsługująca przycisk/wyłącznik
 * TODO: funkcje dla kliknięć.
 */
class Przycisk: public Device
{
public:
    enum StanPrzycisku
    {
        BRAK_AKCJI,
        PUSZCZONY,
        PRZYCISNIETY,
        PRZYTRZYMANY
    };

private:
    byte pin;
    StanPrzycisku stan;
    int klikniecia;
    Timer* time;
    Dioda dioda;
    /**
     *  Przechowuje Komendy wykonywane po odpowiedniej liczbie klikniec
     */
    LinkedList<Command*> funkcje_klikniecia;
    /**
     * Przechowuje Komendy wykonywane po przytrzymaniach. 
     * 
     */
    LinkedList<Command*> funkcje_przytrzymania;
    /**
     * Przechowuje Komendy wykonywane po puszczeniu przycisku po przytrzymaniu.
     * 
     */
    LinkedList<Command*> funkcje_przytrzymania_puszczenie;

    /**
     * @brief obiekt używany do zapychania niezdefiniowanych funkcji w przycisku
     * 
     */
    const static Command *zapychacz;

public:
    Przycisk();
    ~Przycisk();
    bool begin(byte pin);
    

    byte getPin();
    bool setPin(byte pin);
    
    StanPrzycisku getStan();
    void updateStan();

    void tic();

    bool wykonaj();

    bool dodajFunkcjeKlikniecia(Command *command, byte klikniec);
    bool usunFunkcjeKlikniecia(byte klikniec);
    bool dodajFunkcjePrzytrzymania(Command* command, byte klikniec);
    bool dodajFunkcjePuszczeniaPoPrzytrzymaniu(Command* command, byte klikniec);
    
    /**
     * @brief Wykonuje komendę dostarczoną w argumencie.
     *
     * @param command komenda do wykonania
     * @return true
     * @return false
     */
    bool runCommand(Command *command);
};

#endif // !Wylacznik_h
