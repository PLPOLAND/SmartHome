#ifndef I2CCONVERTER_H
#define I2CCONVERTER_H

#include "Arduino.h"
#include "Wire.h"
#include <Kontener.h>
#include <devices/Termometr.h>
#include <devices/Przekaznik.h>
#include "LinkedList.h"
#include "Command.h"
// #include "System.h"6


#define DEBUG //Wyswietl informacje debugowania //NOT USED YET

#define PINOW_NA_ADRES 6

//BUFFORY
#define BUFFOR_IN_SIZE 5
#define BUFFOR_OUT_SIZE 8


// class System;



class I2CConverter
{
protected:
    I2CConverter();
    ~I2CConverter();
    static I2CConverter* singleton;
    static LinkedList<Command*> doWyslania;

public:
    I2CConverter(I2CConverter &other) = delete;
    void operator=(const I2CConverter &) = delete;
    static I2CConverter* getInstance();

    static void onRecieveEvent(int howManyBytes);// funkcja do rejestracji - wykonuje się przy przesyłaniu danych z Raspi/mastera
    static void onRequestEvent();// Funkcja do rejestracji - wykonuje się przy przesyłaniu na Raspi/mastera

    void RecieveEvent(int howManyBytes);//Funkcja wywyłwana przez onRecieveEvent(int howManyBytes);
    void RequestEvent();//Funkcja wywoływana przez onRequestEvent();


    void begin();
    bool isWorkToDo = false;

    byte buf[BUFFOR_IN_SIZE];//buffor wejsciowy
    byte buf_out[BUFFOR_OUT_SIZE];//buffor wyjsciowy

    

    // Komendy find_command(byte size);
    
    /// Wysyła temperaturę z termometru
    void printTemperature(byte id);
    



};

#endif // !I2CCONVERTER_H