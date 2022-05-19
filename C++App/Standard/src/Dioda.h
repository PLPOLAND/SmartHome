#ifndef DIODA_H
#define DIODA_H

#pragma once

#include "Arduino.h"
#include "Timers.h"

class Dioda
{
public:
    Dioda();
    ~Dioda();

    void on();
    void off();
    void tic(); 
    void on(long time);

private : 
    Timer timer;
    int pin = 7;
};

#endif