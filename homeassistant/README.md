# SmartHome Config – integracja Home Assistant

Pełna konfiguracja serwera SmartHome z poziomu Home Assistant: pokoje, urządzenia, czujniki,
przyciski i ich lokalne funkcje kliknięć oraz serwis płytek (slave'ów). Zastępuje w tym zakresie
aplikację mobilną.

Encje (światła, rolety, czujniki, eventy przycisków) nadal pochodzą z MQTT Discovery serwera —
ta integracja dokłada tylko warstwę konfiguracji i rozmawia z serwerem przez ten sam broker MQTT.

## Wymagania

- Home Assistant 2024.12 lub nowszy ze skonfigurowaną integracją **MQTT**,
- serwer SmartHome podłączony do tego samego brokera (`mqtt.broker-url` w `application.properties`).

## Instalacja

1. Skopiuj katalog `custom_components/smarthome_config` do `<config>/custom_components/` w Home Assistant.
2. Zrestartuj Home Assistant.
3. *Ustawienia → Urządzenia i usługi → Dodaj integrację → SmartHome Config*; podaj bazowy topic
   (domyślnie `smarthome`, czyli `mqtt.base-topic` serwera).

## Używanie

*Ustawienia → Urządzenia i usługi → SmartHome Config → **Konfiguruj*** otwiera menu:

| Pozycja | Co robi |
|---|---|
| Dodaj / Edytuj / Usuń urządzenie | światło, gniazdko, wentylator, roleta: nazwa, pokój, slave, pin (roleta: dwa piny) |
| Dodaj / Edytuj / Usuń czujnik lub przycisk | przycisk (slave + pin) i higrometr; termometry 1-Wire serwer wykrywa sam |
| Funkcje kliknięć przycisku | dla 1..3 kliknięć: urządzenie z tej samej płytki + akcja (przełącz / góra / dół / stop) |
| Pokoje | dodaj, zmień nazwę (zmienia też obszar HA o tej samej nazwie), usuń pusty pokój |
| Serwis | restart wszystkich płytek, ponowne wysłanie konfiguracji na wybraną płytkę |

Serwer waliduje każdą zmianę (zajęte piny, nieistniejący pokój itd.) — odrzucona zmiana wraca
jako błąd w formularzu.

**Uwaga:** firmware płytki nie potrafi zmienić ani usunąć pojedynczego urządzenia. Zmiana slave'a
lub pinu, usunięcie urządzenia/przycisku i zmiana funkcji kliknięć wysyłają na płytkę całą
konfigurację od nowa, co na chwilę resetuje jej wyjścia. Samo dodanie urządzenia tego nie robi.

Zmiana zrobiona, gdy płytka jest niedostępna, jest zapamiętywana i wysyłana, gdy tylko płytka
znów odpowiada (także po restarcie serwera).

## Ograniczenia

- Usunięty termometr 1-Wire wróci przy następnej konfiguracji płytki — serwer wykrywa termometry sam.
  Żeby się go pozbyć, trzeba go odłączyć.
- Zmiana nazwy pokoju zmienia nazwę obszaru HA tylko wtedy, gdy obszar o nowej nazwie jeszcze nie
  istnieje; w przeciwnym razie urządzenia zostają w starym obszarze i trzeba je przenieść ręcznie.
- Automatyzacje serwera (`*_Function.json`) nie są dostępne z HA. Usunięte urządzenie przestaje
  reagować na automatyzację, która nim sterowała.

## Protokół MQTT

Integracja korzysta z API serwera, którego można też używać bezpośrednio (np. `mqtt.publish`):

- żądanie: `<base>/bridge/request/<akcja>` — JSON z parametrami i opcjonalnym `request_id`
  (musi być unikalne: żądanie z `request_id` użytym już dla tej akcji nie jest wykonywane
  drugi raz, tylko dostaje poprzednią odpowiedź),
- odpowiedź: `<base>/bridge/response/<akcja>` —
  `{"request_id": …, "status": "ok" | "error", "error": "…", "data": {…}}`,
- retained snapshot konfiguracji: `<base>/bridge/config` (pokoje, urządzenia, czujniki, slave'y).

| Akcja | Parametry |
|---|---|
| `room/add` | `name` |
| `room/rename` | `id`, `name` |
| `room/remove` | `id` (pokój musi być pusty) |
| `device/add` | `name`, `type` (`LIGHT`/`GNIAZDKO`/`WENTYLATOR`/`BLIND`), `room`, `slave`, `pin`, `pin2` (roleta) |
| `device/update` | `id` + dowolne z `name`, `room`, `slave`, `pin`, `pin2` |
| `device/remove` | `id` |
| `sensor/add` | `type` (`BUTTON`/`THERMOMETR_HYGROMETR`), `name`, `room`, `slave`, `pin` i `click_functions` (przycisk) |
| `sensor/update` | `id` + dowolne z `name`, `room`, `slave`, `pin`, `click_functions` |
| `sensor/remove` | `id` |
| `bridge/restart_slaves` | – |
| `bridge/reinit_slave` | `slave` |

`click_functions` to lista `{"clicks": 1..3, "device": <id urządzenia>, "state": "NONE"|"UP"|"DOWN"|"STOP"}`
(`NONE` = przełącz); podana lista zastępuje wszystkie funkcje przycisku.

Każdy, kto ma dostęp do brokera, może zmieniać konfigurację — broker powinien wymagać logowania.

## Testy

```bash
pip install -r requirements_test.txt
pytest
```
