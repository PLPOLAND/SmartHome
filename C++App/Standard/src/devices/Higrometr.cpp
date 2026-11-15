#include "Higrometr.h"
#include "Stale.h"

Higrometr::Higrometr() : Device(TYPE::HIGROMETR)
{
    temperature = NAN; // do pierwszego odczytu (po 15 s) wartość jest nieznana
    humidity = 0;

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
    byte* tmp = new byte[8]; // zerowane jest 8 bajtów - przy 7 nadpisywany był bajt sterty za buforem
    for (byte i = 0; i < 8; i++)
    {
        tmp[i] = 0;
    }
    if (isnan(this->temperature) || this->temperature < -200.0f || this->temperature > 200.0f)
    {
        tmp[0] = 'E'; // brak/nieprawidłowy odczyt - dtostrf dużej liczby przepełniłby bufor
        return tmp;
    }
    char tekst[12]; // dtostrf na stosie zamiast String (bez dodatkowej alokacji na stercie)
    dtostrf(this->temperature, 1, 2, tekst); // w zakresie +-200 najwyżej "-200.00" (7 znaków)
    for (byte i = 0; i < 5 && tekst[i] != '\0'; i++)
    {
        tmp[i] = tekst[i];
    }
    tmp[5] = this->humidity;
    return tmp;
}