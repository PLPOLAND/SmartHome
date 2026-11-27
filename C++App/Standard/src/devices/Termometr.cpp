#include <devices/Termometr.h>
OneWire Termometr::oneWire = OneWire(ONEWIRE_BUS);
DallasTemperature Termometr::sensors = DallasTemperature(&oneWire);
byte Termometr::inSystem = 0;
byte Termometr::lastSensorsCount =0;
System* Termometr::system = System::getSystem();
LinkedList<byte *> Termometr::adressesOfFreeThermometrs = LinkedList<byte*>();

void copyAdress(byte* from, byte* to){
    for (byte i = 0; i < 8; i++)
    {
        to[i] = from[i];
    }
    
}

Termometr::Termometr() : Device(TYPE::TERMOMETR)
{
    OUT_LN("TERMOMETR()");
    
    timer.restart();
}

Termometr::Termometr(byte id) : Device(TYPE::TERMOMETR, id)
{
    timer.restart();
}

Termometr::~Termometr(){}

Termometr::Termometr(const Termometr &t) : Device((Device)t)
{
    // OUT_LN(F("COPY TERMO"))
    memcpy(this->adress,t.adress,8);
    this->temperatura = t.temperatura;
    timer.restart();
}

//Returns byte[8]
const byte* Termometr::getAddres(){
    return this->adress;
}

String Termometr::getAddresAsString(){
    String tmp;
    for (byte i = 0; i < 8; i++)
    {
        tmp+= this->adress[i];
        tmp+=" ";
    }
    

    return tmp;
}

//Zwraca temeraturę czujnika
float Termometr::getTemperature(){
    return temperatura;
}

bool Termometr::isCorrect(){//TODO usunąć albo poprawić
    if (this->getId() == -1)
        return false;
    return true;
    
}

///skonfiguruj termometr
///@return true jeśli udała się poprawna konfiguracja; false jeśli niema już więcej wolnych termometrów w systemie;
bool Termometr::begin()
{
    sensors.begin();
    timer.begin(1);
    byte tmpAdress[8]; // adress of termometr to add
    if (system->howManyThermometers() != sensors.getDeviceCount() && sensors.getDeviceCount() == 1)
    {
        OUT_LN("ONE TER")
        sensors.getAddress(tmpAdress,0);
        copyAdress(tmpAdress, this->adress);
        OUT_LN("TER addr:")
        for (byte i = 0; i < 8; i++)
        {
            OUT(" ");
            OUT(this->adress[i]);
        }
        OUT_LN();
        return true;
    }
    else{
        return false;
    }
    if (system->howManyThermometers()<sensors.getDeviceCount())//TODO SPRAWDZIĆ DZIAŁANIE
    {
        LinkedList<Termometr*>* termometry =  &(system->termometry);

        bool found = false;
        for (byte i = 0; i < sensors.getDeviceCount() && found == false; i++)
        {
            found = false;
            byte currAdress[8];
            sensors.getAddress(currAdress,i);
            for (byte j = 0; j < termometry->size(); j++)
            {
                if (compare2Adresses(currAdress,termometry->get(j)->getAddres()))
                {
                    found = true;
                    break;
                }
                
            }
            if (found == false)
            {
                copyAdress(currAdress, tmpAdress);//znaleziono nowy termometr. zapisz jego adres do zmiennej tymczasowej
            }
            
            
        }
        if (!found)
        {
            return false;
        }
        else
        {
            copyAdress(tmpAdress,this->adress);
            // OUT_LN(F("therm added ok"))
            return true;
        }
        
        

    }
    return false;
}
//uaktualnij temperaturę termometru
void Termometr::updateTemperature(){
    // OUT_LN(F("updateTemperature"))
    // OUT(F("Time(")) OUT(timer.time()) OUT_LN(F(")"))
    if (timer.available())
    {
        // OUT(F("termometr o id: "))
        // OUT_LN(this->getId());
        sensors.setWaitForConversion(false); // makes it async
        sensors.requestTemperatures();
        sensors.setWaitForConversion(true);
        // OUT_LN(F("updatingTemperature"))
        // for (byte i = 0; i < 8; i++)
        // {
        //     OUT(" ");
        //     OUT(this->adress[i]);
        // }
        // if (sensors.isConnected(this->getAddres()))
        //     OUT_LN("CONNECTED")
        this->temperatura = sensors.getTempC(this->getAddres());
        // OUT(F("temperature = "));
        // OUT_LN(this->temperatura);
        timer.begin(1000);
    }
}
bool Termometr::compare2Adresses(const byte *addr1, const byte *addr2){
    for (int i = 0; i < 8; i++)
    {
        if (addr1[i] != addr2[i])
        {
            // OUT(addr1[i]) OUT(" != ") OUT(addr2[i]) OUT_LN()
            return false;
        }
        // OUT(addr1[i]) OUT(" = ") OUT(addr2[i]) OUT_LN()
    }
    return true;
    
}

uint8_t Termometr::howManyThermometers(){
    return sensors.getDeviceCount();
}
void Termometr::init(){
    sensors.begin();
}