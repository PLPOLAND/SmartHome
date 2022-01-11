#ifndef COMMAND_H
#define COMMAND_H
#include <Arduino.h>
#include "devices/Device.h"
#include "Stale.h"

class Command
{
public:
    enum class KOMENDY
    {
        NIC,
        //Odbieranie
        RECEIVE_ADD_THERMOMETR,  //Dodaj nowy termometr do systemu
        RECEIVE_ADD_ROLETA,      //Dodaj nową roletę do systemu
        RECEIVE_ADD_PRZYCISK,    //Dodaj nowy przycisk do systemu
        RECEIVE_ADD_PRZEKAZNIK,  //Dodaj nowy przekaznik do systemu
        RECEIVE_GET_TEMPERATURE, //Pobierz temperaturę z termometru
        RECEIVE_ZMIEN_STAN_PRZEKAZNIKA,      //Zmien stan konkretnego urzadzenia
        RECEIVE_ZMIEN_STAN_ROLETY, // Podnies opuść konkretną roletę.
        RECEIVE_IS_INIT,         //Czy urządzenie zostało zainicjowane
        RECEIVE_INIT,            //reinicjalizuj system
        RECEIVE_GET,             //Wczytaj następny z kolejki

        //Wysylanie
        SEND_REPLY,      //Odpowiedz z zapisanymi danymi w bufforze
        SEND_STATUS,     //Wyślij status urządzenia
        SEND_TEMPERATURA //Odpowiedz z temperatura według szablonu
    };

private:
    byte id_slave;      //wykorzystywane przy przesyłaniu komendy do innego urządzenia
    Device *urzadzenie; //urządzenie docelowe
    byte parametry[8];  //dodatkowe parametry
    KOMENDY komenda;

public:
    Command();
    // Command(Command *command);
    ~Command();

    /**
    *   Konwertuje otrzymany ciąg byte-ów na Komendę. 
    *
    *@param c ciąg znaków do konwersji
    *@param size ilość znaków do konwersjii
     */
    void convert(const byte *c, byte size);

    byte getSlaveID();
    Device *getDevice();
    byte *getParams();
    KOMENDY getCommandType();

    void setSlaveID(byte sId);
    void setDevice(Device *u);
    void setParams(const byte *param);
    void setCommandType(KOMENDY komenda);

    void makeCopy(Command *command);
    /**
    *   
     */
    void printParametry();

    // void operator delete(void* ptr){
    //     OUT_LN("XXX")
    //     ((Command*)ptr)->~Command();
    //     free(ptr);
    //     OUT_LN("|XXX")
    // }
};

#endif // !COMMAND_H
