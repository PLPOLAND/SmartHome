#include <Arduino.h>
#include <Wire.h>

template <typename T>
unsigned int I2C_writeAnything( T &value)
{
    Wire.write((byte *)&value, sizeof(value));
    for (int i = 0; i < sizeof(value); i++)
    {
        void *t = &value;
        Wire.write(*((byte *)t+i));
    }
    
    return sizeof(value);
} // end of I2C_writeAnything

template <typename T>
unsigned int I2C_readAnything(T &value)
{
    byte *p = (byte *)&value;
    unsigned int i;
    for (i = 0; i < sizeof value; i++)
        *p++ = Wire.read();
    return i;
} // end of I2C_readAnything