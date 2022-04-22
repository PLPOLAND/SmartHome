#include <devices/Przekaznik.h>
Przekaznik::Przekaznik(){
    Device(TYPE::PRZEKAZNIK);
}
Przekaznik::~Przekaznik()
{
    digitalWrite(pin, LOW);
}
bool Przekaznik::begin(byte pin, bool stan)
{
    this->setType(TYPE::PRZEKAZNIK);
    if (pin >= PINOW_NA_ADRES + 2 && pin <= 16)
    {
        this->pin = pin;
        // OUT(F("PIN: "))
        // OUT((int)pin)
        // OUT_LN(F(" PIN OUTPUT"))
        pinMode(pin, OUTPUT);
    }
    else
    {
        return false;
    }
    this->stan = stan;
    if (stan)
    {
        digitalWrite(pin, HIGH);
    }
    else
    {
        digitalWrite(pin, LOW);
    }
    return true;
}

byte Przekaznik::getPin()
{
    return pin;
};

//Ustawianie pinu uzywanego do sterowania przekaźnikiem
void Przekaznik::setPin(byte pin)
{
    if (pin >= PINOW_NA_ADRES+2 && pin <= 16)
    {
        digitalWrite(pin, LOW); //resetowanie poprzedniego stanu wyjscia
        this->pin = pin;
        pinMode(pin, OUTPUT);
        digitalWrite(pin, stan == false ? LOW : HIGH);
    }
};

bool Przekaznik::getStan()
{
    return stan;
};
//włącz/wyłącz przekaźnik (true/false)
void Przekaznik::setStan(bool stan)
{
    OUT_LN(F("SET STAN"))
    OUT(F("STAN: "))
    OUT_LN(stan);
    OUT(F("PIN: "))
    OUT_LN(pin);
    this->stan = stan;
    digitalWrite(pin, stan == false ? LOW : HIGH);
    OUT(F("PIN STAN: "))
    OUT_LN(digitalRead(pin));

};
//włącz/wyłącz przekaźnik (1 = włącz; inna wartość = wyłącz)
void Przekaznik::setStan(int stan)
{
    this->stan = stan == 1 ? true : false;

    digitalWrite(pin, this->stan == false ? LOW : HIGH);
}
/**
 * @brief Zwraca String opisujący ten obiekt.
 * 
 * @return String 
 */
String Przekaznik::toString(){
    String str;

    str+="ID: ";
    str+=this->getId();
    str+="\tPIN: ";
    str+=pin;
    str+="\tSTAN: ";
    str+=stan;
    return str;
}