#include "System.h"

System* System::system = nullptr;
Timer System::timer = Timer();

System *System::getSystem()
{
    if (system == nullptr)
    {
        system = new System();
        Serial.println("getSystem System()");
        Serial.flush();
    }

    Serial.println("getSystem()");
    Serial.flush();
    return system;
}
System::System()
{
    Serial.begin(115200); // start serial for output
    Serial.println(freeMemory());
    Serial.println("SerialStarted");
    // Serial.flush();
    // comunication = I2CConverter::getInstance();
    // comunication->begin();
    // Serial.println("comunication->begin();");
    // Serial.flush();
    // Wire.onReceive(I2CConverter::onRecieveEvent);
    // Serial.println("Wire.onReceive(I2CConverter::onRecieveEvent);");
    // Serial.flush();
    // Wire.onRequest(I2CConverter::onRequestEvent);
    // Serial.println("Wire.onReceive(I2CConverter::onRequestEvent);");
    // Serial.flush();
    // timer.begin(MINS(CZAS_ODSWIERZANIA_TEMPERATURY));
    // Serial.println("timer.begin(MINS(CZAS_ODSWIERZANIA_TEMPERATURY));");
    // Serial.println(freeMemory());
    // Serial.flush();
}

System::~System(){
    termometry.clear();
    przekazniki.clear();
    przyciski.clear();
}

void System::begin(){
    // Serial.begin(115200); // start serial for output
    Serial.println(freeMemory());
    Serial.println("SerialStarted");
    Serial.flush();
    comunication = I2CConverter::getInstance();
    comunication->begin();
    Serial.println("comunication->begin();");
    Serial.flush();
    Wire.onReceive(I2CConverter::onRecieveEvent);
    Serial.println("Wire.onReceive(I2CConverter::onRecieveEvent);");
    Serial.flush();
    Wire.onRequest(I2CConverter::onRequestEvent);
    Serial.println("Wire.onReceive(I2CConverter::onRequestEvent);");
    Serial.flush();
    timer.begin(MINS(CZAS_ODSWIERZANIA_TEMPERATURY));
    Serial.println("timer.begin(MINS(CZAS_ODSWIERZANIA_TEMPERATURY));");
    Serial.println(freeMemory());
    Serial.flush();
}

void System::tic(){
    if (this->termometry.size() > 0 && timer.available()) //TODO ustawić częstotliwość sprawdzania!
    {
        for (byte i = 0; i < this->termometry.size(); i++)
        {
            this->termometry.get(i)->updateTemperature();
            Serial.print("Termometr: ");
            Serial.print(i);
            Serial.print(" = ");
            Serial.println(this->termometry.get(i)->getTemperature());
            Serial.flush();
        }
        Serial.println("timer");
        timer.begin(MINS(CZAS_ODSWIERZANIA_TEMPERATURY));
    }

}

byte System::addDevice(Device::TYPE typeOfDevice){
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
                return tmp->getId();
            }
            else
            {
                delete tmp;
                return -1; //TODO Poprawić
            }
            break;
        }
        case Device::TYPE::PRZEKAZNIK:
        
            break;
        case Device::TYPE::PRZYCISK:
            
            break;
        case Device::TYPE::PRZYCISK_ROLETA:
            
            break;
        case Device::TYPE::ROLETA:
            
            break;
        
        default:
            break;
    }
    return -1;//TODO poprawić???
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
// byte System::addPrzekaznik(){
    
// }
// byte System::addRoleta(){

// }
// byte System::addPrzycisk(){

// }
// byte System::addTermometr(){
//     Termometr *tmp = (Termometr *)malloc(sizeof(Termometr));
//     tmp->begin();//TODO czy termometr nie powinien dostawać ID według miejsca w kontenerze?
//     Serial.println("newTermometr");
//     if (!tmp->isCorrect())
//     {
//         delete tmp;
//         return -1;
//     }
//     else
//     {
//         Serial.println(tmp->getTemperature());
//         this->termometry.add(tmp);
//         return termometry.get(termometry.size() - 1)->getID(); // Zwróć ID termometru na płytce
        
//     }
// }


// bool System::removePrzekaznik(byte id){
//     delete getPrzekaznik(id);
//     przekazniki.remove(id);
// }
// bool System::removeRoleta(byte id){
//     delete getRoleta(id);
//     rolety.remove(id);
// }
// bool System::removePrzycisk(byte id){
//     delete getPrzycisk(id);
//     przyciski.remove(id);
// }
// bool System::removeTermometr(byte id){
//     delete getTermometr(id);
//     termometry.remove(id);
// }

// Termometr* System::getTermometr(byte id){
//     return termometry.get(id);
// }

// Przycisk* System::getPrzycisk(byte id){
//     return przyciski.get(id);
// }

// Przekaznik* System::getPrzekaznik(byte id){
//     return przekazniki.get(id);
// }

// Roleta* System::getRoleta(byte id){
//     return rolety.get(id);
// }
