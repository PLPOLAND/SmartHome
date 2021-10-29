#ifndef COMMAND_H
#define COMMAND_H
#include <Arduino.h>
#include "devices/Device.h"
#include "Stale.h"


class Command
{
private:
    enum class KOMENDY;
    byte id_slave; //wykorzystywane przy przesyłaniu komendy do innego urządzenia
    Device *urzadzenie;//urządzenie docelowe
    byte parametry[8];//dodatkowe parametry
    KOMENDY komenda;
    
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

    Command();
    Command(const Command * command);
    ~Command();
    
    void convert(const byte *c, byte size);


    byte getSlaveID();
    Device* getDevice();
    byte* getParams();
    KOMENDY getCommandType();
    
    void setSlaveID(byte sId);
    void setDevice(Device * u);
    void setParams(const byte* param);
    void setCommandType(KOMENDY komenda);

    void printParametry();
};



#endif // !COMMAND_H
