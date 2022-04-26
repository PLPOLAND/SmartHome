#include <devices/Przycisk.h>
#include <System.h>
#include <Stale.h>

const Command *Przycisk::zapychacz = new Command;

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

    funkcje_klikniecia.clear();
    funkcje_przytrzymania.clear();
    funkcje_przytrzymania.clear();
}

/**
 * 
 * \param pin pin nasłuchu stanu
 * 
 */
bool Przycisk::begin(byte pin)
{
    Device(Device::TYPE::PRZYCISK);
    Command* tmp = new Command;
    for (size_t i = 0; i < MAX_NUMBER_OF_BUTTON_FUNCTIONS; i++)
    {
        funkcje_klikniecia.add(tmp);
        funkcje_przytrzymania.add(tmp);
        funkcje_przytrzymania_puszczenie.add(tmp);
    }
    
    if(this->setPin(pin)){
        time = new Timer();
        time->time(STOP);
        stan = BRAK_AKCJI;
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
    if (pin >= PINOW_NA_ADRES + 2 && pin <= 17)
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
        time->begin(BUTTON_CLICK_TIME);
        stan = PRZYCISNIETY;
        klikniecia++;
        OUT_LN(klikniecia);
    }
    else if (tmpStan == 1 && stan == BRAK_AKCJI) //Przyciśniecie po raz pierwszy
    {
        OUT_LN(F("first click"));
        stan = PRZYCISNIETY;
        time->begin(BUTTON_CLICK_TIME);
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
    // static int i = 0;
    // if(i++>10000){
    //     OUT_LN(F("TIC"));
    // OUT_LN(time->time());
    //     OUT_LN(digitalRead(pin)?"LOW":"HIGH");
    //     OUT_LN(stan);
    //     i = 0 ;
    // }
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
            OUT_LN(F("Wykonywanie PUSZCZONY"))
            OUT(F("klikniec: "))
            OUT_LN(klikniecia);
            Command* command = funkcje_klikniecia.get(klikniecia);
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

bool Przycisk::dodajFunkcjeKlikniecia(Command* command, byte klikniec){
    OUT_LN(F("DODAJ FUNKCJE KLIKNIECIE"))
    OUT(F("Przyski id: "))
    OUT_LN(this->getId());
    OUT("Klikniec: ")
    OUT_LN(klikniec);
    OUT_LN(command->toString());
    OUT(F("Device ID: "))
    OUT_LN(command->getDevice()->getId());
    OUT_LN();
    Command* tmp = new Command;
    tmp->makeCopy(command);
    if(!this->funkcje_klikniecia.set(klikniec,tmp)){//TODO: Wyciek pamięci!
        this->funkcje_klikniecia.add(klikniec,tmp); // jeśli komenda dla tylu kliknięć jeszcze nie istnieje to dodaj ją.
    }
    OUT_LN(F("DODANO KOMENDE"));

    return true;
}
bool Przycisk::usunFunkcjeKlikniecia(byte klikniec){
    OUT_LN(F("USUN FUNKCJE KLIKNIECIE"))
    OUT(F("Przyski id: "))
    OUT_LN(this->getId());
    OUT("Klikniec: ")
    OUT_LN(klikniec);
    OUT_LN();
    delete this->funkcje_klikniecia.get(klikniec);
    if (!this->funkcje_klikniecia.set(klikniec, const_cast<Command *> (zapychacz))){
        this->funkcje_klikniecia.add(klikniec, const_cast<Command *>(zapychacz)); // jeśli komenda dla tylu kliknięć jeszcze nie istnieje to dodaj ją.
    }
    OUT_LN(F("DODANO KOMENDE"));

    return true;
}

bool Przycisk::dodajFunkcjePrzytrzymania(Command* command, byte klikniec){//TODO
    return true;
}

bool Przycisk::dodajFunkcjePuszczeniaPoPrzytrzymaniu(Command* command, byte klikniec){//TODO
    return true;
}

bool Przycisk::runCommand(Command *command)
{
    OUT_LN()
    OUT_LN(F("RUN_COMMAND:"))
    OUT_LN(command->toString())
    switch (command->getCommandType())
    {
    case Command::KOMENDY::RECEIVE_ZMIEN_STAN_PRZEKAZNIKA:
    {
        OUT_LN(F("RECEIVE_ZMIEN_STAN_PRZEKAZNIKA"))
        if (command->getDevice()->getType() == Device::TYPE::PRZEKAZNIK)
        {
            Przekaznik *tmp = (Przekaznik *)System::getSystem()->getDevice(command->getDevice()->getId());
            OUT_LN(F("PRZEKAZNIK"))
            OUT_LN(tmp->toString());
            if (tmp->getStan())
            {
                tmp->setStan(false);
            }
            else
            {
                tmp->setStan(true);
            }
            // OUT_LN(((Przekaznik *)command->getDevice())->toString());
            // if (((Przekaznik *)command->getDevice())->getStan())
            // {
            //     ((Przekaznik *)command->getDevice())->setStan(false);
            // }
            // else
            // {
            //     ((Przekaznik *)command->getDevice())->setStan(true);
            // }
        }
        else{
            return false;
        }
    }
    break;
    case Command::KOMENDY::RECEIVE_ZMIEN_STAN_ROLETY:
    {
        OUT_LN(F("RECEIVE_ZMIEN_STAN_ROLETY"))


        if (command->getDevice()->getType() == Device::TYPE::ROLETA)
        {
            Roleta *tmp = (Roleta*) System::getSystem()->getDevice(command->getDevice()->getId());
            OUT_LN(F("ROLETA"))
            OUT_LN(tmp->toString());
            if (command->getParams()[0] == 'U')
            {
                tmp->podnies();
                
            }
            else if (command->getParams()[0] == 'D')
            {
                tmp->opusc();
            }
            
            
            // if (tmp->getStan() == StanRolety::NIEOKRESLONY || tmp->getStan() == StanRolety::OPUSZCZONA)
            // {
            // }
            // else
            // {
            //     tmp->opusc();
            // }
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