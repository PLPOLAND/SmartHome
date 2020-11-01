#include <I2CConverter.h>

I2CConverter::I2CConverter()
{
    static_assert(PINOW_NA_ADRES >= 1, "ZA MALO PINOW NA ADRESS");
    static_assert(ONEWIRE_BUS > (PINOW_NA_ADRES+1),"Termometr na zajetym pinie");
    for (byte i = 0; i < PINOW_NA_ADRES; i++)
    {
        pinMode(2 + i, INPUT);
    }
    byte tmp = 1;
    byte adress = 0;
    for (byte i = 0; i < PINOW_NA_ADRES; i++)
    {
        adress += tmp * digitalRead(2 + i);
        tmp *= 2;
    }

    Wire.begin(adress);
    Wire.onReceive(onRecieveEvent);
    Wire.onRequest(onRequestEvent);
}

I2CConverter::~I2CConverter()
{
    Wire.end();
}

void I2CConverter::onRecieveEvent(int howManyBytes)
{
    while (1 < Wire.available())
    {                         // loop through all but the last
        char c = Wire.read(); // receive byte as a character
        if (c < 65)
            Serial.print((int)c); // print the character
        else
            Serial.print(c);
    }
}

void I2CConverter::onRequestEvent()
{
}


void I2CConverter::addTermometr(){
    try
    {
        termometry.add(new Termometr());
        Wire.write(termometry.getLast()->getID());//Zwróć ID termometru na płytce
    }
    catch(String e)
    {
        Serial.println(e);
    }

}
///Wysyła dane termometru
void I2CConverter::printTemperature(byte id){
    String tmp;
    tmp = String(termometry[id]->getTemperature(), 2);

    Wire.write(id);//wyslij ID Termometru na płytce
    for (byte i = 0; i < tmp.length(); i++)
    {
        Wire.write(tmp[i]);//wyslij kolejne cyfry temperatury
    }
}
