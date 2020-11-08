#include <Kontener.h>
#include <Termometr.h>

// template<typename T>
// void Kontener<T>::add(T dana)
// {
//     if (glowa == nullptr)
//     {
//         glowa = new Konwerter<T>(dana);
//         ogon = glowa;
//     }
//     else
//     {
//         Konwerter<T> wsk = ogon;
//         wsk->next = new Konwerter<T>(dana);
//         ogon = wsk->next;
//         wsk->next->back = wsk;
//     }
//     top++; //Podnieś ilość przetrzymywanych danych
// }
// /// Pobierz ostatnią daną i ją usuń
// template <typename T>
// T Kontener<T>::pop_back()
// {
//     auto wsk = ogon;
//     ogon = wsk->back;
//     ogon->next = nullptr;
//     T dana = wsk->dana;
//     delete wsk;
//     return dana;
// }

/// Pobierz daną na podanej pozycji
// template <typename T>
// T Kontener<T>::operator[](int i)
// {
//     auto wsk = glowa;
//     //assert(i < this->top);//TODO:???
//     while (i-- > 0)
//     {
//         wsk = wsk->next;
//     }
//     return wsk->dana;
// }

// template <typename T>
// T Kontener<T>::get(int i)
// {
//     auto wsk = glowa;
//     //assert(i < this->top);//TODO:???
//     while (i-- > 0)
//     {
//         wsk = wsk->next;
//     }
//     return wsk->dana;
// }
// template <>
// Termometr* Kontener<Termometr *>::get(int i)
// {
//     auto wsk = glowa;
//     //assert(i < this->top);//TODO:???
//     while (i-- > 0)
//     {
//         wsk = wsk->next;
//     }
//     return wsk->dana;
// }


// template <typename T>
// T Kontener<T>::getLast()
// {
//     return ogon->dana;
// }