#ifndef Wylacznik_h
#define Wylacznik_h
#include <Arduino.h>
#include <Timers.h>
#include <Stale.h>
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
        PRAWIE_WCISNIETY,
        PRZYTRZYMANY
    };

private:
    byte pin;
    StanPrzycisku stan;
    int klikniecia;
    Timer time;  // okres na kolejne kliknięcia / wykrycie przytrzymania
    Timer time2; // eliminacja drgań styków przy pierwszym wciśnięciu
    bool czyPominac = false;

    Dioda dioda;
    /**
     *  Przechowuje Komendy wykonywane po odpowiedniej liczbie klikniec (indeks = liczba kliknięć).
     *  Nieużywane pozycje wskazują na zapychacz. Stała tablica zamiast LinkedList - lista alokowała
     *  osobny węzeł na każdą pozycję, a RAM-u na slave-ach z kilkoma przyciskami brakowało.
     */
    Command *funkcje_klikniecia[MAX_NUMBER_OF_BUTTON_FUNCTIONS];

    /**
     * @brief obiekt używany do zapychania niezdefiniowanych funkcji w przycisku
     * 
     */
    const static Command *zapychacz;

public:
    Przycisk();
    ~Przycisk();
    bool begin(byte pin, bool czyPomijacPierszwy = false);

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
    
    void setCzyPominac(bool pominac);
    bool isCzyPominac();

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
