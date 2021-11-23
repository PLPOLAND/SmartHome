#include <devices/Roleta.h>
#include <devices/Przekaznik.h>

/**
 * 
 * Pusty konstruktor
 * @warning Nie przypisuje pinów Przekaźnikom , należy zrobić to samemu!;
 * 
 * */
Roleta::Roleta()
{
    Device(Device::TYPE::ROLETA);
    akcja = Akcja::POSTOJ;
    time = new Timer();
    time->time(STOP);
}

Roleta::~Roleta()
{
    delete time;
}

/**
 * 
 * Konstruktor 
 * 
 * @param pinup pin przekaznika od ruchu w gore
 * @param pindown pin przekaznika od ruchu w dol
 * 
 */
Roleta::Roleta(byte pinup, byte pindown)
{
    Device(Device::TYPE::ROLETA);
    this->setPinUp(pinup);
    this->setPinDown(pindown);

    akcja = Akcja::POSTOJ;
    time = new Timer();
    time->time(STOP);
}
/**
 * 
 * Konstruktor 
 * 
 * @param id id urzadzenia w systemie
 * @param pinup pin przekaznika od ruchu w gore
 * @param pindown pin przekaznika od ruchu w dol
 * 
 */
Roleta::Roleta(byte id, byte pinup, byte pindown)
{
    Device(Device::TYPE::ROLETA, id);
    this->setPinUp(pinup);
    this->setPinDown(pindown);

    akcja = Akcja::POSTOJ;
    time = new Timer();
    time->time(STOP);
}

byte Roleta::getPinUp() { return this->p_up.getPin(); };
void Roleta::setPinUp(byte pin)
{
    this->p_up.setPin(pin);
    this->p_up.setStan(false);
};
byte Roleta::getPinDown() { return this->p_down.getPin(); };
void Roleta::setPinDown(byte pin)
{
    this->p_down.setPin(pin);
    this->p_down.setStan(false);
};
StanRolety Roleta::getStan() { return stan; };

/**
 * wywołuje rzeczy które powinny byc wywolywane w kazdym obrocie loop 
 * TODO: logistyka
 * */
void Roleta::tic()
{
    if (akcja == Akcja::PODNOSZENIE_CALKOWITE && time->available())
    {
        this->stop();
        this->stan = StanRolety::PODNIESIONA;
        
    }
    else if (akcja == Akcja::OPUSZCZANIE_CALKOWITE && time->available())
    {
        this->stop();
        this->stan = StanRolety::OPUSZCZONA;
    }
    
}


/**
 * Calkowite podniesienie rolety (z timerem)
 */
void Roleta::podnies()
{
    akcja = Akcja::PODNOSZENIE_CALKOWITE;
    time->begin(CZAS_CALKOWITEJ_ZMIANY_POLOZENIA);
    stan = StanRolety::NIEOKRESLONY;
    setPinUpState(true); //Zalacza pin sterowania roleta do gory i wylacza pin sterowania roleta do dolu
};
/**
 * Calkowite opuszczenie rolety (z timerem)
 */
void Roleta::opusc()
{
    akcja = Akcja::OPUSZCZANIE_CALKOWITE;
    time->begin(CZAS_CALKOWITEJ_ZMIANY_POLOZENIA);
    stan = StanRolety::NIEOKRESLONY;
    setPinDownState(true); //Zalacza pin sterowania roleta do dolu i wylacza pin sterowania roleta do gory
};
/**
 * Ruch rolety w gore bez ograniczen
 */
void Roleta::up()
{
    akcja = Akcja::PODNOSZENIE;
    stan = StanRolety::NIEOKRESLONY;
    setPinUpState(true); //Zalacza pin sterowania roleta do gory i wylacza pin sterowania roleta do dolu
};
/**
 * Ruch rolety w dol bez ograniczen
 */
void Roleta::down()
{
    akcja = Akcja::OPUSZCZANIE;
    stan = StanRolety::NIEOKRESLONY;
    setPinDownState(true); //Zalacza pin sterowania roleta do dolu i wylacza pin sterowania roleta do gory
};

/**
 * Zatrzymanie ruchu rolety 
 */
void Roleta::stop()
{
    akcja = Akcja::POSTOJ;
    time->time(STOP); //zeruj minutnik
    forcePinDownState(false);
    forcePinUpState(false);
};

/**
 * Konwertuje bool na stan niski/wysoki dla pinUp, NIE zmienia stanu drugiego pinu
 * TODO: Sprawdzić czy powinoo być LOW dla "wylaczenia"
 */
void Roleta::forcePinUpState(bool stan)
{
    this->p_up.setStan(stan);
}

/**
 * Konwertuje bool na stan niski/wysoki dla pinDown, NIE zmienia stanu drugiego pinu
 * TODO: Sprawdzić czy powinoo być LOW dla "wylaczenia"
 */
void Roleta::forcePinDownState(bool stan)
{
    this->p_down.setStan(stan);
}

/**
 * Konwertuje bool na stan niski/wysoki dla pinUp
 * TODO: Sprawdzić czy powinoo być LOW dla "wylaczenia"
 */
void Roleta::setPinUpState(bool stan)
{
    if (stan == false) {
        this->p_up.setStan(false);
        this->p_down.setStan(true);
    } else
    {
        this->p_up.setStan(true);
        this->p_down.setStan(false);
    }
}

/**
 * Konwertuje bool na stan niski/wysoki dla pinDown
 * TODO: Sprawdzić czy powinoo być LOW dla "wylaczenia"
 */
void Roleta::setPinDownState(bool stan)
{
    if (stan == false) {
        this->p_up.setStan(true);
        this->p_down.setStan(false);
    }
    else
    {
        this->p_up.setStan(false);
        this->p_down.setStan(true);
    }
}