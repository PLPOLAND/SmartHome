#ifndef COMMAND_H
#define COMMAND_H
#include <Arduino.h>
#include "devices/Device.h"
#include "Stale.h"


class Command
{
private:
    
public:
    enum class KOMENDY
    {
        NIC,
        //Odbieranie
        RECEIVE_ADD_THERMOMETR,
        RECEIVE_ADD_ROLETA,
        RECEIVE_ADD_PRZYCISK,
        RECEIVE_GET_TEMPERATURE,
        RECEIVE_ZMIEN_STAN,

        //Wysylanie
        SEND_REPLY, //Odpowiedz z zapisanymi danymi w bufforze
        SEND_STATUS,
        SEND_TEMPERATURA //Odpowiedz z temperatura według szablonu
    };
    byte id_slave;//wykorzystywane przy przesyłaniu komendy do innego urządzenia
    Device *urzadzenie;//urządzenie docelowe
    String parametry;//dodatkowe parametry
    KOMENDY komenda;

    Command();
    Command(const Command * command);
    ~Command();
    
    void convert(const byte *c, byte size);
    void setUrzadzenie(Device * u);
};



#endif // !COMMAND_H
