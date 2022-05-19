#include "Dioda.h"

Dioda::Dioda()
{
    pinMode(pin,OUTPUT);
    digitalWrite(pin,LOW);
    timer.restart();

}

Dioda::~Dioda()
{
    digitalWrite(pin, LOW);

}

void Dioda::on(){
    digitalWrite(pin, HIGH);
}

void Dioda::on(long time){
    timer.begin(time);
    digitalWrite(pin, HIGH);
}

void Dioda::off(){
    digitalWrite(pin, LOW);
}

void Dioda::tic(){
    if (timer.available())
    {
        this->off();
    }
    
}
