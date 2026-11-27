#include <devices/Przekaznik.h>
Przekaznik::Przekaznik(){
    Device(TYPE::PRZEKAZNIK);
    pin = 0;
    stan = false;
}
Przekaznik::~Przekaznik()
{
    // Przekaźniki są aktywne stanem niskim - HIGH = wyłączony
    if (pin >= PINOW_NA_ADRES + 2 && pin <= 16)
        digitalWrite(pin, HIGH);
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
        this->stan = stan;
        // Najpierw poziom (aktywny stan niski), dopiero potem OUTPUT - bez impulsu załączenia
        digitalWrite(pin, stan ? LOW : HIGH);
        pinMode(pin, OUTPUT);
    }
    else
    {
        return false;
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
        this->pin = pin;
        // Aktywny stan niski (jak w setStan): ustaw poziom przed OUTPUT, żeby nie załączyć przekaźnika
        digitalWrite(pin, stan ? LOW : HIGH);
        pinMode(pin, OUTPUT);
    }
};

bool Przekaznik::getStan()
{
    return stan;
};
//włącz/wyłącz przekaźnik (true/false)
void Przekaznik::setStan(bool stan)
{
    // OUT_LN(F("SET STAN"))
    // OUT(F("STAN: "))
    // OUT_LN(stan);
    // OUT(F("PIN: "))
    // OUT_LN(pin);
    this->stan = stan;
    digitalWrite(pin, stan == true ? LOW : HIGH);
    // OUT(F("PIN STAN: "))
    // OUT_LN(digitalRead(pin));

};
//włącz/wyłącz przekaźnik (1 = włącz; inna wartość = wyłącz)
void Przekaznik::setStan(int stan)
{
    this->stan = stan == 1 ? true : false;

    digitalWrite(pin, this->stan == true ? LOW : HIGH);
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