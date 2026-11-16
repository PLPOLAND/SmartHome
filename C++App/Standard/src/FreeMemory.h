#ifndef FreeMemory
#define FreeMemory

    #ifdef __arm__
    // should use uinstd.h to define sbrk but Due causes a conflict
    extern "C" char* sbrk(int incr);
    #else // __ARM__
    extern char* __brkval;
    #endif // __arm__

    int freeMemory();

    // Diagnostyka pamięci (komenda I2C 'M')
    #include <stdint.h>
    void paintFreeRam();        // wypełnia wolny RAM między stertą a stosem znanym wzorem (wywołać na początku setup())
    uint16_t heapSize();        // rozmiar sterty (od __heap_start do __brkval)
    uint16_t untouchedRam();    // ile bajtów nad stertą nie zostało nigdy nadpisane (ani przez stertę, ani przez stos)
    uint16_t freeListBytes();   // suma bajtów w zwolnionych blokach sterty (do ponownego użycia)

#endif // !FreeMemory