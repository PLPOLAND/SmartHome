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
    NIC,//Brak danych do wysłania
    REPLY,//Odpowiedz z zapisanymi danymi w bufforze
    STATUS,
    TEMPERATURA//Odpowiedz z temperatura według szablonu
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

    static void onRecieveEvent(int howManyBytes);// funkcja do rejestracji - wykonuje się przy przesyłaniu danych z Raspi/mastera
    static void onRequestEvent();// Funkcja do rejestracji - wykonuje się przy przesyłaniu na Raspi/mastera

    void RecieveEvent(int howManyBytes);//Funkcja wywyłwana przez onRecieveEvent(int howManyBytes);
    void RequestEvent();//Funkcja wywoływana przez onRequestEvent();
public:

    DoWyslania coWyslac = DoWyslania::NIC;//TODO: zrobić kolejkę do wysyłania danych
    bool isWorkToDo = false;

    byte buf[BUFFOR_IN_SIZE];//buffor wejsciowy
    byte buf_out[BUFFOR_OUT_SIZE];//buffor wyjsciowy

    byte idTermometru = 0;//Id termometru do wypisania

    LinkedList<Termometr*> termometry;
    LinkedList<Przekaznik*> przekazniki;

    Komendy find_command(byte size);

    void printTemperature(byte id);
    void addTermometr();
};

#endif // !I2CCONVERTER_H