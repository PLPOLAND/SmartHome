#include "Command.h"

Command::Command()
{

}

Command::~Command()
{

}

void Command::convert(const byte *c, byte size)
{
    switch (size)
    {
    case 1:
        break;
    case 2:
        if (c[0] == 'A')
        {
            if (c[1] == 'T')
            {
                this->komenda = Command::KOMENDY::ADD_THERMOMETR;
            }
        }
        else if (c[0] == 'T')
        {
            // OUTPUT_LN(F("Temperatura"));
            // idTermometru = c[1];
            urzadzenie = new Device();
            urzadzenie->setId(c[1]);
            urzadzenie->setType(Device::TYPE::TERMOMETR);
            this->komenda = Command::KOMENDY::GET_TEMPERATURE;
        }

        break;
    default:
        break;
    }
    // return Komendy::NIC;
}
