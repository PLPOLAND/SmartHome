#include <I2CConverter.h>
#include <Kontener.h>
// TODO poprawic odwołania do kontenerów
I2CConverter *I2CConverter::singleton = nullptr;

I2CConverter::I2CConverter() {
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
}

I2CConverter::~I2CConverter() { Wire.end(); }

I2CConverter *I2CConverter::getInstance() {
  if (singleton == nullptr) {
    singleton = new I2CConverter();
  }

  return singleton;
}

void I2CConverter::onRecieveEvent(int howManyBytes) {
  singleton->RecieveEvent(howManyBytes);
}

void I2CConverter::onRequestEvent() { singleton->RequestEvent(); }

void I2CConverter::RecieveEvent(int howManyBytes) {
  byte i = 0;
  while (0 < Wire.available()) {
    buf[i++] = Wire.read();
    Serial.println(buf[i - 1]);
    if (!(i < BUFFOR_IN_SIZE)) {
      i--;
      break;  // TODO: Obsługa błędu???
    }
  }
  switch (find_command(i)) {
    case Komendy::DODAJ_TERMOMETR:
      this->addTermometr();
      break;
    case Komendy::TEMPERATURA:
        this->coWyslac = DoWyslania::TEMPERATURA;
        break;
    default:
      break;
  };
  for (byte i = 0; i < BUFFOR_IN_SIZE; i++)
  {
      buf[i] = 0;
  }
  
}
void I2CConverter::RequestEvent() {
  switch (singleton->coWyslac) {
    case DoWyslania::TEMPERATURA:
        singleton->printTemperature(0);
        break;
    case DoWyslania::REPLY: {
        byte i = 0;
        while (buf_out[i] != 0 && i < BUFFOR_OUT_SIZE) {
            Wire.write(buf_out[i]);
            #ifdef DEBUG
                Serial.print((int)buf_out[i]);
            #endif  // DEBUG
            i++;
        }
        #ifdef DEBUG
            Serial.println();
        #endif  // DEBUG
    } break;
    case DoWyslania::STATUS:
        break;
    default:
        break;
  }
}
Komendy I2CConverter::find_command(byte size) {
  switch (size) {
    case 1:
      break;
    case 2:
      if (buf[0] == 'A') {
        if (buf[1] == 'T') {
          return Komendy::DODAJ_TERMOMETR;
        }

      } else if (buf[0] == 'T') {
          Serial.println(F("Temperatura"));
        return Komendy::TEMPERATURA;
      }

      break;
    default:
      break;
  }
  return Komendy::NIC;
}
void I2CConverter::addTermometr() {
  if (termometry == nullptr) {
    termometry = new Kontener<Termometr *>();
  }
  Termometr *tmp = new Termometr();
  if (!tmp->isCorrect())
  {
      delete tmp;
      this->buf_out[0] = -1;  // Zwróć ID termometru na płytce
      this->coWyslac = DoWyslania::REPLY;
  }
  else{
    Serial.println(tmp->getTemperature());
    this->termometry->add(tmp);
    this->buf_out[0] =
        termometry->getLast()->getID();  // Zwróć ID termometru na płytce
    this->coWyslac = DoWyslania::REPLY;
  }
}
/// Wysyła dane termometru
void I2CConverter::printTemperature(byte id) {
  String tmp;
  tmp = String(termometry->get(id)->getTemperature(), 2);
  Serial.println(tmp);
  Wire.write(id);  // wyslij ID Termometru na płytce
  for (byte i = 0; i < tmp.length(); i++) {
    Wire.write(tmp[i]);  // wyslij kolejne cyfry temperatury
  }
}
