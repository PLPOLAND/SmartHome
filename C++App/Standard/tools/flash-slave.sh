#!/bin/bash
# Wgrywa firmware na slave-a podłączonego do Raspberry Pi przez Arduino Uno bez ATmegi
# (TX/RX/RESET/GND Uno -> gniazdo RJ45 slave-a, BEZ +5V).
#
# Użycie:
#   flash-slave.sh [-a ADRES_I2C] [-p PORT] [-n] [plik.hex]   jeden slave
#   flash-slave.sh -w [-p PORT] [plik.hex]                    tryb seryjny
#
#   -w        tryb seryjny: aplikacja jest zatrzymywana raz, a skrypt sam wykrywa slave-a
#             pod kablem, rozpoznaje jego adres I2C i wgrywa firmware tylko, gdy się różni.
#             Wystarczy przepinać kabel RJ45. Kończy się po obsłużeniu wszystkich slave-ów
#             widocznych na starcie albo po Ctrl+C; potem uruchamia aplikację.
#   -a ADRES  adres I2C slave-a (DIP switch, dziesiętnie). Po wgraniu skrypt sprawdza,
#             czy slave odpowiada na komendę 'M' (pamięć), zanim uruchomi aplikację.
#   -p PORT   port Uno (domyślnie /dev/ttyACM0)
#   -n        nie zatrzymuj aplikacji smarthome (niezalecane - po ~7 s błędów I2C
#             aplikacja odcina zasilanie wszystkim slave-om)
#   plik.hex  domyślnie /home/pi/firmware/slave-latest.hex
#
# Każde wgranie jest dopisywane do /home/pi/firmware/flash-history.log.
# Powiadomienia na telefon: adres webhooka Home Assistant w zmiennej FLASH_NOTIFY_URL
# albo w pliku /home/pi/firmware/flash-notify.url (poza repo). Bez niego - tylko terminal.

set -u

FW_DIR=/home/pi/firmware
HEX="$FW_DIR/slave-latest.hex"
PORT=/dev/ttyACM0
ADDR=""
STOP_APP=1
SERIAL_MODE=0
SERVICE=smarthome
ADDR_MIN=1
ADDR_MAX=31 # 5 pinów adresu (PINOW_NA_ADRES w Stale.h)
NOTIFY_URL=${FLASH_NOTIFY_URL:-}
[ -z "$NOTIFY_URL" ] && [ -r "$FW_DIR/flash-notify.url" ] && NOTIFY_URL=$(head -n1 "$FW_DIR/flash-notify.url")

usage() {
    sed -n '2,23p' "$0" | sed 's/^# \{0,1\}//'
    exit "${1:-0}"
}

while getopts "a:p:nwh" opt; do
    case $opt in
        a) ADDR=$OPTARG ;;
        p) PORT=$OPTARG ;;
        n) STOP_APP=0 ;;
        w) SERIAL_MODE=1 ;;
        h) usage 0 ;;
        *) usage 1 ;;
    esac
done
shift $((OPTIND - 1))
[ $# -ge 1 ] && HEX=$1
[ -e "$HEX" ] && HEX=$(readlink -f "$HEX")

if [ ! -f "$HEX" ]; then
    echo "Brak pliku $HEX" >&2
    exit 1
fi
if [ "$SERIAL_MODE" -eq 1 ] && { [ -n "$ADDR" ] || [ "$STOP_APP" -eq 0 ]; }; then
    echo "Tryb seryjny (-w) sam rozpoznaje adresy i zawsze zatrzymuje aplikację - bez -a i -n." >&2
    exit 1
fi
if [ "$SERIAL_MODE" -eq 0 ] && [ ! -e "$PORT" ]; then
    echo "Brak portu $PORT - czy Uno jest podłączone do USB?" >&2
    exit 1
fi
if [ -n "$ADDR" ] && ! [[ "$ADDR" =~ ^[0-9]+$ && "$ADDR" -ge 1 && "$ADDR" -le 127 ]]; then
    echo "Nieprawidłowy adres I2C: $ADDR" >&2
    exit 1
fi
HEX_NAME=$(basename "$HEX")
HEX_MD5=$(md5sum "$HEX" | cut -c1-8)

# Pomocnik w Pythonie (bez dodatkowych pakietów):
#   query ADRES          - odpytuje slave-a komendą 'M' (combined write+read na /dev/i2c-1),
#                          wypisuje "heap=... nietknięty RAM=... freelist=..." albo kończy się błędem
#   scan OD DO           - wypisuje adresy z zakresu, które odpowiadają na 'M'
#   probe PORT ADRESY... - resetuje slave-a pod kablem i sprawdza, czy odpowiada bootloader (STK500).
#                          Jeśli tak, trzyma go ~2,5 s w bootloaderze i odpytuje podane adresy:
#                          ten, który przez cały czas milczy, to slave pod kablem. Wypisuje adres
#                          albo "?" (kod 0); kod 2 = nic nie jest podłączone.
PY_HELPER=$(cat <<'EOF'
import ctypes, fcntl, os, struct, sys, termios, time
I2C_RDWR, I2C_M_RD = 0x0707, 0x0001
class Msg(ctypes.Structure):
    _fields_ = [("addr", ctypes.c_uint16), ("flags", ctypes.c_uint16),
                ("len", ctypes.c_uint16), ("buf", ctypes.POINTER(ctypes.c_uint8))]
class RdWr(ctypes.Structure):
    _fields_ = [("msgs", ctypes.POINTER(Msg)), ("nmsgs", ctypes.c_uint32)]

def query(fd, addr):
    wbuf = (ctypes.c_uint8 * 1)(ord('M'))
    rbuf = (ctypes.c_uint8 * 8)()
    msgs = (Msg * 2)(Msg(addr, 0, 1, wbuf), Msg(addr, I2C_M_RD, 8, rbuf))
    try:
        fcntl.ioctl(fd, I2C_RDWR, RdWr(msgs, 2))
    except OSError:
        return None
    r = list(rbuf)
    if r[0] != ord('M'):
        return None
    le = lambda i: r[i] | (r[i + 1] << 8)
    return le(1), le(3), le(5)

def open_i2c():
    return os.open("/dev/i2c-1", os.O_RDWR)

def cmd_query(addr):
    fd = open_i2c()
    for _ in range(5):
        m = query(fd, addr)
        if m:
            print("heap=%d B, nietknięty RAM=%d B, freelist=%d B" % m)
            return 0
        time.sleep(0.5)
    return 1

def cmd_scan(lo, hi):
    fd = open_i2c()
    found = []
    for a in range(lo, hi + 1):
        for _ in range(2):
            if query(fd, a):
                found.append(str(a))
                break
            time.sleep(0.05)
    print(" ".join(found))
    return 0

def cmd_probe(port, addrs):
    try:
        fd = os.open(port, os.O_RDWR | os.O_NOCTTY | os.O_NONBLOCK)
    except OSError:
        return 2
    try:
        attrs = termios.tcgetattr(fd)
        attrs[0] = 0                                   # iflag
        attrs[1] = 0                                   # oflag
        attrs[2] = termios.CS8 | termios.CREAD | termios.CLOCAL | termios.HUPCL
        attrs[3] = 0                                   # lflag
        attrs[4] = attrs[5] = termios.B115200
        attrs[6][termios.VMIN] = 0
        attrs[6][termios.VTIME] = 0
        termios.tcsetattr(fd, termios.TCSANOW, attrs)
        lines = struct.pack("I", termios.TIOCM_DTR | termios.TIOCM_RTS)

        def sync():
            termios.tcflush(fd, termios.TCIFLUSH)
            os.write(fd, b"\x30\x20")                  # STK_GET_SYNC, CRC_EOP
            buf, end = b"", time.time() + 0.15
            while time.time() < end:
                try:
                    buf += os.read(fd, 32)
                except BlockingIOError:
                    pass
                if b"\x14\x10" in buf:                 # STK_INSYNC, STK_OK
                    return True
                time.sleep(0.01)
            return False

        found = False
        for _ in range(2):
            # reset jak w avrdude: DTR/RTS w dół, potem w górę
            fcntl.ioctl(fd, termios.TIOCMBIC, lines)
            time.sleep(0.25)
            fcntl.ioctl(fd, termios.TIOCMBIS, lines)
            time.sleep(0.05)
            if any(sync() for _ in range(4)):
                found = True
                break
        if not found:
            return 2
        if not addrs:
            print("?")
            return 0

        # Bootloader nie obsługuje I2C - slave pod kablem milczy, dopóki dostaje komendy.
        i2c = open_i2c()
        fails = dict.fromkeys(addrs, 0)
        rounds, end = 0, time.time() + 2.5
        while time.time() < end:
            sync()                                     # podtrzymuje bootloader (watchdog ~1 s)
            for a in addrs:
                if query(i2c, a) is None:
                    fails[a] += 1
            rounds += 1
        silent = [a for a in addrs if fails[a] == rounds]
        print(silent[0] if len(silent) == 1 else "?")
        return 0
    finally:
        os.close(fd)

cmd, args = sys.argv[1], sys.argv[2:]
if cmd == "query":
    sys.exit(cmd_query(int(args[0])))
if cmd == "scan":
    sys.exit(cmd_scan(int(args[0]), int(args[1])))
if cmd == "probe":
    sys.exit(cmd_probe(args[0], [int(a) for a in args[1:]]))
sys.exit(64)
EOF
)
py() { python3 -c "$PY_HELPER" "$@"; }

# Powiadomienie na telefon przez webhook Home Assistant (jeśli skonfigurowany).
notify() {
    echo ">> $1"
    [ -z "$NOTIFY_URL" ] && return 0
    local body
    body=$(python3 -c 'import json,sys; print(json.dumps({"message": sys.argv[1]}))' "$1")
    curl -s -m 5 -o /dev/null -X POST -H 'Content-Type: application/json' -d "$body" "$NOTIFY_URL" \
        || echo "!! Nie udało się wysłać powiadomienia" >&2
}

log_flash() { # adres wynik
    mkdir -p "$FW_DIR"
    echo "$(date '+%F %T')  slave=${1:-?}  $HEX_NAME  md5=$HEX_MD5  $2" >> "$FW_DIR/flash-history.log"
}

# Zapis z weryfikacją, do 3 prób.
avr_write() {
    local try
    for try in 1 2 3; do
        avrdude -q -p m328p -c arduino -P "$PORT" -b 115200 -U "flash:w:$HEX:i" && return 0
        echo "!! Próba $try nieudana, ponawiam..." >&2
        sleep 2
    done
    return 1
}

# Porównanie bez zapisu: 0 = identyczny, 1 = różni się, 2 = brak połączenia/błąd.
avr_verify() {
    local out
    out=$(avrdude -q -p m328p -c arduino -P "$PORT" -b 115200 -U "flash:v:$HEX:i" 2>&1) && return 0
    grep -qi "mismatch" <<< "$out" && return 1
    echo "$out" | tail -n 3 >&2
    return 2
}

APP_WAS_ACTIVE=0
if [ "$STOP_APP" -eq 1 ] && systemctl is-active --quiet "$SERVICE"; then
    APP_WAS_ACTIVE=1
fi

ON_EXIT_SUMMARY=""
restore_app() {
    [ -n "$ON_EXIT_SUMMARY" ] && eval "$ON_EXIT_SUMMARY"
    if [ "$APP_WAS_ACTIVE" -eq 1 ]; then
        echo ">> Uruchamiam $SERVICE"
        sudo -n systemctl start "$SERVICE" || echo "!! Nie udało się uruchomić $SERVICE - zrób to ręcznie: sudo systemctl start $SERVICE" >&2
    fi
}
trap restore_app EXIT
trap 'exit 130' INT TERM

if [ "$APP_WAS_ACTIVE" -eq 1 ]; then
    echo ">> Zatrzymuję $SERVICE (żeby nie restartowała slave-ów w trakcie wgrywania)"
    sudo -n systemctl stop "$SERVICE" || { APP_WAS_ACTIVE=0; echo "!! Nie udało się zatrzymać $SERVICE" >&2; exit 1; }
fi

# ---------------------------------------------------------------- jeden slave
if [ "$SERIAL_MODE" -eq 0 ]; then
    echo ">> Wgrywam $HEX_NAME (md5 $HEX_MD5) przez $PORT${ADDR:+ na slave $ADDR}"
    RESULT="BŁĄD"
    OK=0
    if avr_write; then
        OK=1
        RESULT="OK"
        echo ">> Wgrano i zweryfikowano."
        if [ -n "$ADDR" ]; then
            sleep 3 # bootloader + setup()
            if MEM=$(py query "$ADDR"); then
                echo ">> Slave $ADDR odpowiada: $MEM"
                RESULT="OK ($MEM)"
            else
                echo "!! Slave $ADDR nie odpowiada na I2C. Sprawdź adres (DIP switch) albo czy kabel Uno nie trzyma go w resecie." >&2
                RESULT="OK, brak odpowiedzi I2C"
            fi
        fi
    else
        echo "!! Wgrywanie nie powiodło się. Sprawdź kabel RJ45, czy slave ma zasilanie i czy RESET Uno jest podłączony." >&2
    fi
    log_flash "$ADDR" "$RESULT"
    [ -n "$NOTIFY_URL" ] && notify "Slave ${ADDR:-?}: $RESULT"
    [ "$OK" -eq 1 ]
    exit
fi

# ---------------------------------------------------------------- tryb seryjny
sleep 1
echo ">> Szukam slave-ów na I2C (adresy $ADDR_MIN-$ADDR_MAX)..."
read -r -a ALL <<< "$(py scan "$ADDR_MIN" "$ADDR_MAX")"
if [ "${#ALL[@]}" -eq 0 ]; then
    echo "!! Żaden slave nie odpowiada na I2C - nie da się rozpoznawać adresów." >&2
    exit 1
fi
declare -A DONE=() FAILED=()
WRITTEN=() SAME=() UNKNOWN=0
LAST_UNKNOWN=0

remaining() {
    local a out=()
    for a in "${ALL[@]}"; do [ -z "${DONE[$a]:-}" ] && out+=("$a"); done
    echo "${out[*]}"
}
summary() {
    local msg="Koniec wgrywania $HEX_NAME. Wgrane: ${WRITTEN[*]:-brak}; bez zmian: ${SAME[*]:-brak}"
    local left; left=$(remaining)
    [ -n "$left" ] && msg+="; NIE obsłużone: $left"
    [ "$UNKNOWN" -gt 0 ] && msg+="; o nieznanym adresie: $UNKNOWN"
    [ "$APP_WAS_ACTIVE" -eq 1 ] && msg+=". Uruchamiam aplikację."
    notify "$msg"
}
ON_EXIT_SUMMARY=summary

notify "Tryb seryjny: $HEX_NAME (md5 $HEX_MD5), ${#ALL[@]} slave-ów: ${ALL[*]}. Podłącz kabel do pierwszego."
PORT_WARNED=0
while [ -n "$(remaining)" ]; do
    if [ ! -e "$PORT" ]; then
        [ "$PORT_WARNED" -eq 0 ] && echo "!! Brak portu $PORT - czekam na Uno..." >&2
        PORT_WARNED=1
        sleep 2
        continue
    fi
    PORT_WARNED=0

    SLAVE=$(py probe "$PORT" "${ALL[@]}") || { sleep 1; continue; }
    if [ "$SLAVE" = "?" ]; then
        # Nie wiadomo, czy to ten sam slave co poprzednio - nie wgrywaj w kółko.
        [ "$LAST_UNKNOWN" -eq 1 ] && { sleep 2; continue; }
    else
        LAST_UNKNOWN=0
        [ -n "${DONE[$SLAVE]:-}" ] && { sleep 1; continue; } # kabel jeszcze nie przepięty
    fi
    LABEL="Slave $SLAVE"
    [ "$SLAVE" = "?" ] && LABEL="Slave o nieznanym adresie"
    echo ">> $LABEL pod kablem, porównuję firmware..."

    avr_verify
    case $? in
        0)  STATUS=same ;;
        1)  echo ">> Firmware się różni, wgrywam..."
            if avr_write; then STATUS=written; else STATUS=error; fi ;;
        *)  STATUS=error ;;
    esac

    if [ "$STATUS" = error ]; then
        log_flash "$SLAVE" "BŁĄD"
        if [ -z "${FAILED[$SLAVE]:-}" ]; then
            FAILED[$SLAVE]=1
            notify "$LABEL: BŁĄD wgrywania - sprawdź kabel. Ponowię, gdy znów wykryję slave-a."
        fi
        sleep 2
        continue
    fi

    sleep 3 # bootloader + setup()
    if [ "$SLAVE" = "?" ]; then
        LAST_UNKNOWN=1
        UNKNOWN=$((UNKNOWN + 1))
        RESULT="OK"
    elif MEM=$(py query "$SLAVE"); then
        RESULT="OK ($MEM)"
    else
        RESULT="OK, brak odpowiedzi I2C"
    fi
    [ "$STATUS" = same ] && RESULT="bez zmian, $RESULT"
    log_flash "$SLAVE" "$RESULT"

    if [ "$SLAVE" != "?" ]; then
        DONE[$SLAVE]=1
        unset "FAILED[$SLAVE]"
        if [ "$STATUS" = written ]; then WRITTEN+=("$SLAVE"); else SAME+=("$SLAVE"); fi
    fi
    LEFT=$(remaining)
    if [ -n "$LEFT" ]; then
        notify "$LABEL: ${RESULT}. Zostały: $LEFT - przepnij kabel."
    fi
done
exit 0
