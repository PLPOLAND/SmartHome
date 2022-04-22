#ifndef STALE
#define STALE
    #define DEBUG
    #ifdef DEBUG
        #define OUT(x) Serial.print(x); Serial.flush();
        #define OUT_LN(x) Serial.println(x); Serial.flush();
    #else
        #define OUT(x)
        #define OUT_LN(x)
    #endif // DEBUG

    #define CZAS_ODSWIERZANIA_TEMPERATURY 0.1 //w minutach

    //pinów od 2 do 2+5 używanych do określania adresu urządzenia ((2^PINOW_NA_ADRES)-1 dostępnych adresów)
    #define PINOW_NA_ADRES 5

    #define MAX_NUMBER_OF_BUTTON_FUNCTIONS 4
    //Czas podnoszenia/opuszczenia rolety
    #define CZAS_CALKOWITEJ_ZMIANY_POLOZENIA SECS(10) // TODO: kalibracja czasu!

    #define SECS(t) (unsigned long)(t * 1000)
    #define MINS(t) SECS(t) * 60
    #define HOURS(t) MINS(t) * 60

    #define BUTTON_CLICK_TIME SECS(1)

    //BUFFORY
    #define BUFFOR_IN_SIZE 9
    #define BUFFOR_OUT_SIZE 9
    //pin komunikacji oneWire
#define ONEWIRE_BUS 11

#endif // !Stale

