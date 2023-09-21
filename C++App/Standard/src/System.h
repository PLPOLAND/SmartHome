#ifndef SYSTEM_H
#define SYSTEM_H

#include <Arduino.h>
#include <EEPROM.h>
#include <Timers.h>
#include <Wire.h>
#include "FreeMemory.h"
#include "Stale.h"
#include "I2C/I2CConverter.h"
#include "I2C/I2CAnything.h"
#include "devices/Przekaznik.h"
#include "devices/Przycisk.h"
#include "devices/Roleta.h"
#include "devices/Termometr.h"
#include "devices/Higrometr.h"

//Główna klasa obsługująca "slave-a"
class I2CConverter;
class Termometr;

class System
{
private:
    I2CConverter *comunication;
    static Timer timer;
    static System * system;
    System();
    static bool is_initiated;

protected:
    byte idTermometru = 0; //Id termometru do wypisania
    byte idPrzycisku = 0; //Id przycisku do wypisania
    byte idPrzekaznika = 0; //Id Przekaznika do wypisania
    byte idRolety = 0; //Id Rolety do wypisania
    static byte idDevice;

    static LinkedList<Device *> devices;
    static LinkedList<Termometr *> termometry;
    static LinkedList<Higrometr *> higrometry;
    static LinkedList<Przekaznik *> przekazniki;
    static LinkedList<Roleta *> rolety;

public:
    friend class Termometr;
    static LinkedList<Przycisk *> przyciski;
    System(const System&) = delete;
    void operator= (const System &) = delete;
    ~System();
    void begin();
    static System* getSystem();
    //Usuwa wszystkie urządzenia w systemie, w celu ponownego zainicjalizowania systemu przez I2C
    static void reinit_system();
    //ustawaia flagę zainicjalizowania systemu na true
    static void init_system();
    //zwraca stan czy system został już zainicjowany przez I2C
    static bool is_init();

    //Główna funkcja obsługująca działanie systemu
    void tic();

    ///Dodaje urządzenie o podanym typie do systemu
    ///@return utworzone urzadzenie
    Device *addDevice(Device::TYPE typeOfDevice, byte pin1=0, byte pin2=0);

    /// usuwa urządzenie
    ///@param id ID urządzenia do usunięcia na płytce
    bool removeDevice(byte id);
    
    ///@return Wskaźnik do urządzenia o podanym ID;
    ///@param id ID urządzenia do znalezienia
    Device* getDevice(byte id);
    /**
     * @brief Zwraca termometr o adresie podanym w argumencie.
     * 
     * @param adress adres szukanego termometru
     * @return Device* - termometr o podanym adresie
     */
    Termometr *getTermometr(const byte *adress);

    ///Zwraca listę adresów termometrów zarejestrowanych w systemie
    LinkedList<byte*> getAdrOfThermometrs();

    /**
     * @brief Zwraca liczbę termoemtrów dodanych do systemu
     * 
     * 
     * @return byte 
     */
    byte howManyThermometers();

    // /**
    //  * @brief Wykonuje komendę dostarczoną w argumencie.
    //  * 
    //  * @param command 
    //  * @return true 
    //  * @return false 
    //  */
    // bool runCommand(Command *command);
    /**
     * @brief Get the Start Up Variant from EEPROM
     * 
     * @return true if startup should be without deafult Devices
     * @return false if startup should be with deafult Devices
     */
    bool getStartUpVariant();
    /**
     * @brief Set the Next Startup Variant in EEPROM
     *
     * @param variant true if next startup should be without deafult Devices
     */
    void setNextStartupVariant(bool variant);
};


#endif // !SYSTEM_H