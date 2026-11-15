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
    time.time(STOP);
    time2.time(STOP);
    stan = BRAK_AKCJI;
    klikniecia = 0;
    for (byte i = 0; i < MAX_NUMBER_OF_BUTTON_FUNCTIONS; i++)
    {
        funkcje_klikniecia[i] = const_cast<Command *>(zapychacz);
    }
}

Przycisk::~Przycisk()
{
    for (byte i = 0; i < MAX_NUMBER_OF_BUTTON_FUNCTIONS; i++)
    {
        if (funkcje_klikniecia[i] != zapychacz)
        {
            delete funkcje_klikniecia[i];
        }
    }
}

/**
 * 
 * \param pin pin nasłuchu stanu
 * 
 */
bool Przycisk::begin(byte pin, bool czyPomijacPierwszy = false)
{
    Device(Device::TYPE::PRZYCISK);
    this->setCzyPominac(czyPomijacPierwszy);

    if(this->setPin(pin)){
        time.time(STOP);
        time2.time(STOP);
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

    if (tmpStan == 1 && stan == PUSZCZONY && !time.available()) //Przyciśniecie po raz kolejny
    {

        //OUT_LN(F("next click "));
        time.begin(BUTTON_CLICK_TIME);
        stan = PRZYCISNIETY;
        klikniecia++;
        // OUT(F("clicks: "))
        // OUT_LN(klikniecia);
        this->dioda.on(200);
    }
    else if (tmpStan == 1 && stan == BRAK_AKCJI) //Przyciśniecie po raz pierwszy
    {
        //OUT_LN(F("prawiewcisniety"));
        stan = PRAWIE_WCISNIETY;
        time2.begin(50);
        // klikniecia = 1;
        this->dioda.on(200);
    }
    else if (tmpStan == 1 && stan == PRAWIE_WCISNIETY && time2.available()) //Przyciśniecie po raz pierwszy
    {
        //OUT_LN(F("first click"));
        stan = PRZYCISNIETY;
        time.begin(BUTTON_CLICK_TIME);
        klikniecia = 1;
        this->dioda.on();
    }
    else if (stan == PRAWIE_WCISNIETY && tmpStan == 0 && !time2.available())
    {
        stan = BRAK_AKCJI;
        //OUT_LN(F("PUSZCZONY P WCISNIETY"));
        time.time(STOP);
        time2.time(STOP);
    }

    if (stan == PRZYCISNIETY && tmpStan == 0 )//Zakończenie kliknięcia bez przytrzymania
    {

        //OUT_LN(F("puszczony bez przytrzymania"));
        stan = PUSZCZONY;

        

    }
    
    
    if (stan == PRZYCISNIETY && tmpStan == 1 && time.available()) //Wykrycie przytrzymania
    {

        //OUT_LN(F("Wykrycie Przytrzymanie"));
        stan = PRZYTRZYMANY;
        dioda.on();
    }
    if (stan == PRZYTRZYMANY && tmpStan == 0) //Puszczenie po przytrzymaniu
    { 

        //OUT_LN(F("Puszczony po przytrzymaniu"));
        time.time(STOP);
        stan = BRAK_AKCJI;
        dioda.off();
        this->wykonaj();//dla BRAK_AKCJI
        klikniecia = 0;
    }
    else if (stan == PRZYTRZYMANY && tmpStan == 1) //Przytrzymywanie
    {
        this->dioda.on();
        // OUT_LN(F("Przytrzymanie"));
        this->wykonaj();//dla PRZYTRZYMANY
        //TODO
    }

    if (time.available() && stan == PUSZCZONY) {

        //OUT_LN(F("Koniec okresu klikniec"));
        time.time(STOP);
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
    dioda.tic();
    // static int i = 0;
    // if(i++>10000){
    //     //OUT_LN(F("TIC"));
    // OUT_LN(time.time());
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
            //TODO dorobić dla przytrzymanego
        }
        break;
    case PUSZCZONY:// Przycisk nie był przytrzymywany i skończył się czas na kolejne przyciśnięcie
        {
            //OUT_LN(F("Wykonywanie PUSZCZONY"))
            // OUT(F("klikniec: "))
            // OUT_LN(klikniecia);
            Command *command = nullptr;
            if (klikniecia >= 0 && klikniecia < MAX_NUMBER_OF_BUTTON_FUNCTIONS)
            {
                // wskaźnik może być podmieniany w przerwaniu I2C (konfiguracja funkcji) - odczyt 2 bajtów niepodzielnie
                ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
                {
                    command = funkcje_klikniecia[klikniecia];
                }
            }
            if (command != nullptr && command->getCommandType() != Command::KOMENDY::NIC)
            {
                this->runCommand(command);
            }
            else
            {
                byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                params[0] = 'C';
                params[1] = this->getId(); // Button ID
                params[2] = klikniecia;    // ilość kliknięć przed przytrzymaniem
                params[3] = 'C';           // kliknięcie
                I2CConverter::getInstance()->addReplyToSent(params); // alokacja przy wyłączonych przerwaniach
            }
            
            
        // System::getSystem()->runCommand(command);
        }
        break;
    case BRAK_AKCJI://Przycisk był przytrzymany i został właśnie puszczony
        {
            // OUT_LN(F("Po przytrzymaniu"))
            // OUT(F("klikniec: "))
            // OUT_LN(klikniecia);
            byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
            params[0] = 'C';
            params[1] = this->getId();  //Button ID
            params[2] = klikniecia;     //ilość kliknięć przed przytrzymaniem
            params[3] = 'P';            //po przytrzymaniu
            I2CConverter::getInstance()->addReplyToSent(params); // alokacja przy wyłączonych przerwaniach

        }
        break;
    default:
        break;
    }
    return true; //TODO obsluga bledow?
}

bool Przycisk::dodajFunkcjeKlikniecia(Command* command, byte klikniec){
    //OUT_LN(F("DODAJ FUNKCJE KLIKNIECIE"))
    // OUT(F("Przyski id: "))
    // OUT_LN(this->getId());
    // OUT("Klikniec: ")
    // OUT_LN(klikniec);
    // OUT_LN(command->toString());
    // OUT(F("Device ID: "))
    // OUT_LN(command->getDevice()->getId());
    // OUT_LN();
    if (klikniec >= MAX_NUMBER_OF_BUTTON_FUNCTIONS)
    {
        return false;
    }
    Command* tmp = new Command;
    tmp->makeCopy(command);
    if (funkcje_klikniecia[klikniec] != zapychacz)
    {
        delete funkcje_klikniecia[klikniec]; // nadpisywana funkcja - wcześniej tu ciekła pamięć
    }
    funkcje_klikniecia[klikniec] = tmp;
    //OUT_LN(F("DODANO KOMENDE"));

    return true;
}
bool Przycisk::usunFunkcjeKlikniecia(byte klikniec){
    //OUT_LN(F("USUN FUNKCJE KLIKNIECIE"))
    // OUT(F("Przyski id: "))
    // OUT_LN(this->getId());
    // OUT("Klikniec: ")
    // OUT_LN(klikniec);
    // OUT_LN();
    if (klikniec >= MAX_NUMBER_OF_BUTTON_FUNCTIONS)
    {
        return false;
    }
    if (funkcje_klikniecia[klikniec] != zapychacz)
    {
        delete funkcje_klikniecia[klikniec];
    }
    funkcje_klikniecia[klikniec] = const_cast<Command *>(zapychacz);
    //OUT_LN(F("DODANO KOMENDE"));

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
    // OUT_LN()
    //OUT_LN(F("RUN_COMMAND:"))
    // OUT_LN(command->toString())

    if (this->isCzyPominac())
    {
        //OUT_LN(F("POMIJANIE"))
        this->setCzyPominac(false);
        return true;
    }
    

    switch (command->getCommandType())
    {
    case Command::KOMENDY::RECEIVE_ZMIEN_STAN_PRZEKAZNIKA:
    {
        //OUT_LN(F("REC_ZMIEN_STAN_PRZEKAZNIKA"))
        if (command->getDevice()->getType() == Device::TYPE::PRZEKAZNIK)
        {
            Przekaznik *tmp = (Przekaznik *)System::getSystem()->getDevice(command->getDevice()->getId());
            //OUT_LN(F("PRZEKAZNIK"))
            // OUT_LN(tmp->toString());
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
        //OUT_LN(F("REC_ZMIEN_STAN_ROLETY"))


        if (command->getDevice()->getType() == Device::TYPE::ROLETA)
        {
            Roleta *tmp = (Roleta*) System::getSystem()->getDevice(command->getDevice()->getId());
            //OUT_LN(F("ROLETA"))
            OUT_LN(tmp->toString());
            if (command->getParams()[0] == 'U')
            {
                tmp->podnies();
                
            }
            else if (command->getParams()[0] == 'D')
            {
                tmp->opusc();
            }
            else if (command->getParams()[0] == 'S')
            {
                tmp->stop();
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

void Przycisk::setCzyPominac(bool pominac){
    this->czyPominac = pominac;
}

bool Przycisk::isCzyPominac(){
    return this->czyPominac;
}