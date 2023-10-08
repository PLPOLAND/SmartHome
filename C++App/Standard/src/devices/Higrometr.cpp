#include "Higrometr.h"
#include "Stale.h"

Higrometr::Higrometr() : Device(TYPE::HIGROMETR)
{

    dht = new DHT(DHTPIN, DHTTYPE);
    timer = new Timer;
    timer->begin(2500);
    timer->restart();
    dht->begin();
    this->update(true);
}

Higrometr::~Higrometr()
{
    delete dht;
    delete timer;
}

int Higrometr::getHumidity()
{
    return this->humidity;
}

float Higrometr::getTemperature()
{
    return this->temperature;
}

void Higrometr::update(bool force)
{
    // OUT_LN(timer->time())
    if(timer->available() || force)
    {
        OUT_LN(F("H()"));
        timer->restart();
        float tmpHum = dht->readHumidity(force);
        if (isnan(tmpHum))
        {
            humidity = 0;
        }
        else
        {
            humidity = (int) (tmpHum+0.5);
        }
        if (isnan(temperature = dht->readTemperature(false, force)))
        {
            temperature = 0;
        }
        
        // temperature = dht->readTemperature(false, force);
        // OUT(F("isCorr: "))
        // OUT_LN(this->isCorrect())
        // OUT("H: ")
        // OUT_LN(this->getHumidity())
        // OUT("T: ")
        // Serial.println(this->getTemperature());
    }
}

bool Higrometr::isCorrect()
{
    return (humidity != 0 && temperature != NAN);
}

byte* Higrometr::getStateAsByteArray()
{
    byte* tmp = new byte[7];
    String tmpStr = String (this->temperature, 2U);
    for (byte i = 0; i < 8; i++)
    {
        tmp[i] = 0;
    }
    for (byte i = 0; i < 5; i++)
    {
        tmp[i] = tmpStr.charAt(i);
    }
    tmp[5] = this->humidity;
    return tmp;
}