definition(
    name: "Virtual Thermostat Manager",
    namespace: "piratemedia/smartthings",
    author: "Eliot S.",
    description: "Control a heater in conjunction with any temperature sensor like a SmartSense Multi, to create a thermostat device in SmartThings",
    category: "Green Living",
    iconUrl: "https://raw.githubusercontent.com/eliotstocker/SmartThings-VirtualThermostat-WithDTH/master/logo-small.png",
    iconX2Url: "https://raw.githubusercontent.com/eliotstocker/SmartThings-VirtualThermostat-WithDTH/master/logo.png",
	singleInstance: true
)

preferences {
    page(name: "Install", title: "Thermostat Manager", install: true, uninstall: true) {
        section("Devices") {
        }
        section {
            app(name: "thermostats", appName: "Virtual Thermostat With Device", namespace: "piratemedia/smartthings", title: "New Thermostat", multiple: true)
        }
    }
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
}