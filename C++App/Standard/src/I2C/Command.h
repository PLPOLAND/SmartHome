#ifndef COMMAND_H
#define COMMAND_H
#include <Arduino.h>

enum class Komendy
{
    NIC,
    DODAJ_TERMOMETR,
    TEMPERATURA,
    ZMIEN_STAN,
};

class Command
{
private:
    //ID urządzenia
    int id;
    
    
public:
    Command();
    ~Command();

    static void convert(String command);
};



#endif // !COMMAND_H
