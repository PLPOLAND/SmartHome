#include <Arduino.h>
#include <Przekaznik.h>
#include <Wylacznik.h>
#include <I2CAnything.h>

#include <Wire.h>
// function that executes whenever data is received from master
// this function is registered as an event, see setup()
void receiveEvent(int howMany)
{
    while (1 < Wire.available())
    {                         // loop through all but the last
        char c = Wire.read(); // receive byte as a character
        if (c < 65)
            Serial.print((int)c); // print the character
        else
            Serial.print(c);
    }
    int x = Wire.read(); // receive byte as an integer
    Serial.println(x);   // print the integer
}

void requestEvent()
{
    float t = 9.1;
    String tmp;
    tmp = String(t,2);
    
    
    for (byte i = 0; i < tmp.length(); i++)
    {
        Wire.write(tmp[i]);
    }
    
}


void setup()
{
    Wire.begin(8);                // join i2c bus with address #8
    Wire.onReceive(receiveEvent); // register event
    Wire.onRequest(requestEvent);
    Serial.begin(9600); // start serial for output
}

void loop()
{
    delay(100);
}


