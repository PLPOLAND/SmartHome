#ifndef TERMOMETR_H
#define TERMOMETR_H
#include <Arduino.h>
#include <DallasTemperature.h>
#include <LinkedList.h>
#include "devices/Device.h"
#include "System.h"

//pin komunikacji oneWire
#define ONEWIRE_BUS 8 

class System;

class Termometr : public Device
{
private:
    //Zmienna statyczna przechowująca obiekt do obsługi komunikacji w standardzie OneWire
    static OneWire oneWire;
    static DallasTemperature sensors;
    static byte inSystem;//liczba termometrów dotychczas zarejestrowana w systemie
    static byte lastSensorsCount;//ostatnia liczba termometrów wykrytych w systemie
    static LinkedList<byte*> adressesOfFreeThermometrs;
    static System* system;
    byte adress[8]; //adres termometru (tablica[8])
    float temperatura;
    bool compare2Adresses(const byte* addr1,const byte* addr2);
public:
    Termometr();
    Termometr(byte id);
    ~Termometr();
    bool begin();

    const byte* getAddres();

    float getTemperature();
    bool isCorrect();
    void updateTemperature();
};

#endif // !TERMOMETR_H