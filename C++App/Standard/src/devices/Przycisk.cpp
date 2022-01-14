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

    for (int i = 0; i < funkcje_kliknieca.size(); i++) //TODO doać usuwanie komend przy desturkcji 
    {
        
    }
    

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
    ///stan wejscia
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

        

    }
    
    if (stan == PRZYCISNIETY && tmpStan == 1 && time->available()) //Wykrycie przytrzymania
    {

        OUT_LN(F("Wykrycie Przytrzymanie"));
        stan = PRZYTRZYMANY;
    }
    if (stan == PRZYTRZYMANY && tmpStan == 0) //Puszczenie po przytrzymaniu
    { 

        OUT_LN(F("Puszczony po przytrzymaniu"));

        time->time(STOP);
        stan = BRAK_AKCJI;
        this->wykonaj();//dla BRAK_AKCJI
        klikniecia = 0;
    }
    else if (stan == PRZYTRZYMANY && tmpStan == 1) //Przytrzymywanie
    {

        OUT_LN(F("Przytrzymanie"));
        this->wykonaj();//dla PRZYTRZYMANY
    }

    if (time->available() && stan == PUSZCZONY) {

        OUT_LN(F("Koniec okresu klikniec"));
        time->time(STOP);
        this->wykonaj();//dla PUSZCZONY
        stan = BRAK_AKCJI;
    }

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

/**
 * @brief Wykonuje komendy po wciścnięciu / przytrzymaniu / puszczeniu
 * 
 * @return true 
 * @return false 
 */
bool Przycisk::wykonaj(){
    switch (stan)
    {
    case PRZYTRZYMANY://Przycisk jest przytrzymywany
        {

        }
        break;
    case PUSZCZONY:// Przycisk nie był przytrzymywany i skończył się czas na kolejne przyciśnięcie
        {
            Command* command = funkcje_kliknieca.get(klikniecia);
            this->runCommand(command);
        // System::getSystem()->runCommand(command);
        }
        break;
    case BRAK_AKCJI://Przycisk był przytrzymany i został właśnie puszczony
        {
            
        }
        break;
    default:
        break;
    }
    return true; //TODO obsluga bledow?
}

bool Przycisk::dodajFunkcjeKlikniecia(Command* command){//TODO
    return true;
}

bool Przycisk::dodajFunkcjePrzytrzymania(Command* command){//TODO
    return true;
}

bool Przycisk::dodajFunkcjePuszczeniaPoPrzytrzymaniu(Command* command){//TODO
    return true;
}

bool Przycisk::runCommand(Command *command)
{
    switch (command->getCommandType())
    {
    case Command::KOMENDY::RECEIVE_ZMIEN_STAN_PRZEKAZNIKA:
    {
        if (command->getDevice()->getType() == Device::TYPE::PRZEKAZNIK)
        {
            if (((Przekaznik *)command->getDevice())->getStan())
            {
                ((Przekaznik *)command->getDevice())->setStan(0);
            }
            else
            {
                ((Przekaznik *)command->getDevice())->setStan(1);
            }
        }
        else{
            return false;
        }
    }
    break;
    case Command::KOMENDY::RECEIVE_ZMIEN_STAN_ROLETY:
    {
        if (command->getDevice()->getType() == Device::TYPE::ROLETA)
        {
            if (((Roleta *)command->getDevice())->getStan() == StanRolety::NIEOKRESLONY || ((Roleta *)command->getDevice())->getStan() == StanRolety::OPUSZCZONA)
            {
                ((Roleta *)command->getDevice())->podnies();
            }
            else
            {
                ((Roleta *)command->getDevice())->opusc();
            }
        }
        else{
            return false;
        }
    }
    break;
    default:
        break;
    }
    return true;
}