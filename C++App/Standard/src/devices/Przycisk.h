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
    // Filtr zakłóceń wejścia (patrz odczytajWejscie)
    bool odczytSurowy;            // ostatni surowy odczyt pinu (true = wciśnięty)
    bool odczytStabilny;          // stan po filtrze, na nim działa maszyna stanów
    unsigned long czasZmianySurowego; // millis() ostatniej zmiany surowego odczytu
    bool czekaNaPuszczenie;       // po starcie: wejście ignorowane, aż filtr potwierdzi puszczenie
    bool odczytajWejscie();
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
         * @brief Wykonuje funkcję przycisku na podstawie kopii jej danych (nie wskaźnika na Command,
         * który może zostać zwolniony w przerwaniu I2C w trakcie wykonywania).
         *
         * @param typ typ komendy (zmiana stanu przekaźnika / rolety)
         * @param idUrzadzenia id urządzenia docelowego na slave-ie
         * @param typUrzadzenia typ urządzenia docelowego
         * @param parametry parametry komendy (8 bajtów)
         */
        bool runCommand(Command::KOMENDY typ, byte idUrzadzenia, Device::TYPE typUrzadzenia, const byte *parametry);
};

#endif // !Wylacznik_h
