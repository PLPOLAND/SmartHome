#include "Command.h"

Command::Command()
{

}
Command::Command(const Command * command)
{
    this->id_slave = command->id_slave;
    this->komenda = command->komenda;
    this->urzadzenie = new Device(*(command->urzadzenie));
    memcpy(this->parametry, command->parametry, 8*sizeof(byte));
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
            OUTPUT_LN(F("Command_convert:TX"));
            urzadzenie = new Device();//wskaźnik na urządzenie
            urzadzenie->setId(c[1]-'0');//ustawienie ID urządzenia którego dotyczy komenda
            urzadzenie->setType(Device::TYPE::TERMOMETR);//Ustawienie typu urządzenia którego dotyczy komenda
            this->komenda = Command::KOMENDY::RECEIVE_GET_TEMPERATURE;
        }

        break;
    default:
        break;
    }
    // return Komendy::NIC;
}

//Wypisuje kolejne byte zmiennej parametry na ekran
void Command::printParametry(){
    OUTPUT("PARAMETRY: ")
    for (byte i = 0; i < 8; i++)
    {
        OUTPUT(this->parametry[i]);
        OUTPUT(" ");
    }
    OUTPUT_LN("");
    
}

byte Command::getSlaveID(){
    return this->id_slave;
}

Device *Command::getDevice(){
    return this->urzadzenie;
}

byte *Command::getParams(){
    return this->parametry;
}

Command::KOMENDY Command::getCommandType(){
    return this->komenda;
}

void Command::setDevice(Device * u){
    this->urzadzenie = new Device(*(u));
}
void Command::setParams(const byte *param){
    for (byte i = 0; i < 8; i++)
    {
        this->parametry[i] = param[i];
    }
    
}
void Command::setSlaveID(byte sId){
    this->id_slave = sId;
}
void Command::setCommandType(Command::KOMENDY komenda){
    this->komenda = komenda;
}
