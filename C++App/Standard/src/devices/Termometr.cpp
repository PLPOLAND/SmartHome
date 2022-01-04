#include <devices/Termometr.h>
OneWire Termometr::oneWire = OneWire(ONEWIRE_BUS);
DallasTemperature Termometr::sensors = DallasTemperature(&oneWire);
byte Termometr::inSystem = 0;
byte Termometr::lastSensorsCount =0;
System* Termometr::system = System::getSystem();
LinkedList<byte *> Termometr::adressesOfFreeThermometrs = LinkedList<byte*>();

Termometr::Termometr()
{
    Device(TYPE::TERMOMETR);
}

Termometr::~Termometr()
{
}

Termometr::Termometr(const Termometr &t){
    Device((Device)t); 
    OUT_LN("COPY TERMO")
    memcpy(this->adress,t.adress,8);
    this->temperatura = t.temperatura;

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

///skonfiguruj temperaturę
///@return true jeśli udała się poprawna konfiguracja; false jeśli niema już więcej wolnych termometrów w systemie;
bool Termometr::begin()
{
    sensors.begin();
    
    if (lastSensorsCount != sensors.getDeviceCount()){
        LinkedList<byte*> defined = system->getAdrOfThemp();
        for (int i = 0; i < sensors.getDeviceCount(); i++)// poszukaj nowych termometrów podlaczonych do sysytemu
        {
            bool found = false ;
            byte currentThempAddr[8];
            sensors.getAddress(currentThempAddr,i);//pobierz adres kolejnego urządzenia w sensors

            for (int j = 0; j < this->adressesOfFreeThermometrs.size(); j++)//sprawdź czy adres już nie istenieje w liście wolnych
            {
                if (compare2Adresses(adressesOfFreeThermometrs.get(j),currentThempAddr))
                {
                    found = true;
                    break;
                }
            }
            if (found == false)//nie znaleziono takego
            {
                for (int j = 0; j < defined.size(); i++)//wyszukaj więc wśród adresów termometrów już dodanych do systemu
                {
                    if (compare2Adresses(adressesOfFreeThermometrs.get(j), defined.get(j)))
                    {
                        found = true;
                        break;
                    }
                }
                if (found==false)//jeśli nadal nie znaleziono to dodaj adres do listy wolnych
                {
                    adressesOfFreeThermometrs.add(currentThempAddr);
                }
                   
            }
            
            
        }
        
    }
    OUT_LN("Termo_begin");
    if (this->inSystem < sensors.getDeviceCount()) {
        for (byte i = 0; i < 8; i++)
        {
            this->adress[i] = adressesOfFreeThermometrs.get(0)[i];
        }
        
        // this->adress = adressesOfFreeThermometrs.get(0);//przypisz pierwszy wolny adres
        adressesOfFreeThermometrs.remove(0);//usuń ten adres z listy wolnych adresów
        this->inSystem++;//zwiększ liczbę termometrów w systemie
        OUT_LN("Stworzono nowy termometr");
        OUT("Adres:");
        for (int i = 0; i < 8; i++)
        {
            OUT(this->adress[i]);
            OUT(" ");
        }
        OUT_LN();
        } else {
            OUT_LN("Błąd brak nowych termo");
            return false;
    }
    return true;
}
//uaktualnij temperaturę termometru
void Termometr::updateTemperature(){
    // OUT_LN("getT");
    // for (int i = 0; i < 8; i++)
    // {
    //     OUTPUT((int)(this->adress[i]));
    // }
    // sensors.requestTemperaturesByAddress(this->getAddres());
    sensors.requestTemperatures();
    // delay(sensors.millisToWaitForConversion(sensors.getResolution(this->getAddres())));
    temperatura=sensors.getTempC(this->getAddres());
    
    // OUT_LN("getTReq");
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