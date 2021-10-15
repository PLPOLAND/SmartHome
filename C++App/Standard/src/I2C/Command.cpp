#include "Command.h";

Command::Command()
{

}

Command::~Command()
{

}

void Command::convert(String command){
    switch (command.length())
    {
    case 1:
        break;
    case 2:
        if (command[0] == 'A')
        {
            if (command[1] == 'T')
            {
                // return Komendy::DODAJ_TERMOMETR;
            }
        }
        else if (command[0] == 'T')
        {
            // Serial.println(F("Temperatura"));
            // idTermometru = command[1];
            // return Komendy::TEMPERATURA;
        }

        break;
    default:
        break;
    }
    // return Komendy::NIC;
}
