#include "System.h"

System* System::system = nullptr;
Timer System::timer = Timer();
bool System::is_initiated = false;
byte System::idDevice = 0;
LinkedList<Device *> System::devices = LinkedList<Device *>();
LinkedList<Termometr *> System::termometry = LinkedList<Termometr*>();
LinkedList<Przekaznik *> System::przekazniki = LinkedList<Przekaznik*>();
LinkedList<Przycisk *> System::przyciski = LinkedList<Przycisk*>();
LinkedList<Roleta *> System::rolety = LinkedList<Roleta*>();

System *System::getSystem()
{
    if (system == NULL)
    {
        // Serial.begin(115200); // start serial for output
        Serial.begin(500000); // start serial for output
        system = new System();
        // OUT_LN(F("getSystem System()"));
    }

    // OUT_LN("getSystem()");
    return system;
}
System::System()
{
    // Serial.begin(115200); // start serial for output
    // OUT_LN(freeMemory());
    // OUT_LN("SerialStarted");
}

System::~System(){
    termometry.clear();
    przekazniki.clear();
    przyciski.clear();
}

void System::begin(){
    // Serial.begin(115200); // start serial for output
    OUT_LN(freeMemory());
    OUT_LN(F("SerialStarted"));
    comunication = I2CConverter::getInstance();
    comunication->begin();
    // OUT_LN(F("comunication->begin();"));
    Wire.onReceive(I2CConverter::onRecieveEvent);
    // OUT_LN(F("Wire.onReceive(I2CConverter::onRecieveEvent);"));
    Wire.onRequest(I2CConverter::onRequestEvent);
    // OUT_LN(F("Wire.onReceive(I2CConverter::onRequestEvent);"));
    timer.begin(MINS(CZAS_ODSWIERZANIA_TEMPERATURY));
    // OUT_LN(F("timer.begin(MINS(CZAS_ODSWIERZANIA_TEMPERATURY));"));
    if (EEPROM[EEPROM_ADRES_OF_STARTUP_BYTE] != 1 && EEPROM[EEPROM_ADRES_OF_STARTUP_BYTE] != 0)
    {
        EEPROM.write(EEPROM_ADRES_OF_STARTUP_BYTE,0);
    }
    
    if (!this->getStartUpVariant())
    {
        Przycisk *p1 = (Przycisk *)this->addDevice(Device::TYPE::PRZYCISK, A3);
        Przycisk *p2 = (Przycisk *)this->addDevice(Device::TYPE::PRZYCISK, 14);

        Roleta *r = (Roleta *)this->addDevice(Device::TYPE::ROLETA, 15, 16);
        Przekaznik *s2 = (Przekaznik *)this->addDevice(Device::TYPE::PRZEKAZNIK, 12);
        Przekaznik *s1 = (Przekaznik *)this->addDevice(Device::TYPE::PRZEKAZNIK, 13);
        // p1->setCzyPominac(true);
        // p2->setCzyPominac(true);
        Command *tmp = new Command;
        tmp->setDevice(r);
        tmp->setCommandType(Command::KOMENDY::RECEIVE_ZMIEN_STAN_ROLETY);
        byte parametry[8] = {'U', 0, 0, 0, 0, 0, 0, 0};
        tmp->setParams(parametry);
        p1->dodajFunkcjeKlikniecia(tmp, 1);
        tmp = new Command;
        tmp->setDevice(r);
        tmp->setCommandType(Command::KOMENDY::RECEIVE_ZMIEN_STAN_ROLETY);
        parametry[0] = 'D';
        tmp->setParams(parametry);
        p1->dodajFunkcjeKlikniecia(tmp, 2);
        tmp = new Command;
        tmp->setDevice(r);
        tmp->setCommandType(Command::KOMENDY::RECEIVE_ZMIEN_STAN_ROLETY);
        parametry[0] = 'S';
        tmp->setParams(parametry);
        p1->dodajFunkcjeKlikniecia(tmp, 3);

        tmp = new Command;
        tmp->setDevice(s1);
        tmp->setCommandType(Command::KOMENDY::RECEIVE_ZMIEN_STAN_PRZEKAZNIKA);
        parametry[0] = 0;
        tmp->setParams(parametry);
        p2->dodajFunkcjeKlikniecia(tmp, 1);
        tmp = new Command;
        tmp->setDevice(s2);
        tmp->setCommandType(Command::KOMENDY::RECEIVE_ZMIEN_STAN_PRZEKAZNIKA);
        parametry[0] = 0;
        tmp->setParams(parametry);
        p2->dodajFunkcjeKlikniecia(tmp, 2);
    }
    else
    {
        init_system();
        this->setNextStartupVariant(false);//next startup should be with devices
        Command * afterInitCommand = new Command;
        byte parametry[8] = {1,0,0,0,0,0,0,0};
        afterInitCommand->setParams(parametry);
        afterInitCommand->setCommandType(Command::KOMENDY::SEND_REPLY);
        comunication->doWyslania.add(afterInitCommand);
        pinMode(12, OUTPUT);
        pinMode(13, OUTPUT);
        pinMode(15, OUTPUT);
        pinMode(16, OUTPUT);
    }
    
    Termometr::init();
    OUT_LN(freeMemory());
}

void System::tic(){
    // if (this->termometry.size() > 0 && timer.available()) //TODO ustawić częstotliwość sprawdzania!
    // {
    //     for (byte i = 0; i < this->termometry.size(); i++)
    //     {
    //         this->termometry.get(i)->updateTemperature();
    //         OUT("Termometr: ");
    //         OUT(i);
    //         OUT(" = ");
    //         OUT_LN(this->termometry.get(i)->getTemperature());
    //     }
    //     OUT_LN(F("timer"));
    //     timer.begin(MINS(CZAS_ODSWIERZANIA_TEMPERATURY));
    // }
    if (this->termometry.size() > 0 ) //TODO ustawić częstotliwość sprawdzania!
    {
        for (byte i = 0; i < this->termometry.size(); i++)
        {
            this->termometry.get(i)->updateTemperature();
            // OUT("Termometr: ");
            // OUT(i);
            // OUT(" = ");
            // OUT_LN(this->termometry.get(i)->getTemperature());
        }
    }
    if (this->rolety.size() > 0) 
    {
        for (byte i = 0; i < this->rolety.size(); i++)
        {
            this->rolety.get(i)->tic();
            // OUT("tic: Roleta: ");
            // OUT_LN(i);
        }
    }
    if (this->przyciski.size() > 0)
    {
        for (byte i = 0; i < this->przyciski.size(); i++)
        {
            this->przyciski.get(i)->tic();
        }
    }
    
}

Device* System::addDevice(Device::TYPE typeOfDevice, byte pin1, byte pin2){
    switch (typeOfDevice)
    {
        case Device::TYPE::BRAK:
        break;

        case Device::TYPE::TERMOMETR:{
                OUT_LN(F("---Add Ther---"))
            Termometr *tmp = new Termometr;
            if (tmp->begin())
            { //spr. skonfigurować kolejny termometr
                OUT_LN(F("TMP = notnull"))
                tmp->setId(idDevice++);
                OUT(F("ID: "));
                OUT_LN(tmp->getId());
                this->devices.add(tmp);    //dodaj do głównej listy urządzeń
                this->termometry.add(tmp); //dodaj do listy termometrów w systemie
                OUT(F("ID: "));
                OUT_LN(tmp->getId());
                OUT("DEV TYPE: ");
                OUT_LN(tmp->getType());
                return tmp;
            }
            else
            {
                OUT_LN(F("TMP =null"))
                delete tmp;
                return nullptr; //TODO Poprawić
            }
            break;
        }
        case Device::TYPE::PRZEKAZNIK:
            {
                OUT_LN(F("---Add Switch---"))
                Przekaznik * tmp = new Przekaznik();
                if(tmp->begin(pin1)){//jeśli uda się poparawnie dodać przekaźnik do systemu
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
                OUT_LN(F("---Add Button---"))
                // OUT("pin1:")
                // OUT_LN(pin1);
                Przycisk * tmp = new Przycisk();
                if(tmp->begin(pin1)){//jeśli uda się poparawnie dodać przekaźnik do systemu
                    // OUT(F("Poprawnie dodano Przycisk"))
                    tmp->setId(idDevice++);//nadaj mu id
                    OUT("\t id: ")
                    OUT_LN(tmp->getId());
                    this->devices.add(tmp->getId(), tmp);//dodaj do listy urzadzen
                    this->przyciski.add(tmp);//dodaj do listy urzadzen
                    return tmp;
                }
                else{
                    // OUT_LN(F("Nie udało dodać Przycisku"))
                    return nullptr;
                }
            }
            break;
        case Device::TYPE::PRZYCISK_ROLETA:
            
            break;
        case Device::TYPE::ROLETA:
            {
                OUT_LN(F("---Dodaj Blind---"));
                // OUT("pin1:");
                // OUT_LN(pin1);
                // OUT("pin2:");
                // OUT_LN(pin2);
                Roleta *tmp = new Roleta();
                if (tmp->begin(pin1, pin2))
                { //jeśli uda się poparawnie dodać przekaźnik do systemu
                    // OUT_LN(F("Poprawnie dodano Roletę"))
                    tmp->setId(idDevice++); //nadaj mu id
                    // OUT("id:")
                    // OUT_LN(tmp->getId());
                    // OUT(F("ROLETA: "));
                    // OUT_LN(tmp->toString());
                    this->devices.add(tmp->getId(), tmp); //dodaj do listy urzadzen
                    this->rolety.add(tmp);                //dodaj do listy rolet
                    return tmp;
                }
                else
                {
                    // OUT_LN(F("Nie udało dodać Rolety"))
                    return nullptr;
                }
            }
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
        for (byte i = 0; i < rolety.size(); i++) //przeszukaj kontener z roletami
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
    default:
        break;
    }
    return true;
}
Device* System::getDevice(byte id){
    OUT(F("Search dev id:\t"))
    OUT_LN(id);
    if (devices.get(id) == nullptr)
    {
        OUT_LN("NO SUCH DEV!")
    }
    
    return devices.get(id);
}

Termometr* System::getTermometr(const byte* adress){

    for (int i = 0; i < termometry.size(); i++)
    {
        if (termometry.get(i)->compare2Adresses(termometry.get(i)->getAddres(), adress)){
            OUT(F("FOUND ADRESS : "));
            Termometr *termometr = termometry.get(i);
            for (int j = 0; j < 8; j++)
            {
                OUT(termometr->getAddres()[j])
                OUT(" ")
            }
            OUT_LN(" ")
            OUT(F("FOUND DEV TYPE: "))
            OUT(termometr->getType());
            OUT_LN(termometr->getType() == Device::TERMOMETR ? " - OK" : " - NOT TER");
            return termometr;
        }
            
    }
    return nullptr;    

}

LinkedList<byte*> System::getAdrOfThermometrs(){
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
    OUT_LN(F("DEL"))
    // Device *tmp;
    // for (byte i = 0; i < devices.size(); i++)
    // {
    //     tmp = devices.get(i);
    //     delete tmp;//usuń wszystkie urządzenia w systemie
    // }
    
    // //oczyść wszystkie listy urządzeń
    // devices.clear();
    // idDevice = 0;
    // przekazniki.clear();
    // rolety.clear();
    // termometry.clear();
    // przyciski.clear();
    // init_system();
    System::getSystem()->setNextStartupVariant(true);
    OUT_LN(freeMemory());
    resetFunc();//zresetuj system
}

void System::init_system(){
    is_initiated = true;
}

bool System::is_init(){
    return is_initiated;
}

bool System::getStartUpVariant(){
    OUT(F("EEPROM: "))
    OUT_LN(EEPROM[EEPROM_ADRES_OF_STARTUP_BYTE]);
    return EEPROM[EEPROM_ADRES_OF_STARTUP_BYTE] == 1;
}

void System::setNextStartupVariant(bool variant){
    OUT(F("EEPROM bef change: "))
    OUT_LN(EEPROM[EEPROM_ADRES_OF_STARTUP_BYTE]);
    EEPROM.update(EEPROM_ADRES_OF_STARTUP_BYTE,variant?1:0);
    OUT(F("EEPROM aft change: "))
    OUT_LN(EEPROM[EEPROM_ADRES_OF_STARTUP_BYTE]);
}

byte System::howManyThermometers(){
    return this->termometry.size();
}