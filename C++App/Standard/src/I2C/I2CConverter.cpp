#include <I2C/I2CConverter.h>
#include <Kontener.h>
#include <FreeMemory.h>
#include "Command.h"

// TODO poprawic odwołania do kontenerów
I2CConverter* I2CConverter::singleton = nullptr;
// System *I2CConverter::system = nullptr;

I2CConverter::I2CConverter()
{
    // OUTPUT_LN(freeMemory());
    // OUTPUT_LN("I2CConverter()");
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

    OUTPUT_LN(freeMemory());
    OUTPUT_LN(F("Wystartowano na:"));
    OUTPUT_LN((int)adress);
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
    OUTPUT_LN("OnRecive");
    singleton->RecieveEvent(howManyBytes);
}

void I2CConverter::onRequestEvent() { singleton->RequestEvent(); }

void I2CConverter::RecieveEvent(int howManyBytes)
{
    byte buffReadSize = 0;
    OUTPUT(F("howmanybytes: "));
    OUTPUT_LN(howManyBytes);
    while (0 < Wire.available()) {
        buf[buffReadSize++] = Wire.read();
        OUTPUT("i:");
        OUTPUT(buffReadSize-1);
        OUTPUT("buf:");
        OUTPUT_LN(buf[buffReadSize - 1]);
        if (!(buffReadSize < BUFFOR_IN_SIZE)) {
            buffReadSize--;
            break; // TODO: Obsługa błędu???
            while (0 < Wire.available())
                ;
        }
    }
    Command komenda;
    komenda.convert(buf,buffReadSize);
    //TODO dodać obsługę
    switch (komenda.komenda) {
    case Command::KOMENDY::ADD_THERMOMETR:
        this->addTermometr();
        break;
    case Command::KOMENDY::GET_TEMPERATURE:
        this->coWyslac = DoWyslania::TEMPERATURA;//TODO PRZEROBIĆ NA COMMAND
        break;
    default:
        break;
    };
    for (byte i = 0; i < BUFFOR_IN_SIZE; i++) {
        buf[i] = 0;
    }
    OUTPUT_LN(freeMemory());
}
//TODO kolejka komend
void I2CConverter::RequestEvent()
{
    switch (singleton->coWyslac) {
    case DoWyslania::TEMPERATURA:
        // OUTPUT_LN("PrintTEMP1");
        printTemperature(0);//TODO wczytywanie ID z komendy??
        // OUTPUT_LN("PrintTEMP");
        this->coWyslac = DoWyslania::NIC;
        break;
    case DoWyslania::REPLY: {
        byte i = 0;
        while (buf_out[i] != 0 && i < BUFFOR_OUT_SIZE) {
            Wire.write(buf_out[i]);
            OUTPUT((int)buf_out[i]);
            i++;
        }
        OUTPUT_LN();
    } break;
    case DoWyslania::STATUS:
        break;
    default:
        break;
    }
    OUTPUT_LN(freeMemory());
}

// Komendy I2CConverter::find_command(byte size)
// {
//     switch (size) {
//     case 1:
//         break;
//     case 2:
//         if (buf[0] == 'A') {
//             if (buf[1] == 'T') {
//                 return Komendy::DODAJ_TERMOMETR;
//             }

//         } else if (buf[0] == 'T') {
//             Serial.println(F("Temperatura"));
//             idTermometru = buf[1];
//             return Komendy::TEMPERATURA;
//         }

//         break;
//     default:
//         break;
//     }
//     return Komendy::NIC;
// }

///Old version
// void I2CConverter::addTermometr()
// {
//     Termometr* tmp = (Termometr*)malloc(sizeof(Termometr));
//     tmp->begin();
//     Serial.println("newTermometr");
//     // Serial.println(tmp->getID());
//     // tmp* = Termometr();
//     if (!tmp->isCorrect()) {
//         delete tmp;
//         this->buf_out[0] = -1; // Zwróć ID termometru na płytce
//         this->coWyslac = DoWyslania::REPLY;
//     } else {
//         Serial.println(tmp->getTemperature());
//         this->termometry.add(tmp);
//         this->buf_out[0] = termometry.get(termometry.size() - 1)->getID(); // Zwróć ID termometru na płytce
//         this->coWyslac = DoWyslania::REPLY;
//     }
// }
void I2CConverter::addTermometr()
{
    this->buf_out[0] = System::getSystem()->addDevice(Device::TYPE::TERMOMETR); // Zwróć otrzymane id
    this->coWyslac = DoWyslania::REPLY;
}

void I2CConverter::printTemperature(byte id)
{
    // OUTPUT_LN("freeMemory(): ");
    // OUTPUT_LN(freeMemory());
    String tmp = String(((Termometr *)System::getSystem()->getDevice(id))->getTemperature(), 2);
    // OUTPUT("afterString: ");
    // OUTPUT_LN(*tmp);
    // OUTPUT_LN(termometry.get(id)->getTemperature());
    Wire.write(id);  // wyslij ID Termometru na płytce
    for (byte i = 0; i < tmp.length(); i++) {
        Wire.write(tmp.charAt(i));  // wyslij kolejne cyfry temperatury
    }
}
