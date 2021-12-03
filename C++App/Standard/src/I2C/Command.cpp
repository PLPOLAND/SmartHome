#include "Command.h"

Command::Command()
{
    this->urzadzenie = nullptr;
    this->komenda = KOMENDY::NIC;
}
// Command::Command(Command * command)
// {
//     this->id_slave = command->id_slave;
//     this->komenda = command->komenda;
//     this->urzadzenie = new Device(*(command->urzadzenie));
//     memcpy(this->parametry, command->parametry, 8*sizeof(byte));
// }

Command::~Command()
{
    // OUT_LN(F("START ~Command()"))
    if (this->urzadzenie != nullptr)
    {
        // OUT_LN(F("delete urzadzenie"))
        delete this->urzadzenie;
        // OUT_LN(F("END delete urzadzenie"))
    }

    // OUT_LN(F("END OF ~Command()"))
}

void Command::makeCopy(Command *command)
{
    this->id_slave = command->id_slave;
    this->komenda = command->komenda;
    this->urzadzenie = new Device(*(command->urzadzenie));
    for (int i = 0; i < 8; i++)
    {
        parametry[i] = command->parametry[i];
    }

}
void Command::convert(const byte *c, byte size)
{
    switch (size)
    {
    case 1:{
        if (c[0] == 'I')
            this->komenda = Command::KOMENDY::RECEIVE_IS_INIT;
        if (c[0] == 'W')
            this->komenda = Command::KOMENDY::RECEIVE_GET;
        if (c[0] == 'R')
            this->komenda = Command::KOMENDY::RECEIVE_INIT;
        if (c[0] == 0)
            this->komenda = Command::KOMENDY::NIC;
        }
        break;
    case 2:
        if (c[0] == 'A')
        {
            if (c[1] == 'T')//Add Thermomentr
            {
                this->komenda = Command::KOMENDY::RECEIVE_ADD_THERMOMETR;
            }
        }
        else if (c[0] == 'T')//GetTemperature
        {
            OUT_LN(F("Command_convert:TX"));
            urzadzenie = new Device();//wskaźnik na urządzenie
            urzadzenie->setId(c[1]-'0');//ustawienie ID urządzenia którego dotyczy komenda
            urzadzenie->setType(Device::TYPE::TERMOMETR);//Ustawienie typu urządzenia którego dotyczy komenda
            this->komenda = Command::KOMENDY::RECEIVE_GET_TEMPERATURE;
        }

        break;
    case 3:
        {
            if (c[0] == 'A')
            {
                if (c[1] == 'S')// Dodaj przekaźnik
                {
                    OUT_LN(F("case 3, AS"))
                    this->komenda = Command::KOMENDY::RECEIVE_ADD_PRZEKAZNIK;
                    byte parametry[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                    parametry[0] = c[2];//nr pinu urzadzenia
                    OUT("pin = ")
                    OUT_LN((int)c[2]);
                    this->setParams(parametry);
                }
                else if (c[1] == 'P')//Dodaj Przycisk zwykły
                {
                    OUT_LN(F("case 3, AP"))
                    this->komenda = Command::KOMENDY::RECEIVE_ADD_PRZYCISK;
                    byte parametry[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                    parametry[0] = c[2];//nr pinu urzadzenia
                    OUT("pin = ")
                    OUT_LN((int)c[2]);
                    this->setParams(parametry);
                }
                
            }
            
        }
        break;
    case 4:
        {
            if (c[0] == 'A')
            {
                if (c[1] == 'R')// Dodaj roleta
                {
                    this->komenda = Command::KOMENDY::RECEIVE_ADD_ROLETA;
                    byte parametry[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                    parametry[0] = c[2];//nr pinu up
                    parametry[1] = c[3];//nr pinu down
                    this->setParams(parametry);
                }
            }
        }
        break;
    case 5:
        {

        }
        break;
    case 6:
        {

        }
        break;
    case 7:
        {

        }
        break;
    case 8:
        {

        }
        break;
    
    default:
        this->komenda = Command::KOMENDY::NIC;
        break;
    }
    // return Komendy::NIC;
}

//Wypisuje kolejne byte zmiennej parametry na ekran
void Command::printParametry(){
    OUT("PARAMETRY: ")
    for (byte i = 0; i < 8; i++)
    {
        OUT(this->parametry[i]);
        OUT(" ")
    }
    OUT_LN(F(""))
    
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
void Command::setParams(byte *param){
    for (byte i = 0; i < 8; i++)
    {
        this->parametry[i] = param[i];
    }
    OUT_LN()
}
void Command::setSlaveID(byte sId){
    this->id_slave = sId;
}
void Command::setCommandType(Command::KOMENDY komenda){
    this->komenda = komenda;
}
