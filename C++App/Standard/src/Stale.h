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

    //pinów od 2 do 2+6 używanych do określania adresu urządzenia ((2^PINOW_NA_ADRES)-1 dostępnych adresów)
    #define PINOW_NA_ADRES 6

    //BUFFORY
    #define BUFFOR_IN_SIZE 5
    #define BUFFOR_OUT_SIZE 8

#endif // !Stale

