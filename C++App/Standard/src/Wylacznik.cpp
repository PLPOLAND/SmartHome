#include <Wylacznik.h>

Wylacznik::Wylacznik(/* args */)
{
}

Wylacznik::~Wylacznik()
{
}
void Wylacznik::updateStan(){
    bool tmpStan = 0;
    if (digitalRead(pin)==HIGH)
    {
        tmpStan = 1;
    }
    else
    {
        tmpStan = 0;
    }

    if (tmpStan == 1 && stan == PUSZCZONY && !time.available()) //Przyciśniecie po raz kolejny
    {
        stan = PRZYCISNIETY;
        klikniecia++;
    }
    else if (tmpStan == 1 && stan == PUSZCZONY) //Przyciśniecie po raz pierwszy
    {
        stan = PRZYCISNIETY;
        time.begin(1000);
        klikniecia = 1;
    }
    if (stan == PRZYCISNIETY && tmpStan == 0 && !time.available())//Zakończenie kliknięć bez przytrzymania
    {
        stan = PUSZCZONY;
        //TODO::
    }
    if (stan == PRZYCISNIETY && tmpStan == 1 && time.available()) //Wykrycie przytrzymania
    {
        stan == PRZYTRZYMANY;
    }
    if (stan == PRZYTRZYMANY && tmpStan == 0)
    { //Puszczenie po przytrzymaniu
        stan = PUSZCZONY;
        //TODO WYkonanie po puszczeniu przytrzymania
    }
    else if (stan == PRZYTRZYMANY && tmpStan == 1) //Przytrzymywanie
    {
        //TODO wykonanie podczas przytrzymania
    }
}