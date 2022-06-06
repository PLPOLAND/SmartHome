#include <I2C/I2CConverter.h>
#include <Kontener.h>
#include <FreeMemory.h>
#include "Command.h"

// TODO poprawic odwołania do kontenerów
I2CConverter* I2CConverter::singleton = nullptr;
LinkedList<Command*> I2CConverter::doWyslania = LinkedList<Command*>();

I2CConverter::I2CConverter()
{
    // OUT_LN(freeMemory());
    // OUT_LN("I2CConverter()");
    static_assert(PINOW_NA_ADRES >= 1, "ZA MALO PINOW NA ADRESS");
    static_assert(ONEWIRE_BUS > (PINOW_NA_ADRES + 1), "BUS na zajetym pinie");
    for (byte i = 0; i < PINOW_NA_ADRES; i++) {
        pinMode(2 + i, INPUT_PULLUP);
    }
    byte tmp = 1;
    byte adress = 7;
    for (byte i = 0; i < PINOW_NA_ADRES; i++) {
        adress += tmp * (digitalRead(2 + i) == HIGH ? 0:1);
        tmp *= 2;
    }

    OUT_LN(freeMemory());
    OUT_LN(F("Wystartowano na:"));
    OUT_LN((int)adress);
    Wire.begin(adress);
    
}

I2CConverter::~I2CConverter()
{
    Wire.end();
}
void I2CConverter::begin(){
    // system = System::getSystem();
}

I2CConverter* I2CConverter::getInstance()
{
    if (singleton == nullptr) {
        singleton = new I2CConverter();
    }

    return singleton;
}

void I2CConverter::onRecieveEvent(int howManyBytes)
{
    // OUT_LN("OnRecive");
    singleton->RecieveEvent(howManyBytes);
}

void I2CConverter::onRequestEvent() { 
    // OUT_LN("onRequest")
    singleton->RequestEvent();
    // OUT_LN("END onRequest")
}

void I2CConverter::RecieveEvent(int howManyBytes)
{
    byte buffReadSize = 0;
    OUT(F("howmanybytes: "));
    OUT_LN(howManyBytes);
    if (howManyBytes == 0)
    {
        Wire.read();
    }
    
    while (0 < Wire.available()) {
        buf[buffReadSize++] = Wire.read();
        OUT("i:" );
        OUT(buffReadSize-1);
        OUT("  buf:" );
        OUT_LN(buf[buffReadSize - 1]);
        if (!(buffReadSize <= BUFFOR_IN_SIZE)) {
            buffReadSize--;
            break; // TODO: Obsługa błędu???
            while (0 < Wire.available())
                ;
        }
    }
    
    if (!(howManyBytes == 1 && buf[0] == 0)) // 0 jest wysyłane w celu sprawdzenia czy urzadzenie o takim adresie jest podpięte do systemu
    {
        Command komenda;
        komenda.convert(buf, buffReadSize);
        OUT("Komenda: ");
        OUT_LN((int)komenda.getCommandType());
        Command* komendaZwrotna = new Command();
        //TODO dodać obsługę
        switch (komenda.getCommandType())
        {
            case Command::KOMENDY::NIC:
                {
                    komendaZwrotna->setCommandType(Command::KOMENDY::SEND_REPLY);
                    byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                    komendaZwrotna->setParams(params);
                    komendaZwrotna->printParametry();
                    doWyslania.add(0, komendaZwrotna);
                }
                break;
            case Command::KOMENDY::RECEIVE_ADD_THERMOMETR:
            {
                //Dodaje termometr do systemu o ile istnieje jakiś wolny, nie podłączony
                //Jeśli udało się dodać termometr dodaje do wysłania jego id na płytce w przeciwnym wypadku wyśle -1 -> czyli info o niepowodzeniu
                OUT_LN(F("REC_ADD_THERMOMETR"));
                Device *tmpDev = System::getSystem()->addDevice(Device::TYPE::TERMOMETR);
                komendaZwrotna->setDevice(tmpDev); // Zwróć dodane urządzenie //TODO obsługa nullptr
                komendaZwrotna->setCommandType(Command::KOMENDY::SEND_REPLY);
                komendaZwrotna->setParams(((Termometr *)tmpDev)->getAddres());
                komendaZwrotna->printParametry();
                doWyslania.add(0, komendaZwrotna); //Dodaj komendę do wysłania na sam przód kolejki.

            }
            break;
            case Command::KOMENDY::RECEIVE_ADD_ROLETA:
            {
                OUT_LN(F("REC_ADD_ROLETA"));
                OUT(F("pinUp: "))
                OUT_LN(komenda.getParams()[0]);
                OUT(F("pinDown: "))
                OUT_LN(komenda.getParams()[1]);
                komendaZwrotna->setDevice(System::getSystem()->addDevice(Device::TYPE::ROLETA, komenda.getParams()[0], komenda.getParams()[1]));
                komendaZwrotna->setCommandType(Command::KOMENDY::SEND_REPLY);
                byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                params[0] = komendaZwrotna->getDevice()->getId();
                params[1] = ((Roleta *)komendaZwrotna->getDevice())->getSwitchUp()->getId();
                params[2] = ((Roleta *)komendaZwrotna->getDevice())->getSwitchDown()->getId();
                komendaZwrotna->setParams(params);
                doWyslania.add(0, komendaZwrotna);
            }
            break;
            case Command::KOMENDY::RECEIVE_ADD_PRZYCISK:
            {
                OUT_LN(F("REC_ADD_PRZYCISK"));
                komendaZwrotna->setDevice(System::getSystem()->addDevice(Device::TYPE::PRZYCISK, komenda.getParams()[0]));
                komendaZwrotna->setCommandType(Command::KOMENDY::SEND_REPLY);
                byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                params[0] = komendaZwrotna->getDevice()->getId();
                komendaZwrotna->setParams(params);
                doWyslania.add(0, komendaZwrotna);
            }
            break;
            case Command::KOMENDY::RECEIVE_ADD_PRZEKAZNIK:
            {
                OUT_LN(F("REC_ADD_PRZEKAZNIK"));
                komendaZwrotna->setDevice(System::getSystem()->addDevice(Device::TYPE::PRZEKAZNIK, komenda.getParams()[0]));
                komendaZwrotna->setCommandType(Command::KOMENDY::SEND_REPLY);
                byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                params[0] = komendaZwrotna->getDevice()->getId();
                komendaZwrotna->setParams(params);
                doWyslania.add(0, komendaZwrotna);
            }
            break;

            case Command::KOMENDY::RECEIVE_GET_TEMPERATURE:
            {
                OUT_LN(F("REC_GET_TEMPERATURE"));
                komendaZwrotna->setCommandType(Command::KOMENDY::SEND_TEMPERATURA);
                komendaZwrotna->setParams(komenda.getParams());

                
                doWyslania.add(0, komendaZwrotna);
            }
            break;
            case Command::KOMENDY::RECEIVE_ZMIEN_STAN_PRZEKAZNIKA:
            {
                OUT_LN(F("REC_ZMIEN_STAN_PRZEKAZNIKA"));
                komendaZwrotna->setCommandType(Command::KOMENDY::SEND_REPLY);

                Przekaznik* p = (Przekaznik*) System::getSystem()->getDevice(komenda.getParams()[0]);
                OUT_LN(F("Przekaznik: "))
                OUT_LN(p->toString())
                p->setStan(komenda.getParams()[1] == 1 ? true : false);
                byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                params[0]= 'O';
                komendaZwrotna->setParams(params);
                doWyslania.add(0, komendaZwrotna);
            }
                break;
            case Command::KOMENDY::RECEIVE_ZMIEN_STAN_ROLETY:
            {
                OUT_LN(F("REC_ZMIEN_STAN_ROLETY"));
                komendaZwrotna->setCommandType(Command::KOMENDY::SEND_REPLY);
                byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};

                Roleta *r = (Roleta *)System::getSystem()->getDevice(komenda.getParams()[0]);
                if (komenda.getParams()[1] == 'U')
                {
                    r->podnies();
                    params[0] = 'O';
                }
                else if (komenda.getParams()[1]== 'D')
                {
                    r->opusc();
                    params[0] = 'O';
                }
                else
                {
                    params[0] = 'E';
                }
                komendaZwrotna->setParams(params);
                doWyslania.add(0, komendaZwrotna);
            }
            break;
            case Command::KOMENDY::RECEIVE_CHECK_INIT:
            {
                OUT_LN(F("REC_CHECK_INIT"));
                komendaZwrotna->setCommandType(Command::KOMENDY::SEND_REPLY);
                byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                params[0] = System::is_init() == true ? 1:0;
                komendaZwrotna->setParams(params);
                doWyslania.add(0, komendaZwrotna);
            }
            break;
            case Command::KOMENDY::RECEIVE_INIT:
            {
                OUT_LN(F("REC_INIT"));
                komendaZwrotna->setCommandType(Command::KOMENDY::SEND_REPLY);
                byte params[8] = {1, 0, 0, 0, 0, 0, 0, 0};// potwierdź odebranie komendy
                komendaZwrotna->setParams(params);

                for (int i = 0; i < doWyslania.size(); i++)//oczyść listę komend do wysłania jako że będą już nie aktualne
                {
                    delete doWyslania.get(i);
                }
                doWyslania.clear();

                System::reinit_system();//reinicjalizuj system!
                
                doWyslania.add(0, komendaZwrotna);//dodaj wysłanie potwierdzenia otrzymania komendy

            }
            break;
            case Command::KOMENDY::RECEIVE_ADD_PRZYCISK_LOCAL_FUNCTION:
            {
                OUT_LN(F("REC_ADD_PRZYCISK_LOCAL_FUNCTION"));
                komendaZwrotna->setCommandType(Command::KOMENDY::SEND_REPLY);
                byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                Przycisk *przycisk = (Przycisk *) System::getSystem()->getDevice(komenda.getParams()[0]);
                if (przycisk != nullptr)
                {
                    Device* dev = System::getSystem()->getDevice(komenda.getParams()[1]);
                    if (dev!=nullptr)
                    {
                        Command* funkcja = new Command;
                        funkcja->setDevice(dev);
                        OUT(F("device type: "))
                        OUT_LN(dev->getType())
                        OUT_LN(funkcja->getDevice()->getType())
                        switch (dev->getType())
                        {
                            case Device::PRZEKAZNIK:
                                {
                                    funkcja->setCommandType(Command::KOMENDY::RECEIVE_ZMIEN_STAN_PRZEKAZNIKA);
                                    byte parametry[8] = {0, 0, 0, 0, 0, 0, 0, 0}; // TODO może da się zmniejszyć rozmiar tablicy?
                                    funkcja->setParams(parametry);
                                }
                                break;
                            case Device::ROLETA:
                                {
                                    funkcja->setCommandType(Command::KOMENDY::RECEIVE_ZMIEN_STAN_ROLETY);
                                    byte parametry[8] = {komenda.getParams()[2], 0, 0, 0, 0, 0, 0, 0};//TODO może da się zmniejszyć rozmiar tablicy?
                                    funkcja->setParams(parametry);
                                }
                                break;
                            default:
                                break;
                        }
                        przycisk->dodajFunkcjeKlikniecia(funkcja, komenda.getParams()[3]);
                    }
                    else
                    {
                        params[0] = 'E';//ERROR
                    }

                }
                else{
                    params[0] = 'E';//ERROR
                }
                komendaZwrotna->setParams(params);
                doWyslania.add(0, komendaZwrotna); // dodaj wysłanie potwierdzenia otrzymania komendy
                break;
            }

            case Command::KOMENDY::RECEIVE_REMOVE_PRZYCISK_LOCAL_FUNCTION:
            {
                OUT_LN(F("REC_RM_PRZYCISK_L_FUNCTION"));
                komendaZwrotna->setCommandType(Command::KOMENDY::SEND_REPLY);
                byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                Przycisk *przycisk = (Przycisk *)System::getSystem()->getDevice(komenda.getParams()[0]);
                if (przycisk != nullptr)
                {
                    przycisk->usunFunkcjeKlikniecia(komenda.getParams()[1]);//Check
                }
                else
                {
                    params[0] = 'E'; // ERROR
                }

                komendaZwrotna->setParams(params);
                doWyslania.add(0, komendaZwrotna); // dodaj wysłanie potwierdzenia otrzymania komendy
            }
            break;
            case Command::KOMENDY::RECIEVE_DEVICES_STATUS:
            {
                OUT_LN(F("REC_DEVICES_STATUS"));
                komendaZwrotna->setCommandType(Command::KOMENDY::SEND_REPLY);

                Device *p =System::getSystem()->getDevice(komenda.getParams()[0]);
                if (p != nullptr)
                {
                    
                    switch (p->getType())
                    {
                    case Device::TYPE::PRZEKAZNIK:
                        {
                            byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                            params[0] = ((Przekaznik*) p)->getStan(); // 0/1
                            komendaZwrotna->setParams(params);
                            doWyslania.add(0, komendaZwrotna);
                        }
                        break;
                    case Device::TYPE::ROLETA:
                        {
                            byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                            StanRolety tmp = ((Roleta *)p)->getStan();
                            if (tmp == StanRolety::NIEOKRESLONY)
                            {
                                params[0] = 'M';
                            }
                            else if (tmp == StanRolety::PODNIESIONA)
                            {
                                params[0] = 'U';
                            }
                            else if (tmp == StanRolety::OPUSZCZONA)
                            {
                                params[0] = 'D';
                            }
                            
                            komendaZwrotna->setParams(params);
                            doWyslania.add(0, komendaZwrotna);
                        }
                        break;
                    default:
                        break;
                    }

                }
                else
                {
                    byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                    komendaZwrotna->setParams(params);
                    doWyslania.add(0, komendaZwrotna);
                }
                
            }
            break;

            case Command::KOMENDY::RECEIVE_GET:

                break;

            default:
                break;
        };

        for (byte i = 0; i < BUFFOR_IN_SIZE; i++) {//clear buff
            buf[i] = 0;
        }
        OUT_LN(freeMemory());
    }
    
}
//TODO kolejka komend
void I2CConverter::RequestEvent()
{
    Command* command;
    if (doWyslania.size()>0)
    {
        command = doWyslania.remove(0);//usuń z kolejki
        switch (command->getCommandType())
        {
            case Command::KOMENDY::SEND_TEMPERATURA:
                {
                    // OUT_LN(F("SEND_TEMPERATURA"));
                    // OUT_LN("freeMemory(): ");
                    // OUT_LN(freeMemory());
                    // OUT("Temperatura: ");
                    for (byte i = 0; i < 8; i++)
                    {
                        
                        OUT(command->getParams()[i])
                        OUT(" ")
                    }
                    Wire.write(0); // wyslij ID Termometru na płytce
                    for (byte i = 0; i < 7; i++)
                    {
                        
                        Wire.write(command->getParams()[i]);
                        // OUT(" ")
                    }
                    


                    // OUT_LN(((Termometr *)command->getDevice())->getTemperature());
                    // String tmp = String(((Termometr *)command->getDevice())->getTemperature(), 2U);
                    // OUT("afterString: ");
                    // OUT_LN(tmp);
                    // // OUT_LN(termometry->get(id)->getTemperature());
                    // Wire.write(0); // wyslij ID Termometru na płytce
                    // for (byte i = 0; i < tmp.length(); i++)
                    // {
                    //     Wire.write(tmp.charAt(i)); // wyslij kolejne cyfry temperatury
                    // }
                    break;
                }
            case Command::KOMENDY::SEND_REPLY:
            {
                OUT_LN(F("SEND_REPLY"));
                Wire.write(command->getParams(), 8);
                Wire.clearWriteError();
                command->printParametry();
                OUT_LN(F("END SEND_REPLY"))
                OUT_LN();
                break;
            } 
            case Command::KOMENDY::SEND_STATUS:
                break;
            default:
                break;
        }
        OUT_LN(freeMemory());
        // OUT_LN("END OF FREEMEMORY")
    }
    else
    {
        OUT_LN(F("Wire.write('0')"));
        Wire.write('0');
    }
    delete command;
    
}

/*

addRoom Marek
addTermometr Marek

updateTemperature 40 255 30 49 0 22 2 171


*/