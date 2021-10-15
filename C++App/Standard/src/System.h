#ifndef SYSTEM_H
#define SYSTEM_H

#include <Arduino.h>
#include <Timers.h>
#include <Wire.h>
#include "Stale.h"
#include "devices/Przekaznik.h"
#include "devices/Przycisk.h"
#include "devices/Roleta.h"
#include "I2C/I2CConverter.h"
#include "I2C/I2CAnything.h"
//Główna klasa obsługująca "slave-a"
class System
{
private:
    I2CConverter *comunication;
    Timer timer;
    static System * system;
    System();
protected:
    byte idTermometru = 0; //Id termometru do wypisania
    byte idPrzycisku = 0; //Id przycisku do wypisania
    byte idPrzekaznika = 0; //Id Przekaznika do wypisania
    byte idRolety = 0; //Id Rolety do wypisania

    LinkedList<Termometr *> termometry;
    LinkedList<Przekaznik *> przekazniki;
    LinkedList<Przycisk *> przyciski;
    LinkedList<Roleta *> rolety;

public:
    System(const System&) = delete;
    void operator= (const System &) = delete;
    ~System();
    static System* getSystem();

    //Główna funkcja obsługująca działanie systemu
    void tic();
    ///Dodaje termometr do systemu
    ///@return id na płytce
    byte addTermometr();
    ///Dodaje przycisk do systemu
    ///@return id na płytce
    byte addPrzycisk();
    ///Dodaje przekaźnik do systemu
    ///@return id na płytce
    byte addPrzekaznik();
    ///Dodaje roleta do systemu
    ///@return id na płytce
    byte addRoleta();

    /// usuwa termometr
    ///@param id - ID termometru do usunięcia na płytce
    bool removeTermometr(byte id);
    /// usuwa przycisk
    ///@param id - ID przycisku do usunięcia na płytce
    bool removePrzycisk(byte id);
    /// usuwa przekaźnik
    ///@param id - ID przekaźnika do usunięcia na płytce
    bool removePrzekaznik(byte id);
    /// usuwa roletę
    ///@param id - ID rolety do usunięcia na płytce
    bool removeRoleta(byte id);
    
    ///@return Wskaźnik do termometru o podanym ID;
    ///@param id - ID termometru do usunięcia na płytce
    Termometr* getTermometr(byte id);

    ///@return Wskaźnik do przycisku o podanym ID;
    ///@param id - ID przycisku do usunięcia na płytce
    Przycisk* getPrzycisk(byte id);

    ///@return Wskaźnik do przekaźnika o podanym ID;
    ///@param id - ID przekaźnika do usunięcia na płytce
    Przekaznik* getPrzekaznik(byte id);

    ///@return Wskaźnik do rolety o podanym ID;
    ///@param id - ID rolety do usunięcia na płytce
    Roleta* getRoleta(byte id);

};


#endif // !SYSTEM_H