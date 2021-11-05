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
 * \param pin pin nasłuchu stanu
 * 
 */
bool Przycisk::begin(byte pin)
{
    Device(Device::TYPE::PRZYCISK);
    if(this->setPin(pin)){
        time = new Timer();
        time->time(STOP);
        return true;
    }
    return false;
}

byte Przycisk::getPin()
{
    return pin;
}
bool Przycisk::setPin(byte pin)
{
    if (pin >= PINOW_NA_ADRES + 2 && pin <= 16)
    {
        this->pin = pin;
        pinMode(this->pin, INPUT_PULLUP);
        return true;
    }
    return false;
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

        OUT_LN(F("next click "));
        time->begin(SECS(1));
        stan = PRZYCISNIETY;
        klikniecia++;
        OUT_LN(klikniecia);
    }
    else if (tmpStan == 1 && stan == BRAK_AKCJI) //Przyciśniecie po raz pierwszy
    {
        OUT_LN(F("first click"));
        stan = PRZYCISNIETY;
        time->begin(SECS(1));
        klikniecia = 1;
    }
    if (stan == PRZYCISNIETY && tmpStan == 0 )//Zakończenie kliknięcia bez przytrzymania
    {

        OUT_LN(F("puszczony bez przytrzymania"));
        stan = PUSZCZONY;
        //TODO::

    }
    
    if (stan == PRZYCISNIETY && tmpStan == 1 && time->available()) //Wykrycie przytrzymania
    {

        OUT_LN(F("Wykrycie Przytrzymanie"));
        stan = PRZYTRZYMANY;
    }
    if (stan == PRZYTRZYMANY && tmpStan == 0)
    { //Puszczenie po przytrzymaniu

        OUT_LN(F("Puszczony po przytrzymaniu"));

        time->time(STOP);
        stan = BRAK_AKCJI;
        //TODO WYkonanie po puszczeniu przytrzymania
        klikniecia = 0;
    }
    else if (stan == PRZYTRZYMANY && tmpStan == 1) //Przytrzymywanie
    {

        OUT_LN(F("Przytrzymanie"));
        //TODO wykonanie podczas przytrzymania
    }

    if (time->available() && stan == PUSZCZONY) {

        OUT_LN(F("Koniec okresu klikniec"));
        time->time(STOP);
        stan = BRAK_AKCJI;
        //TODO:
    }

    // if (time->time() != 0) {
    //     OUT_LN(time->time());
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