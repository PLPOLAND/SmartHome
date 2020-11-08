#include <Termometr.h>
byte Termometr::termometrowWSystemie = 0;
DallasTemperature *Termometr::sensors = nullptr;

Termometr::Termometr()
{
    if (this->sensors == nullptr)
    {
        this->sensors = new DallasTemperature(new OneWire(ONEWIRE_BUS));
        this->sensors->begin();
    }
    if (termometrowWSystemie<this->sensors->getDeviceCount())
    {
        this->id = termometrowWSystemie;//przypisanie id sensora
        termometrowWSystemie++;
        Serial.println(F("Stworzono nowy termometr"));

    }
    else{
      this->id = -1;
    }
}

Termometr::~Termometr()
{
    delete sensors;
}

//Zwraca temeraturę czujnika
float Termometr::getTemperature(){
    this->sensors->requestTemperaturesByIndex(id);
    Serial.println(this->sensors->getTempCByIndex(id));
    return this->sensors->getTempCByIndex(id);
}

byte Termometr::getID(){
    return this->id;
}
bool Termometr::isCorrect(){
    if (id== -1)
        return false;
    return true;
    
}