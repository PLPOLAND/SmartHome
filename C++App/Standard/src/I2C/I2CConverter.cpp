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
        pinMode(2 + i, INPUT);
    }
    byte tmp = 1;
    byte adress = 0;
    for (byte i = 0; i < PINOW_NA_ADRES; i++) {
        adress += tmp * digitalRead(2 + i);
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
    OUT_LN("OnRecive");
    singleton->RecieveEvent(howManyBytes);
}

void I2CConverter::onRequestEvent() { 
    OUT_LN("onRequest")
    singleton->RequestEvent(); }

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
        if (!(buffReadSize < BUFFOR_IN_SIZE)) {
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
        //TODO dodać obsługę
        switch (komenda.getCommandType())
        {
            case Command::KOMENDY::NIC:
                {
                    komenda.setCommandType(Command::KOMENDY::SEND_REPLY);
                    byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                    komenda.setParams(params);
                    komenda.printParametry();
                    doWyslania.add(0, new Command(komenda));
                }
                break;
            case Command::KOMENDY::RECEIVE_ADD_THERMOMETR:
            {
                //Dodaje termometr do systemu o ile istnieje jakiś wolny, nie podłączony
                //Jeśli udało się dodać termometr dodaje do wysłania jego id na płytce w przeciwnym wypadku wyśle -1 -> czyli info o niepowodzeniu
                OUT_LN(F("RECEIVE_ADD_THERMOMETR"));

                komenda.setDevice(System::getSystem()->addDevice(Device::TYPE::TERMOMETR)); // Zwróć dodane urządzenie //TODO obsługa nullptr
                komenda.setCommandType(Command::KOMENDY::SEND_REPLY);
                komenda.setParams(((Termometr *)System::getSystem()->getDevice(komenda.getDevice()->getId()))->getAddres());
                komenda.printParametry();
                doWyslania.add(0, new Command(komenda)); //Dodaj komendę do wysłania na sam przód kolejki.

            }
            break;
            case Command::KOMENDY::RECEIVE_ADD_ROLETA:
            {
                OUT_LN(F("RECEIVE_ADD_ROLETA"));
                komenda.setDevice(System::getSystem()->addDevice(Device::TYPE::ROLETA, komenda.getParams()[0], komenda.getParams()[1]));
                komenda.setCommandType(Command::KOMENDY::SEND_REPLY);
                byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                params[0] = komenda.getDevice()->getId();
                komenda.setParams(params);
                doWyslania.add(0, new Command(komenda));
            }
            break;
            case Command::KOMENDY::RECEIVE_ADD_PRZYCISK:
            {
                OUT_LN(F("RECEIVE_ADD_PRZYCISK"));
                komenda.setDevice(System::getSystem()->addDevice(Device::TYPE::PRZYCISK, komenda.getParams()[0]));
                komenda.setCommandType(Command::KOMENDY::SEND_REPLY);
                byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                params[0] = komenda.getDevice()->getId();
                komenda.setParams(params);
                doWyslania.add(0, new Command(komenda));
            }
            break;
            case Command::KOMENDY::RECEIVE_ADD_PRZEKAZNIK:
            {
                OUT_LN(F("RECEIVE_ADD_PRZEKAZNIK"));
                komenda.setDevice(System::getSystem()->addDevice(Device::TYPE::PRZEKAZNIK, komenda.getParams()[0]));
                komenda.setCommandType(Command::KOMENDY::SEND_REPLY);
                byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                params[0] = komenda.getDevice()->getId();
                komenda.setParams(params);
                doWyslania.add(0, new Command(komenda));
                OUT_LN(F("END OF RECEIVE_ADD_PRZEKAZNIK"))
            }
            break;

            case Command::KOMENDY::RECEIVE_GET_TEMPERATURE:
            {
                OUT_LN(F("RECEIVE_GET_TEMPERATURE"));
                komenda.setCommandType(Command::KOMENDY::SEND_TEMPERATURA);
                komenda.setDevice((Termometr *)System::getSystem()->getDevice(komenda.getDevice()->getId()));
                doWyslania.add(0, new Command(komenda));
            }
            break;
            case Command::KOMENDY::RECEIVE_ZMIEN_STAN:
                break;
            case Command::KOMENDY::RECEIVE_IS_INIT:
            {
                OUT_LN(F("RECEIVE_IS_INIT"));
                komenda.setCommandType(Command::KOMENDY::SEND_REPLY);
                byte params[8] = {0, 0, 0, 0, 0, 0, 0, 0};
                params[0] = System::is_init();
                komenda.setParams(params);
                doWyslania.add(0, new Command(komenda));
            }
            break;
            case Command::KOMENDY::RECEIVE_INIT:
            {
                OUT_LN(F("RECEIVE_INIT"));
                komenda.setCommandType(Command::KOMENDY::SEND_REPLY);
                byte params[8] = {1, 0, 0, 0, 0, 0, 0, 0};// potwierdź odebranie komendy
                komenda.setParams(params);

                for (int i = 0; i < doWyslania.size(); i++)//oczyść listę komend do wysłania jako że będą już nie aktualne
                {
                    delete doWyslania.get(i);
                }
                doWyslania.clear();

                doWyslania.add(0, new Command(komenda));//dodaj wysłanie potwierdzenia otrzymania komendy
                
                System::reinit_system();//reinicjalizuj system!

            }
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
    if (doWyslania.size()>0)
    {
        Command command(doWyslania.get(0));//pobierz z "kolejki"
        doWyslania.remove(0);//usuń z kolejki
        switch (command.getCommandType())
        {
            case Command::KOMENDY::SEND_TEMPERATURA:
                {
                    OUT_LN(F("SEND_TEMPERATURA"));
                    // OUT_LN("freeMemory(): ");
                    // OUT_LN(freeMemory());
                    OUT("Temperatura: ");
                    OUT_LN(((Termometr *)command.getDevice())->getTemperature());
                    String tmp = String(((Termometr *)command.getDevice())->getTemperature(), 2U);
                    OUT("afterString: ");
                    OUT_LN(tmp);
                    // OUT_LN(termometry.get(id)->getTemperature());
                    Wire.write(command.getDevice()->getId()); // wyslij ID Termometru na płytce
                    for (byte i = 0; i < tmp.length(); i++)
                    {
                        Wire.write(tmp.charAt(i)); // wyslij kolejne cyfry temperatury
                    }
                    break;
                }
            case Command::KOMENDY::SEND_REPLY:
            {
                OUT_LN(F("SEND_REPLY"));
                    Wire.write(command.getParams(),8);
                    Wire.clearWriteError();
                for (byte i =0; i < BUFFOR_OUT_SIZE; i++)
                {
                        // Wire.write(command->getParams()[i]);
                    OUT((int)command.getParams()[i]);
                    OUT(" ");
                }
                OUT_LN();
                break;
            } 
            case Command::KOMENDY::SEND_STATUS:
                break;
            default:
                break;
        }
        OUT_LN(freeMemory());
        
    }
    else
    {
        OUT_LN(F("Wire.write('0')"));
        Wire.write('0');
    }
}
