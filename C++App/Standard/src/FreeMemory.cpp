#include <Arduino.h>
#include "FreeMemory.h"

int freeMemory(){
    char top;
#ifdef __arm__
    return &top - reinterpret_cast<char *>(sbrk(0));
#elif defined(CORE_TEENSY) || (ARDUINO > 103 && ARDUINO != 151)
    return &top - __brkval;
#else  // __arm__
    return __brkval ? &top - __brkval : &top - __malloc_heap_start;
#endif // __arm__
}

#ifndef __arm__
extern char __heap_start;
struct __freelist {
    size_t sz;
    struct __freelist *nx;
};
extern struct __freelist *__flp; // lista wolnych bloków malloc z avr-libc

#define RAM_PAINT 0xA5

static char *heapTop()
{
    return __brkval ? __brkval : &__heap_start;
}

__attribute__((noinline)) void paintFreeRam()
{
    char marker; // aktualna pozycja stosu
    char *p = heapTop();
    char *end = &marker - 64; // margines pod bieżącą ramką stosu
    while (p < end)
    {
        *p++ = RAM_PAINT;
    }
}

uint16_t heapSize()
{
    return heapTop() - &__heap_start;
}

uint16_t untouchedRam()
{
    char marker; // bieżąca pozycja stosu - powyżej niej są aktywne ramki
    const volatile uint8_t *p = (const volatile uint8_t *)heapTop();
    const volatile uint8_t *limit = (const volatile uint8_t *)&marker;
    // Nad __brkval mogą leżeć pozostałości zwolnionych bloków ze szczytu sterty (malloc obniża wtedy __brkval) -
    // pomiń je i policz najdłuższy ciąg nietkniętego wzoru między stertą a stosem.
    uint16_t best = 0, run = 0;
    while (p < limit)
    {
        if (*p == RAM_PAINT)
        {
            run++;
            if (run > best)
            {
                best = run;
            }
        }
        else
        {
            run = 0;
        }
        p++;
    }
    return best;
}

uint16_t freeListBytes()
{
    uint16_t n = 0;
    for (struct __freelist *f = __flp; f != nullptr; f = f->nx)
    {
        n += f->sz + sizeof(size_t);
    }
    return n;
}
#endif // !__arm__
