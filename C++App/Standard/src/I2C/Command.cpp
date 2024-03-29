#include "Command.h"
#include "devices/Termometr.h"

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
    // OUT(F("Rozmiar wejscia Komendy:"))
    // OUT_LN(size);
    // OUT(F("c[0] = "))
    // OUT_LN(c[0]);
    // for (size_t i = 0; i < size ; i++)
    // {
    //     OUT("i: ");
    //     OUT(i)
    //     OUT(" buf: ")
    //     if (c[i]>='A' && c[i] <='Z')
    //     {
    //         OUT_LN((char)c[i])
    //     }
    //     else{
    //         OUT_LN(c[i])
    //     }
        
    // }
    
    switch (size)
    {
    case 1:{
        if (c[0] == 'I')
            this->komenda = Command::KOMENDY::RECEIVE_CHECK_INIT;
        if (c[0] == 'W')
            this->komenda = Command::KOMENDY::RECEIVE_CHECK_HOW_MANY_TO_SENT;
        if (c[0] == 'G')
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

        break;
    case 3:
        {
            if (c[0] == 'A')
            {
                if (c[1] == 'S')// Dodaj przekaźnik
                {
                    // OUT_LN(F("case 3, AS"))
                    this->komenda = Command::KOMENDY::RECEIVE_ADD_PRZEKAZNIK;
                    byte parametry[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                    parametry[0] = c[2];//nr pinu urzadzenia
                    // OUT("pin = ")
                    // OUT_LN((int)c[2]);
                    this->setParams(parametry);
                }
                else if (c[1] == 'P')//Dodaj Przycisk zwykły
                {
                    // OUT_LN(F("case 3, AP"))
                    this->komenda = Command::KOMENDY::RECEIVE_ADD_PRZYCISK;
                    byte parametry[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                    parametry[0] = c[2];//nr pinu urzadzenia
                    // OUT("pin = ")
                    // OUT_LN((int)c[2]);
                    this->setParams(parametry);
                }
                
            }
            else if (c[0] == 'S')
            {
                if (c[1]=='D')//Status urządzenia
                {
                    //OUT_LN(F("---"))
                    //OUT_LN(F("SD"))
                    //OUT_LN(F("---"))
                    this->komenda = Command::KOMENDY::RECIEVE_DEVICES_STATUS;
                    byte parametry[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                    parametry[0] = c[2]; // id urzadzenia
                    // OUT("device id = ");
                    // OUT_LN((int)c[2]);
                    this->setParams(parametry);
                }
                
            }
            else if(c[0] == 'C'){
                if (c[1] == 'T')
                {
                    if (c[2] == 'N')
                    {
                        this->komenda = Command::KOMENDY::RECEIVE_HOW_MANY_THERMOMETR;
                        OUT_LN("CTN");
                    }
                    
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
            else if (c[0] == 'U')
            {
                if (c[1] == 'S')
                {
                    this->komenda = Command::KOMENDY::RECEIVE_ZMIEN_STAN_PRZEKAZNIKA;
                    byte parametry[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                    parametry[0] = c[2]; //ID switcha
                    parametry[1] = c[3]; //stan do ustawienia
                    this->setParams(parametry);
                }
                if (c[1] == 'B')
                {
                    this->komenda = Command::KOMENDY::RECEIVE_ZMIEN_STAN_ROLETY;
                    byte parametry[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                    parametry[0] = c[2];
                    parametry[1] = c[3];
                    this->setParams(parametry);
                }
                
                
            }
            
        }
        break;
    case 5:
        {
            if (c[0] == 'P')
            {
                if (c[1] == 'K')
                {
                    if (c[2] == 'L')
                    {
                        if(c[3] == 'D')
                        this->komenda = Command::KOMENDY::RECEIVE_REMOVE_PRZYCISK_LOCAL_FUNCTION;

                        byte parametry[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                        parametry[0] = c[4]; // ILE CLICK-ów
                        parametry[1] = c[5]; // ILE CLICK-ów
                        this->setParams(parametry);
                    }
                }
            }
        }
        break;
    case 6:
        {
            
        }
        break;
    case 7:
        {
            if (c[0] == 'P')
            {
                if (c[1] == 'K')
                {
                    if (c[2] == 'L')
                    {
                        this->komenda = Command::KOMENDY::RECEIVE_ADD_PRZYCISK_LOCAL_FUNCTION;

                        byte parametry[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                        parametry[0] = c[3]; // ID Przycisku
                        parametry[1] = c[4]; // ID Urządzenia
                        parametry[2] = c[5]; // STAN/U/D
                        parametry[3] = c[6]; // ILE CLICK-ów
                        this->setParams(parametry);
                    }
                }
            }
        }
        break;
    case 8:
        {

        }
        break;
    case 9:
        {
            if (c[0] == 'T')
            {
                this->komenda = Command::KOMENDY::RECEIVE_GET_TEMPERATURE;
                byte parametry[8] = {0,0,0,0,0,0,0,0};
                for (byte i = 0; i < 8; i++)
                {
                    parametry[i] = c[i+1];
                }
                this->setParams(parametry);
                
            }
            
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

Device* Command::getDevice(){
    return this->urzadzenie;
}

byte *Command::getParams(){
    return this->parametry;
}

Command::KOMENDY Command::getCommandType(){
    return this->komenda;
}

void Command::setDevice(Device * u){

    // if (u->getType() == Device::TERMOMETR)
    // {
    //     // OUT_LN("BEGIN OF COPY TEMP")
    //     this->urzadzenie = new Termometr(*(Termometr*)u);
    // }
    // else
    // {
    //     this->urzadzenie = new Device(*u);
    // }
    

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

String Command::toString(){
    String out = "";

    out+="Command Type: ";
    out+=String((int)this->getCommandType());
    out+='\n';

    out+="Device id: ";
    if (this->getDevice() != nullptr)
    {
        out += String(this->getDevice()->getId());
    }
    else
    {
        out += "null";
    }
    out += '\n';
    out += "Parametry: ";
    for (byte i = 0; i < 8; i++)
    {
        out += String(this->parametry[i]);
        out +=" ";
    }
    out +='\n';
    return out;
}
