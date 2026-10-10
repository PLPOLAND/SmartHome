#!/usr/bin/env bash
#
# Automatyczny deploy SmartHome na Raspberry Pi (do uruchamiania z crona).
#
# Sprawdza, czy na wskazanym branchu pojawiły się nowe commity. Jeśli tak:
#   1. pobiera zmiany do osobnego klonu repo (tylko do budowania),
#   2. buduje jar mavenem,
#   3. podmienia jar (stary zostaje jako <jar>.prev),
#   4. restartuje usługę i sprawdza, czy wstała - jeśli nie, przywraca poprzedni jar.
#
# Użycie:
#   raspi-autodeploy.sh -b <branch> [opcje]
#
# Opcje:
#   -b BRANCH    branch do śledzenia (wymagany), np. dev albo master
#   -r DIR       katalog klonu do budowania       (domyślnie: /home/pi/smarthome-build)
#   -u URL       adres repo, gdy klonu jeszcze nie ma (domyślnie: https://github.com/PLPOLAND/SmartHome.git)
#   -j PATH      docelowa ścieżka jara             (domyślnie: /home/pi/Desktop/SmartHomeWebApp-2.0.1.jar,
#                                                  czyli jar z ExecStart w smarthome.service)
#   -s SERVICE   usługa systemd do restartu        (domyślnie: smarthome)
#   -t           uruchom testy przy budowaniu (domyślnie pomijane - na Pi trwają długo)
#   -f           wymuś build i deploy, nawet bez nowych commitów
#   -h           pomoc
#
# Każdą opcję można też ustawić zmienną środowiskową: BRANCH, BUILD_DIR, REPO_URL,
# JAR_PATH, SERVICE, RUN_TESTS=1, STATE_DIR (zapamiętany wdrożony commit, domyślnie
# /home/pi/.smarthome-autodeploy), HEALTH_URL (adres HTTP, który po restarcie musi
# odpowiedzieć - dowolnym kodem; domyślnie http://localhost:8080/, pusty = tylko stan
# usługi), HEALTH_TIMEOUT (sekundy, domyślnie 300 - Spring na Pi startuje długo),
# RESTART_CMD (własna komenda restartu zamiast "sudo systemctl restart $SERVICE"),
# MVN (komenda mavena, domyślnie ./mvnw z repo; np. MVN=mvn dla systemowego).
#
# Przykład wpisu w crontab użytkownika pi (co 5 minut, branch dev):
#   */5 * * * * /home/pi/raspi-autodeploy.sh -b dev >> /home/pi/smarthome-deploy.log 2>&1
# Restart wymaga wpisu w sudoers (sudo visudo -f /etc/sudoers.d/smarthome-deploy):
#   pi ALL=(root) NOPASSWD: /bin/systemctl restart smarthome
#
# UWAGA: klon w BUILD_DIR jest wyłącznie do budowania - skrypt robi w nim
# "git reset --hard". Aplikacja musi działać z innego katalogu (jej dane są
# w smarthome/database względem katalogu roboczego usługi), inaczej reset
# nadpisałby bazę danych.

set -euo pipefail

BRANCH="${BRANCH:-}"
BUILD_DIR="${BUILD_DIR:-/home/pi/smarthome-build}"
REPO_URL="${REPO_URL:-https://github.com/PLPOLAND/SmartHome.git}"
JAR_PATH="${JAR_PATH:-/home/pi/Desktop/SmartHomeWebApp-2.0.1.jar}"
SERVICE="${SERVICE:-smarthome}"
RUN_TESTS="${RUN_TESTS:-0}"
HEALTH_URL="${HEALTH_URL-http://localhost:8080/}"
HEALTH_TIMEOUT="${HEALTH_TIMEOUT:-300}"
RESTART_CMD="${RESTART_CMD:-}"
MVN="${MVN:-./mvnw}"
FORCE=0

# katalog projektu mavena wewnątrz repo
APP_SUBDIR="javaApp/SmartHome"

usage() { sed -n '2,/^set -euo/p' "$0" | sed '$d' | sed 's/^# \{0,1\}//'; }

while getopts ":b:r:u:j:s:tfh" opt; do
    case "$opt" in
        b) BRANCH="$OPTARG" ;;
        r) BUILD_DIR="$OPTARG" ;;
        u) REPO_URL="$OPTARG" ;;
        j) JAR_PATH="$OPTARG" ;;
        s) SERVICE="$OPTARG" ;;
        t) RUN_TESTS=1 ;;
        f) FORCE=1 ;;
        h) usage; exit 0 ;;
        :) echo "Opcja -$OPTARG wymaga wartości" >&2; exit 2 ;;
        *) echo "Nieznana opcja: -$OPTARG" >&2; usage >&2; exit 2 ;;
    esac
done

if [[ -z "$BRANCH" ]]; then
    echo "Podaj branch: -b <branch>" >&2
    exit 2
fi

# cron ma ubogie PATH - dokładamy typowe katalogi
export PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:$PATH"

# stan poza klonem - "git clean" w klonie by go skasował
STATE_DIR="${STATE_DIR:-/home/pi/.smarthome-autodeploy}"

if [[ -n "$HEALTH_URL" ]] && ! command -v curl >/dev/null; then
    echo "Brak curl - sprawdzam tylko stan usługi (sudo apt install curl, by sprawdzać $HEALTH_URL)" >&2
    HEALTH_URL=""
fi
DEPLOYED_FILE="$STATE_DIR/deployed-$(echo "$BRANCH" | tr '/' '_')"
FAILED_FILE="$DEPLOYED_FILE.failed"
LOCK_FILE="$STATE_DIR/lock"
mkdir -p "$STATE_DIR"

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] [$BRANCH] $*"; }
die() { log "BŁĄD: $*"; exit 1; }

# tylko jedna instancja naraz (build na Pi potrafi trwać dłużej niż interwał crona)
exec 9>"$LOCK_FILE"
if ! flock -n 9; then
    log "Poprzedni deploy jeszcze trwa - pomijam."
    exit 0
fi

restart_service() {
    if [[ -n "$RESTART_CMD" ]]; then
        bash -c "$RESTART_CMD"
    elif [[ $EUID -eq 0 ]]; then
        systemctl restart "$SERVICE"
    else
        sudo -n systemctl restart "$SERVICE"
    fi
}

service_healthy() {
    local deadline=$((SECONDS + HEALTH_TIMEOUT)) state
    sleep 5
    while (( SECONDS < deadline )); do
        if [[ -z "$RESTART_CMD" ]]; then
            state="$(systemctl is-active "$SERVICE" 2>/dev/null || true)"
            case "$state" in
                active) ;;
                activating|reloading) sleep 5; continue ;;
                *) return 1 ;;
            esac
        fi
        if [[ -n "$HEALTH_URL" ]]; then
            # każda odpowiedź HTTP (też 302/404) znaczy, że Spring wystartował
            curl -s -o /dev/null --max-time 5 "$HEALTH_URL" && return 0
            sleep 5
            continue
        fi
        # bez adresu HTTP: usługa ma się utrzymać aktywna przez 20 s (błędny start Springa pada wcześniej)
        sleep 20
        [[ -n "$RESTART_CMD" ]] || systemctl is-active --quiet "$SERVICE"
        return
    done
    return 1
}

# --- 1. klon i sprawdzenie nowych commitów -------------------------------------

if [[ ! -d "$BUILD_DIR/.git" ]]; then
    log "Brak klonu w $BUILD_DIR - klonuję $REPO_URL"
    git clone --branch "$BRANCH" "$REPO_URL" "$BUILD_DIR"
fi
cd "$BUILD_DIR"

git fetch --quiet origin "+refs/heads/$BRANCH:refs/remotes/origin/$BRANCH" \
    || die "Nie udało się pobrać brancha $BRANCH z origin"

REMOTE_SHA="$(git rev-parse "origin/$BRANCH")"
DEPLOYED_SHA="$(cat "$DEPLOYED_FILE" 2>/dev/null || true)"

if [[ $FORCE -eq 0 ]]; then
    if [[ "$REMOTE_SHA" == "$DEPLOYED_SHA" ]]; then
        exit 0 # nic nowego - cisza w logu
    fi
    if [[ "$REMOTE_SHA" == "$(cat "$FAILED_FILE" 2>/dev/null || true)" ]]; then
        exit 0 # ten commit już się nie udał - czekamy na kolejny (albo -f)
    fi
fi

PREV_SHORT="${DEPLOYED_SHA:0:8}"
log "Nowe zmiany: ${PREV_SHORT:-(brak)} -> ${REMOTE_SHA:0:8}"

git checkout --quiet -B "$BRANCH" "origin/$BRANCH"
git reset --quiet --hard "origin/$BRANCH"
git clean -fdq

# zmiany poza aplikacją Javy (np. tylko firmware C++) nie wymagają restartu
if [[ $FORCE -eq 0 && -n "$DEPLOYED_SHA" ]] && git cat-file -e "$DEPLOYED_SHA^{commit}" 2>/dev/null \
        && git diff --quiet "$DEPLOYED_SHA" "$REMOTE_SHA" -- "$APP_SUBDIR"; then
    log "Brak zmian w $APP_SUBDIR - zapisuję commit bez budowania."
    echo "$REMOTE_SHA" > "$DEPLOYED_FILE"
    exit 0
fi

git log --oneline "${DEPLOYED_SHA:+$DEPLOYED_SHA..}$REMOTE_SHA" -n 20 2>/dev/null | sed 's/^/    /' || true

# --- 2. build -------------------------------------------------------------------

cd "$BUILD_DIR/$APP_SUBDIR"
[[ "$MVN" != "./mvnw" ]] || chmod +x mvnw
MVN_ARGS=(-B -q clean package)
[[ "$RUN_TESTS" == "1" ]] || MVN_ARGS+=(-DskipTests)

log "Buduję ($( [[ "$RUN_TESTS" == "1" ]] && echo "z testami" || echo "bez testów" ))..."
if ! $MVN "${MVN_ARGS[@]}"; then
    echo "$REMOTE_SHA" > "$FAILED_FILE"
    die "Build nie powiódł się - jar nie został podmieniony."
fi

NEW_JAR="$(ls -t target/*.jar 2>/dev/null | grep -v '\.original$' | head -n 1 || true)"
[[ -n "$NEW_JAR" && -s "$NEW_JAR" ]] || { echo "$REMOTE_SHA" > "$FAILED_FILE"; die "Nie znaleziono zbudowanego jara w target/"; }
log "Zbudowano $NEW_JAR"

# --- 3. podmiana jara -----------------------------------------------------------

mkdir -p "$(dirname "$JAR_PATH")"
cp "$NEW_JAR" "$JAR_PATH.new"
if [[ -f "$JAR_PATH" ]]; then
    cp -p "$JAR_PATH" "$JAR_PATH.prev"
fi
mv -f "$JAR_PATH.new" "$JAR_PATH" # mv w obrębie katalogu jest atomowy

# --- 4. restart i weryfikacja ---------------------------------------------------

log "Restartuję usługę $SERVICE..."
restart_service || die "Restart usługi nie powiódł się"

if service_healthy; then
    echo "$REMOTE_SHA" > "$DEPLOYED_FILE"
    rm -f "$FAILED_FILE"
    log "Deploy ${REMOTE_SHA:0:8} zakończony sukcesem."
    exit 0
fi

log "Usługa nie wstała po deployu ${REMOTE_SHA:0:8}."
echo "$REMOTE_SHA" > "$FAILED_FILE"
if [[ -f "$JAR_PATH.prev" ]]; then
    log "Przywracam poprzedni jar i restartuję."
    cp -p "$JAR_PATH.prev" "$JAR_PATH"
    restart_service || true
    service_healthy && log "Poprzednia wersja działa." || log "Poprzednia wersja też nie wstała - sprawdź: journalctl -u $SERVICE"
fi
exit 1
