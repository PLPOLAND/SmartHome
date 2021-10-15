#include "System.h"
System *System::getSystem()
{
    if (system == nullptr)
    {
        system = new System();
    }

    return system;
}
System::System()
{
    Serial.begin(115200); // start serial for output
    comunication = I2CConverter::getInstance();
    Wire.onReceive(I2CConverter::onRecieveEvent);
    Wire.onRequest(I2CConverter::onRequestEvent);
    timer.begin(MINS(CZAS_ODSWIERZANIA_TEMPERATURY));
}

System::~System(){
    termometry.clear();
    przekazniki.clear();
    przyciski.clear();
}

void System::tic(){
    if (this->termometry.size() > 0 && timer.available()) //TODO ustawić częstotliwość sprawdzania!
    {
        for (byte i = 0; i < this->termometry.size(); i++)
        {
            this->termometry.get(i)->updateTemperature();
        }
        Serial.println("timer");
        timer.begin(MINS(CZAS_ODSWIERZANIA_TEMPERATURY));
    }
}



byte System::addPrzekaznik(){
    
}
byte System::addRoleta(){

}
byte System::addPrzycisk(){

}
byte System::addTermometr(){
    Termometr *tmp = (Termometr *)malloc(sizeof(Termometr));
    tmp->begin();//TODO czy termometr nie powinien dostawać ID według miejsca w kontenerze?
    Serial.println("newTermometr");
    if (!tmp->isCorrect())
    {
        delete tmp;
        return -1;
    }
    else
    {
        Serial.println(tmp->getTemperature());
        this->termometry.add(tmp);
        return termometry.get(termometry.size() - 1)->getID(); // Zwróć ID termometru na płytce
        
    }
}


bool System::removePrzekaznik(byte id){
    delete getPrzekaznik(id);
    przekazniki.remove(id);
}
bool System::removeRoleta(byte id){
    delete getRoleta(id);
    rolety.remove(id);
}
bool System::removePrzycisk(byte id){
    delete getPrzycisk(id);
    przyciski.remove(id);
}
bool System::removeTermometr(byte id){
    delete getTermometr(id);
    termometry.remove(id);
}

Termometr* System::getTermometr(byte id){
    return termometry.get(id);
}

Przycisk* System::getPrzycisk(byte id){
    return przyciski.get(id);
}

Przekaznik* System::getPrzekaznik(byte id){
    return przekazniki.get(id);
}

Roleta* System::getRoleta(byte id){
    return rolety.get(id);
}
