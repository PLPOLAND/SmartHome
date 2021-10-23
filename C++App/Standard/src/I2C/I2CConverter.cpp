#include <I2C/I2CConverter.h>
#include <Kontener.h>
#include <FreeMemory.h>
#include "Command.h"

// TODO poprawic odwołania do kontenerów
I2CConverter* I2CConverter::singleton = nullptr;

I2CConverter::I2CConverter()
{
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
    Serial.print(F("Wystartowano na:"));
    Serial.println((int)adress);
    Wire.begin(adress);
    system = System::getSystem();
}

I2CConverter::~I2CConverter()
{
    Wire.end();
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
    singleton->RecieveEvent(howManyBytes);
}

void I2CConverter::onRequestEvent() { singleton->RequestEvent(); }

void I2CConverter::RecieveEvent(int howManyBytes)
{
    byte buffReadSize = 0;
    Serial.print(F("howmanybytes: "));
    Serial.println(howManyBytes);
    while (0 < Wire.available()) {
        buf[buffReadSize++] = Wire.read();
        Serial.print("i:");
        Serial.print(buffReadSize-1);
        Serial.print("buf:");
        Serial.println(buf[buffReadSize - 1]);
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
    Serial.println(freeMemory());
}
//TODO kolejka komend
void I2CConverter::RequestEvent()
{
    switch (singleton->coWyslac) {
    case DoWyslania::TEMPERATURA:
        // Serial.println("PrintTEMP1");
        printTemperature(0);//TODO wczytywanie ID z komendy??
        // Serial.println("PrintTEMP");
        this->coWyslac = DoWyslania::NIC;
        break;
    case DoWyslania::REPLY: {
        byte i = 0;
        while (buf_out[i] != 0 && i < BUFFOR_OUT_SIZE) {
            Wire.write(buf_out[i]);
            Serial.print((int)buf_out[i]);
            i++;
        }
        Serial.println();
    } break;
    case DoWyslania::STATUS:
        break;
    default:
        break;
    }
    Serial.println(freeMemory());
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
    this->buf_out[0] = system->addDevice(Device::TYPE::TERMOMETR); // Zwróć otrzymane id
    this->coWyslac = DoWyslania::REPLY;
}

void I2CConverter::printTemperature(byte id)
{
    // Serial.print("freeMemory(): ");
    // Serial.println(freeMemory());
    String tmp = String(((Termometr *)system->getDevice(id))->getTemperature(), 2);
    // Serial.print("afterString: ");
    // Serial.println(*tmp);
    // Serial.println(termometry.get(id)->getTemperature());
    Wire.write(id);  // wyslij ID Termometru na płytce
    for (byte i = 0; i < tmp.length(); i++) {
        Wire.write(tmp.charAt(i));  // wyslij kolejne cyfry temperatury
    }
}
