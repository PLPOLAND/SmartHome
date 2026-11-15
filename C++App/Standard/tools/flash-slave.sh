#!/bin/bash
# Wgrywa firmware na slave-a podłączonego do Raspberry Pi przez Arduino Uno bez ATmegi
# (TX/RX/RESET/GND Uno -> gniazdo RJ45 slave-a, BEZ +5V).
#
# Użycie:
#   flash-slave.sh [-a ADRES_I2C] [-p PORT] [-n] [plik.hex]
#
#   -a ADRES  adres I2C slave-a (DIP switch, dziesiętnie). Po wgraniu skrypt sprawdza,
#             czy slave odpowiada na komendę 'M' (pamięć), zanim uruchomi aplikację.
#   -p PORT   port Uno (domyślnie /dev/ttyACM0)
#   -n        nie zatrzymuj aplikacji smarthome (niezalecane - po ~7 s błędów I2C
#             aplikacja odcina zasilanie wszystkim slave-om)
#   plik.hex  domyślnie /home/pi/firmware/slave-latest.hex
#
# Każde wgranie jest dopisywane do /home/pi/firmware/flash-history.log.

set -u

FW_DIR=/home/pi/firmware
HEX="$FW_DIR/slave-latest.hex"
PORT=/dev/ttyACM0
ADDR=""
STOP_APP=1
SERVICE=smarthome

usage() {
    sed -n '2,15p' "$0" | sed 's/^# \{0,1\}//'
    exit "${1:-0}"
}

while getopts "a:p:nh" opt; do
    case $opt in
        a) ADDR=$OPTARG ;;
        p) PORT=$OPTARG ;;
        n) STOP_APP=0 ;;
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
if [ ! -e "$PORT" ]; then
    echo "Brak portu $PORT - czy Uno jest podłączone do USB?" >&2
    exit 1
fi
if [ -n "$ADDR" ] && ! [[ "$ADDR" =~ ^[0-9]+$ && "$ADDR" -ge 1 && "$ADDR" -le 127 ]]; then
    echo "Nieprawidłowy adres I2C: $ADDR" >&2
    exit 1
fi

# Odpytuje slave-a komendą 'M' (combined write+read na /dev/i2c-1).
# Wypisuje "heap=... untouched=... freelist=..." albo kończy się błędem.
query_memory() {
    python3 - "$1" <<'EOF'
import ctypes, fcntl, os, sys, time
addr = int(sys.argv[1])
class Msg(ctypes.Structure):
    _fields_ = [("addr", ctypes.c_uint16), ("flags", ctypes.c_uint16),
                ("len", ctypes.c_uint16), ("buf", ctypes.POINTER(ctypes.c_uint8))]
class RdWr(ctypes.Structure):
    _fields_ = [("msgs", ctypes.POINTER(Msg)), ("nmsgs", ctypes.c_uint32)]
I2C_RDWR, I2C_M_RD = 0x0707, 0x0001
fd = os.open("/dev/i2c-1", os.O_RDWR)
try:
    for attempt in range(5):
        wbuf = (ctypes.c_uint8 * 1)(ord('M'))
        rbuf = (ctypes.c_uint8 * 8)()
        msgs = (Msg * 2)(Msg(addr, 0, 1, wbuf), Msg(addr, I2C_M_RD, 8, rbuf))
        try:
            fcntl.ioctl(fd, I2C_RDWR, RdWr(msgs, 2))
        except OSError:
            time.sleep(0.5)
            continue
        r = list(rbuf)
        if r[0] == ord('M'):
            le = lambda i: r[i] | (r[i + 1] << 8)
            print("heap=%d B, nietknięty RAM=%d B, freelist=%d B" % (le(1), le(3), le(5)))
            sys.exit(0)
        time.sleep(0.5)
    sys.exit(1)
finally:
    os.close(fd)
EOF
}

APP_WAS_ACTIVE=0
if [ "$STOP_APP" -eq 1 ] && systemctl is-active --quiet "$SERVICE"; then
    APP_WAS_ACTIVE=1
fi

restore_app() {
    if [ "$APP_WAS_ACTIVE" -eq 1 ]; then
        echo ">> Uruchamiam $SERVICE"
        sudo -n systemctl start "$SERVICE" || echo "!! Nie udało się uruchomić $SERVICE - zrób to ręcznie: sudo systemctl start $SERVICE" >&2
    fi
}
trap restore_app EXIT

if [ "$APP_WAS_ACTIVE" -eq 1 ]; then
    echo ">> Zatrzymuję $SERVICE (żeby nie restartowała slave-ów w trakcie wgrywania)"
    sudo -n systemctl stop "$SERVICE" || { echo "!! Nie udało się zatrzymać $SERVICE" >&2; exit 1; }
fi

echo ">> Wgrywam $(basename "$HEX") (md5 $(md5sum "$HEX" | cut -c1-8)) przez $PORT${ADDR:+ na slave $ADDR}"
OK=0
for try in 1 2 3; do
    if avrdude -q -p m328p -c arduino -P "$PORT" -b 115200 -U "flash:w:$HEX:i"; then
        OK=1
        break
    fi
    echo "!! Próba $try nieudana, ponawiam..." >&2
    sleep 2
done

RESULT="BŁĄD"
if [ "$OK" -eq 1 ]; then
    RESULT="OK"
    echo ">> Wgrano i zweryfikowano."
    if [ -n "$ADDR" ]; then
        sleep 3 # bootloader + setup()
        if MEM=$(query_memory "$ADDR"); then
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

mkdir -p "$FW_DIR"
echo "$(date '+%F %T')  slave=${ADDR:-?}  $(basename "$HEX")  md5=$(md5sum "$HEX" | cut -c1-8)  $RESULT" >> "$FW_DIR/flash-history.log"

[ "$OK" -eq 1 ]
