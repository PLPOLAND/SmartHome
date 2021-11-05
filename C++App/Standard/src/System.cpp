#include "System.h"

System* System::system = nullptr;
Timer System::timer = Timer();
bool System::is_initiated = false;

LinkedList<Device *> System::devices = LinkedList<Device *>();
LinkedList<Termometr *> System::termometry = LinkedList<Termometr*>();
LinkedList<Przekaznik *> System::przekazniki = LinkedList<Przekaznik*>();
LinkedList<Przycisk *> System::przyciski = LinkedList<Przycisk*>();
LinkedList<Roleta *> System::rolety = LinkedList<Roleta*>();

System *System::getSystem()
{
    if (system == nullptr)
    {
        system = new System();
        // OUT_LN("getSystem System()");
    }

    // OUT_LN("getSystem()");
    return system;
}
System::System()
{
    Serial.begin(115200); // start serial for output
    OUT_LN(freeMemory());
    OUT_LN("SerialStarted");
}

System::~System(){
    termometry.clear();
    przekazniki.clear();
    przyciski.clear();
}

void System::begin(){
    // Serial.begin(115200); // start serial for output
    OUT_LN(freeMemory());
    OUT_LN("SerialStarted");
    comunication = I2CConverter::getInstance();
    comunication->begin();
    OUT_LN("comunication->begin();");
    Wire.onReceive(I2CConverter::onRecieveEvent);
    OUT_LN("Wire.onReceive(I2CConverter::onRecieveEvent);");
    Wire.onRequest(I2CConverter::onRequestEvent);
    OUT_LN("Wire.onReceive(I2CConverter::onRequestEvent);");
    timer.begin(MINS(CZAS_ODSWIERZANIA_TEMPERATURY));
    OUT_LN("timer.begin(MINS(CZAS_ODSWIERZANIA_TEMPERATURY));");
    OUT_LN(freeMemory());
}

void System::tic(){
    if (this->termometry.size() > 0 && timer.available()) //TODO ustawić częstotliwość sprawdzania!
    {
        for (byte i = 0; i < this->termometry.size(); i++)
        {
            this->termometry.get(i)->updateTemperature();
            OUT("Termometr: ");
            OUT(i);
            OUT(" = ");
            OUT_LN(this->termometry.get(i)->getTemperature());
        }
        OUT_LN("timer");
        timer.begin(MINS(CZAS_ODSWIERZANIA_TEMPERATURY));
    }

}

Device* System::addDevice(Device::TYPE typeOfDevice, byte pin1, byte pin2){
    switch (typeOfDevice)
    {
        case Device::TYPE::BRAK:
        break;

        case Device::TYPE::TERMOMETR:{
            Termometr *tmp = (Termometr *)malloc(sizeof(Termometr));
            if (tmp->begin())
            { //spr. skonfigurować kolejny termometr
                tmp->setId(idDevice++);
                this->devices.add(tmp);    //dodaj do głównej listy urządzeń
                this->termometry.add(tmp); //dodaj do listy termometrów w systemie
                return tmp;
            }
            else
            {
                delete tmp;
                return nullptr; //TODO Poprawić
            }
            break;
        }
        case Device::TYPE::PRZEKAZNIK:
            {
                Przekaznik * tmp = new Przekaznik();
                if(tmp->begin(pin1, false)){//jeśli uda się poparawnie dodać przekaźnik do systemu
                    tmp->setId(idDevice++);//nadaj mu id
                    this->devices.add(tmp->getId(), tmp);//dodaj do listy urzadzen
                    this->przekazniki.add(tmp);//dodaj do listy urzadzen
                    return tmp;
                }
                else{
                    return nullptr;
                }
            }
            break;
        case Device::TYPE::PRZYCISK:
            {
                OUT_LN("-----Dodaj Przycisk-----")
                OUT("pin1:")
                OUT_LN(pin1);
                Przycisk * tmp = new Przycisk();
                if(tmp->begin(pin1)){//jeśli uda się poparawnie dodać przekaźnik do systemu
                    OUT_LN("Poprawnie dodano Przycisk")
                    tmp->setId(idDevice++);//nadaj mu id
                    OUT("id:")
                    OUT_LN(tmp->getId());
                    this->devices.add(tmp->getId(), tmp);//dodaj do listy urzadzen
                    this->przyciski.add(tmp);//dodaj do listy urzadzen
                    return tmp;
                }
                else{
                    OUT_LN("Nie udało dodać Przycisku")
                    return nullptr;
                }
            }
            break;
        case Device::TYPE::PRZYCISK_ROLETA:
            
            break;
        case Device::TYPE::ROLETA:
            
            break;
        
        default:
            break;
    }
    return nullptr;//TODO poprawić???
}
bool System::removeDevice(byte id){
    Device* tmp = devices.get(id);
    switch (tmp->getType())
    {
    case Device::TYPE::TERMOMETR:
        for (byte i = 0; i < termometry.size(); i++)//przeszukaj kontener z termometrami
        {
            if (termometry.get(i)->getId() == id)//jeśli id obecnie sprawdzanego termometru zgadza się z poszukiwanym to:
            {
                termometry.remove(i);//usuń z konternera termomotrów
                devices.remove(i);//usuń z kontenera urządzeń
                delete tmp;//usuń obiekt
                tmp = nullptr;//na wszelki wypadek
                break;//przestań szukać
            }
        }
        
        break;
    case Device::TYPE::PRZEKAZNIK:
        for (byte i = 0; i < przekazniki.size(); i++) //przeszukaj kontener z przekaznikami
        {
            if (przekazniki.get(i)->getId() == id) //jeśli id obecnie sprawdzanego przekaznika zgadza się z poszukiwanym to:
            {
                przekazniki.remove(i); //usuń z konternera przekaznikow
                devices.remove(i);    //usuń z kontenera urządzeń
                delete tmp;           //usuń obiekt
                tmp = nullptr;        //na wszelki wypadek
                break;                //przestań szukać
            }
        }
        break;
    case Device::TYPE::PRZYCISK:
    case Device::TYPE::PRZYCISK_ROLETA:
        for (byte i = 0; i < przyciski.size(); i++) //przeszukaj kontener z przyciskami
        {
            if (przyciski.get(i)->getId() == id) //jeśli id obecnie sprawdzanego przycisku zgadza się z poszukiwanym to:
            {
                przyciski.remove(i);    //usuń z konternera przyciskow
                devices.remove(i);      //usuń z kontenera urządzeń
                delete tmp;             //usuń obiekt
                tmp = nullptr;          //na wszelki wypadek
                break;                  //przestań szukać
            }
        }
        break;
    case Device::TYPE::ROLETA:
        for (byte i = 0; i < rolety.size(); i++) //przeszukaj kontener z przyciskami
        {
            if (rolety.get(i)->getId() == id) //jeśli id obecnie sprawdzanego przycisku zgadza się z poszukiwanym to:
            {
                rolety.remove(i);       //usuń z konternera rolet
                devices.remove(i);      //usuń z kontenera urządzeń
                delete tmp;             //usuń obiekt
                tmp = nullptr;          //na wszelki wypadek
                break;                  //przestań szukać
            }
        }
        break;
    }
    return true;
}
Device* System::getDevice(byte id){
    return devices.get(id);
}

LinkedList<byte*> System::getAdrOfThemp(){
    LinkedList<byte*> adresy;
    for (byte i = 0; i < termometry.size(); i++)
    {
        byte* tmp = new byte[8];
        for (byte i = 0; i < 8; i++)
        {
            tmp[i] = (int)((termometry.get(i)->getAddres())[i]);
        }
        
        adresy.add(tmp);    
    }
    return adresy;
};

void System::reinit_system(){
    for (byte i = 0; i < devices.size(); i++)
    {
        delete devices[i];//usuń wszystkie urządzenia w systemie
    }

    //oczyść wszystkie listy urządzeń
    devices.clear();
    przekazniki.clear();
    rolety.clear();
    termometry.clear();
    przyciski.clear();
    init_system();

}

void System::init_system(){
    is_initiated = true;
}

bool System::is_init(){
    return is_initiated;
}
