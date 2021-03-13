#include "Arduino.h"
#include "Wire.h"
#include <Kontener.h>
#include <Termometr.h>
#include <Przekaznik.h>
#include "LinkedList.h"

#ifndef I2CCONVERTER_H
#define I2CCONVERTER_H

#define DEBUG //Wyswietl informacje debugowania

#define PINOW_NA_ADRES 6
//BUFFORY
#define BUFFOR_IN_SIZE 5
#define BUFFOR_OUT_SIZE 8

enum class DoWyslania
{
    NIC,
    REPLY,
    STATUS,
    TEMPERATURA
};
enum class Komendy {
    NIC,
    DODAJ_TERMOMETR,
    TEMPERATURA
 };

class I2CConverter
{
protected:
    I2CConverter();
    ~I2CConverter();
    static I2CConverter* singleton;
public:
    I2CConverter(I2CConverter &other) = delete;
    void operator=(const I2CConverter &) = delete;
    static I2CConverter* getInstance();

    static void onRecieveEvent(int howManyBytes);
    static void onRequestEvent();

    void RecieveEvent(int howManyBytes);
    void RequestEvent();
public:

    DoWyslania coWyslac = DoWyslania::NIC;
    bool isWorkToDo = false;

    byte buf[BUFFOR_IN_SIZE];
    byte buf_out[BUFFOR_OUT_SIZE];
    LinkedList<Termometr*> termometry;
    // Kontener<Termometr*>* termometry;
    // Kontener<Przekaznik*>* przekazniki;

    Komendy find_command(byte size);

    void printTemperature(byte id);
    void addTermometr();
};

#endif // !I2CCONVERTER_H