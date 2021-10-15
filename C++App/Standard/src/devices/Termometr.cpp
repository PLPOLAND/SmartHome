#include <devices/Termometr.h>
byte Termometr::termometrowWSystemie = 0;
OneWire Termometr::oneWire = OneWire(ONEWIRE_BUS);
DallasTemperature Termometr::sensors = DallasTemperature(&oneWire);

Termometr::Termometr()
{
    Device(TYPE::TERMOMETR);
    // // if (sensors.getDeviceCount() == 0)
    // // {
    //     sensors.begin();
    //     Serial.println("Termo_begin");
    // // }
    // if (termometrowWSystemie < sensors.getDeviceCount())
    // {
    //     id = termometrowWSystemie;//przypisanie id sensora
    //     termometrowWSystemie++;
    //     Serial.print("Stworzono nowy termometr \nid: ");
    //     Serial.println(id);
    // }
    // else{
    //     id = -1;
    //     Serial.println("Błąd brak nowych termo");
    // }
}

Termometr::~Termometr()
{
}

//Zwraca temeraturę czujnika
float Termometr::getTemperature(){
    return temperatura;
}

byte Termometr::getID(){
    return id;
}
bool Termometr::isCorrect(){
    if (id== -1)
        return false;
    return true;
    
}
byte Termometr::begin()
{
    // if (sensors.getDeviceCount() == 0)
    // {
    sensors.begin();
    Serial.println("Termo_begin");
    // }
    if (termometrowWSystemie < sensors.getDeviceCount()) {
        id = termometrowWSystemie; //przypisanie id sensora
        termometrowWSystemie++;
        Serial.println("Stworzono nowy termometr");

    } else {
        id = -1;
        Serial.println("Błąd brak nowych termo");
    }
    return id;
}
void Termometr::updateTemperature(){
    // Serial.println("getT");
    // Serial.println(id);
    sensors.requestTemperaturesByIndex(id);
    // Serial.println("getTReq");
    // Serial.println(sensors.getTempCByIndex(id));
    temperatura=sensors.getTempCByIndex(id);
}