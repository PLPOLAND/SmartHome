#include <devices/Przycisk.h>

/**
 * 
 * Pusty konstruktor
 * \warning Nie przypisuje pinu nasluchu, należy zrobić to samemu - \see this.setPin();
 * 
 * */
Przycisk::Przycisk()
{
    Device(TYPE::PRZYCISK);
    time = new Timer();
    time->time(STOP);
}

Przycisk::~Przycisk()
{
    delete time;
}

/**
 * 
 * Konstruktor 
 * 
 * \param pin pin nasłuchu stanu
 * 
 */
Przycisk::Przycisk(byte pin)
{
    Device(Device::TYPE::PRZYCISK);
    this->setPin(pin);
    time = new Timer();
    time->time(STOP);
}
/**
 * 
 * Konstruktor 
 * 
 * \param id id urządzenia w systemie
 * \param pin pin nasłuchu stanu
 */
Przycisk::Przycisk(byte id, byte pin)
{
    this->setPin(pin);
    time = new Timer();
    time->time(STOP);
}

byte Przycisk::getPin()
{
    return pin;
}
void Przycisk::setPin(byte pin)
{
    this->pin = pin;
    pinMode(this->pin, INPUT_PULLUP);
}

Przycisk::StanPrzycisku Przycisk::getStan()
{
    return stan;
}

/**
 * Aktualizuje stan przycisku
 */
void Przycisk::updateStan(){
    bool tmpStan = 0;
    if (digitalRead(pin)==LOW)
    {
        tmpStan = 1;
    }
    else
    {
        tmpStan = 0;
    }

    if (tmpStan == 1 && stan == PUSZCZONY && !time->available()) //Przyciśniecie po raz kolejny
    {

        Serial.print(F("next click "));
        time->begin(SECS(1));
        stan = PRZYCISNIETY;
        klikniecia++;
        Serial.println(klikniecia);
    }
    else if (tmpStan == 1 && stan == BRAK_AKCJI) //Przyciśniecie po raz pierwszy
    {
        Serial.println(F("first click"));
        stan = PRZYCISNIETY;
        time->begin(SECS(1));
        klikniecia = 1;
    }
    if (stan == PRZYCISNIETY && tmpStan == 0 )//Zakończenie kliknięcia bez przytrzymania
    {

        Serial.println(F("puszczony bez przytrzymania"));
        stan = PUSZCZONY;
        //TODO::

    }
    
    if (stan == PRZYCISNIETY && tmpStan == 1 && time->available()) //Wykrycie przytrzymania
    {

        Serial.println(F("Wykrycie Przytrzymanie"));
        stan = PRZYTRZYMANY;
    }
    if (stan == PRZYTRZYMANY && tmpStan == 0)
    { //Puszczenie po przytrzymaniu

        Serial.println(F("Puszczony po przytrzymaniu"));

        time->time(STOP);
        stan = BRAK_AKCJI;
        //TODO WYkonanie po puszczeniu przytrzymania
        klikniecia = 0;
    }
    else if (stan == PRZYTRZYMANY && tmpStan == 1) //Przytrzymywanie
    {

        Serial.println(F("Przytrzymanie"));
        //TODO wykonanie podczas przytrzymania
    }

    if (time->available() && stan == PUSZCZONY) {

        Serial.println(F("Koniec okresu klikniec"));
        time->time(STOP);
        stan = BRAK_AKCJI;
        //TODO:
    }

    // if (time->time() != 0) {
    //     Serial.println(time->time());
    // }
}


/**
 * wywołuje rzeczy które powinny byc wywolywane w kazdym obrocie loop 
 *  
 * np: aktualizacja stanu
 * \see Przycisk::updateStan();
 * */
void Przycisk::tic(){
    this->updateStan(); //obsluga zmiany stanów przycisku

}