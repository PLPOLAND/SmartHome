#include "Higrometr.h"
#include "Stale.h"

Higrometr::Higrometr() : Device(TYPE::HIGROMETR)
{

    dht = new DHT(DHTPIN, DHTTYPE);
    timer = new Timer();
    timer->begin(15000U);
    timer->restart();
    dht->begin();
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

void Higrometr::update()
{
    if(timer->available())
    {
        OUT_LN("H::up()");
        timer->restart();
        float tmpHum = dht->readHumidity();
        if (isnan(tmpHum))
        {
            humidity = 0;
        }
        else
        {
            humidity = (int) (tmpHum+0.5);
        }
        temperature = dht->readTemperature();
        OUT("isCorr: ")
        OUT_LN(this->isCorrect())
        OUT("H: ")
        OUT_LN(this->getHumidity())
        // OUT("T: ")
        // OUT_LN(this->getTemperature())
    }
}

bool Higrometr::isCorrect()
{
    return true;
    // return (humidity != 0 && temperature != NAN);
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