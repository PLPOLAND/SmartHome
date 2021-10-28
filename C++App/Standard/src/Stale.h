#define DEBUG
#ifdef DEBUG
    #define OUTPUT(x) Serial.print(x); Serial.flush();
    #define OUTPUT_LN(x) Serial.println(x); Serial.flush();
#else
    #define OUTPUT(x)
    #define OUTPUT_LN(x)
#endif // DEBUG

#define CZAS_ODSWIERZANIA_TEMPERATURY 0.1 //w minutach
