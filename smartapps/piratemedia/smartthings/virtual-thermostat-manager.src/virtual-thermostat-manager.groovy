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
	section("Temperature Scale"){
		input "scale", "bool", title: "Use Centigrade Scale", defaultValue: true
	}
    section("Devices") {
    }
    section {
        app(name: "thermostats", appName: "Virtual Thermostat With Device", namespace: "piratemedia/smartthings", title: "New Thermostat", multiple: true)
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def updateChildTempScales() {
    def children = getChildApps()
    children.each { child ->
        child.updateTempScale()
    }
}

def initialize() {
    updateChildTempScales()
}

def getTemperatureScale() {
    return scale ? "C" : "F"
}