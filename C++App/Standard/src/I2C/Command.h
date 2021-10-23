#ifndef COMMAND_H
#define COMMAND_H
#include <Arduino.h>
#include "devices/Device.h"


class Command
{
private:
    byte id_slave;//wykorzystywane przy przesyłaniu komendy do innego urządzenia
    Device *urzadzenie;//urządzenie docelowe
    String parametry;//dodatkowe parametry
    
public:
    enum class KOMENDY
    {
        NIC,
        ADD_THERMOMETR,
        ADD_ROLETA,
        ADD_PRZYCISK,
        GET_TEMPERATURE,
        ZMIEN_STAN,
    };

    Command();
    ~Command();
    KOMENDY komenda;//TODO
    void convert(const byte *c, byte size);
};



#endif // !COMMAND_H
