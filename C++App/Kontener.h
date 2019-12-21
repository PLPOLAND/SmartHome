#if !defined(KONTENER_H)
#define KONTENER_H
/*
Autor 
Marek Padyna

Konterner służący do przetrzymywania danych w liście.
Zapewnia... TODO
*/


///Przetrzymuje dane
///Pozycja w kontenerze
template<typename T>
class konwerter
{
private:
    T dana;//dana przetrzymywana w danej pozycji
    konwerter * next;//wskaźnik na koleją pozycję
    konwerter * back;//wskaźnik na poprzednia pozycję
public:
    konwerter(T & Dana):dana(Dana), next(nullptr) {};
    ~konwerter( delete next; );
};

///Przetrzymuje dane w liście
template <typename T>
class Kontener
{
private:
    konwerter<T> * glowa;
    konwerter<T> * ogon;
    int top;
public:
    Kontener(): glowa(nullptr),top(0) {};
    Kontener(T & dana): glowa(new konwerter<T>(dana)), top(1) {};
    ~Kontener(){
        delete glowa;
    }

    void push_back(T & dana){
        if (glowa == nullptr) {
            glowa = new konwerter<T>(dana);
            ogon = glowa;
        }
        else
        {
            auto wsk = ogon;
            wsk->next = new konwerter<T>(dana); 
            ogon = wsk->next;
            wsk->next->back = wsk;
        }
        top++;//Podnieś ilość przetrzymywanych danych
        
    }
    /// Pobierz ostatnią daną i ją usuń
    T pop_back(){
        auto wsk = ogon;
        ogon = wsk->back;
        ogon->next = nullptr;
        T dana = wsk->dana;
        delete wsk;
        return dana;
    }
    /// Pobierz daną na podanej pozycji
    T operator[] (int i){
        auto wks = glowa;
        static_assert(i < this->top);
        while(i-- >0){
            wsk = wsk->next;
        }
        return wsk->dana;
    }
    void ticForEach(){
        auto wsk = glowa;
        while(wsk!=nullptr){
            wsk->tic();///????????????????????????
            wsk = wsk->next;
        }
    }
};




#endif