#ifndef SYSTEM_H
#define SYSTEM_H

#include <Arduino.h>
#include <Timers.h>
#include <Wire.h>
#include "FreeMemory.h"
#include "Stale.h"
#include "devices/Przekaznik.h"
#include "devices/Przycisk.h"
#include "devices/Roleta.h"
#include "I2C/I2CConverter.h"
#include "I2C/I2CAnything.h"

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
    byte idDevice = 0;

    static LinkedList<Device *> devices;
    static LinkedList<Termometr *> termometry;
    static LinkedList<Przekaznik *> przekazniki;
    static LinkedList<Przycisk *> przyciski;
    static LinkedList<Roleta *> rolety;

public:
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
    

    ///Zwraca listę adresów termometrów zarejestrowanych w systemie
    LinkedList<byte*> getAdrOfThemp();

};


#endif // !SYSTEM_H