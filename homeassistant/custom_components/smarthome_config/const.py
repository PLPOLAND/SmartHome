"""Constants of the SmartHome Config integration."""

DOMAIN = "smarthome_config"

CONF_BASE_TOPIC = "base_topic"
DEFAULT_BASE_TOPIC = "smarthome"

# How long the config flow waits for the retained configuration snapshot.
SNAPSHOT_TIMEOUT = 10
# Configuration changes re-send the whole slave configuration over I2C, which takes a while.
REQUEST_TIMEOUT = 30

DEVICE_TYPES = ["LIGHT", "GNIAZDKO", "WENTYLATOR", "BLIND"]
TYPE_BLIND = "BLIND"
SENSOR_TYPE_BUTTON = "BUTTON"
SENSOR_TYPE_HYGROMETER = "THERMOMETR_HYGROMETR"
ADDABLE_SENSOR_TYPES = [SENSOR_TYPE_BUTTON, SENSOR_TYPE_HYGROMETER]

# Firmware limit: its MAX_NUMBER_OF_BUTTON_FUNCTIONS (4) is an array length whose index 0 is
# unused, so at most 3 clicks can be stored. The server reports its configured limit.
MAX_CLICK_FUNCTIONS = 3
# Click function actions -> ButtonLocalFunction.State on the server.
ACTION_TOGGLE = "toggle"
CLICK_ACTIONS = {ACTION_TOGGLE: "NONE", "up": "UP", "down": "DOWN", "stop": "STOP"}
NO_DEVICE = "none"
