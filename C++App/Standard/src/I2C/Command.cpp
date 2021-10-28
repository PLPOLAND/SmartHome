#include "Command.h"

Command::Command()
{

}
Command::Command(const Command * command)
{
    this->id_slave = command->id_slave;
    this->komenda = command->komenda;
    this->urzadzenie = new Device(*(command->urzadzenie));
    this->parametry = String(command->parametry);
}

Command::~Command()
{
    delete this->urzadzenie;
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
                this->komenda = Command::KOMENDY::RECEIVE_ADD_THERMOMETR;
            }
        }
        else if (c[0] == 'T')
        {
            OUTPUT_LN(F("Command_conver:TX"));
            urzadzenie = new Device();//wskaźnik na urządzenie
            urzadzenie->setId(c[1]);//ustawienie ID urządzenia którego dotyczy komenda
            urzadzenie->setType(Device::TYPE::TERMOMETR);//Ustawienie typu urządzenia którego dotyczy komenda
            this->komenda = Command::KOMENDY::RECEIVE_GET_TEMPERATURE;
        }

        break;
    default:
        break;
    }
    // return Komendy::NIC;
}

void Command::setUrzadzenie(Device * u){
    this->urzadzenie = new Device(*(u));
}
