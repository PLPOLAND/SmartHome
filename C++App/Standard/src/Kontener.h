// #include <Arduino.h>
// #ifndef KONTENER_H
// #define KONTENER_H
// /*
// Autor
// Marek Padyna

// Konterner służący do przetrzymywania danych w liście.
// Zapewnia... TODO
// */


// ///Przetrzymuje dane
// ///Pozycja w kontenerze
// template<typename T>
// class Konwerter
// {
// public:
//     T dana;//dana przetrzymywana w danej pozycji
//     Konwerter<T>* next;//wskaźnik na koleją pozycję
//     Konwerter<T>* back;//wskaźnik na poprzednia pozycję

//     Konwerter(T& Dana) :dana(Dana), next(nullptr) {};
//     ~Konwerter() {
//         delete next;
//     }
// };
// // ///Przetrzymuje dane
// // ///Pozycja w kontenerze
// // template<typename T>
// // class Konwerter<T*>
// // {
// // public:
// //     T* dana;//dana przetrzymywana w danej pozycji
// //     Konwerter<T*> * next;//wskaźnik na koleją pozycję
// //     Konwerter<T*> * back;//wskaźnik na poprzednia pozycję

// //     Konwerter(T Dana):dana(Dana), next(nullptr) {};
// //     ~Konwerter(){
// //         delete next;
// //     }
// //     // template<typename E>
// //     // friend class Kontener;
// // };

// ///Przetrzymuje dane w liście
// template <typename T>
// class Kontener
// {
// private:
//     Konwerter<T>* glowa;
//     Konwerter<T>* ogon;
//     int top;
// public:
//     Kontener() : glowa(nullptr), top(0) {};
//     Kontener(T dana) : glowa(new Konwerter<T>(dana)), top(1) {};
//     ~Kontener() {
//         delete (T)glowa;
//     }

//     void add(T dana)
//     {
//         if (glowa == nullptr)
//         {
//             glowa = new Konwerter<T>(dana);
//             ogon = glowa;
//         }
//         else
//         {
//             Konwerter<T>* wsk = ogon;
//             wsk->next = new Konwerter<T>(dana);
//             ogon = wsk->next;
//             wsk->next->back = wsk;
//         }
//         top++; //Podnieś ilość przetrzymywanych danych
//     }

//     T operator[](int i)
//     {
//         auto wsk = glowa;
//         //assert(i < this->top);//TODO:???
//         while (i-- > 0)
//         {
//             wsk = wsk->next;
//         }
//         return wsk->dana;
//     }
//     T getLast()
//     {
//         return ogon->dana;
//     }
//     T get(int i)
//     {
//         Konwerter<T>* wsk = glowa;
//         Serial.print(F("GET c"));
//         Serial.println(i);
//         if (wsk!=nullptr)
//         {
//             Serial.println("wsk!=NULL");
//         }
        
//         //assert(i < this->top);//TODO:???
//         while (i-- > 0)
//         {
//             Serial.println("i "+i);
//             wsk = wsk->next;
//         }
//         return wsk->dana;
//     }
// };




// #endif