#include <Termometr.h>
byte Termometr::termometrowWSystemie = 0;

Termometr::Termometr()
{
    if (sensors == nullptr)
    {
        sensors = new DallasTemperature(new OneWire(ONEWIRE_BUS));
        sensors->begin();
    }
    if (termometrowWSystemie<sensors->getDeviceCount())
    {
        this->id = termometrowWSystemie;//przypisanie id sensora
        termometrowWSystemie++;

    }
    else{
        throw String(F("nie ma wiecej termometrow w systemie"));
    }
    
}

Termometr::~Termometr()
{
    //delete sensors;
}

//Zwraca temeraturę czujnika
float Termometr::getTemperature(){
    sensors->requestTemperaturesByIndex(id);
    return sensors->getTempCByIndex(id);
}

byte Termometr::getID(){
    return this->id;
}