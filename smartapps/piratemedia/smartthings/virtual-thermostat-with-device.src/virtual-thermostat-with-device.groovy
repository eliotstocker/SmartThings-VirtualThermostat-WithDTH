definition(
    name: "Virtual Thermostat With Device",
    namespace: "piratemedia/smartthings",
    author: "Eliot S.",
    description: "Control a heater in conjunction with any temperature sensor, like a SmartSense Multi.",
    category: "Green Living",
    iconUrl: "https://raw.githubusercontent.com/eliotstocker/SmartThings-VirtualThermostat-WithDTH/master/logo-small.png",
    iconX2Url: "https://raw.githubusercontent.com/eliotstocker/SmartThings-VirtualThermostat-WithDTH/master/logo.png",
	parent: "piratemedia/smartthings:Virtual Thermostat Manager",
)

preferences {
	section("Choose a temperature sensor(s)... (If multiple sensors are selected, the average value will be used)"){
		input "sensors", "capability.temperatureMeasurement", title: "Sensor", multiple: true
	}
	section("Select the heater outlet(s)... "){
		input "heating_outlets", "capability.switch", title: "Heating Outlets", multiple: true
	}
    section("Select the cooling outlet(s)... "){
        input "cooling_outlets", "capability.switch", title: "Cooling Outlets", multiple: true
    }
	section("Only heat/cool when contact(s) arent open (optional, leave blank to not require contact sensor)..."){
		input "motion", "capability.contactSensor", title: "Contact", required: false, multiple: true
	}
	section("Never go below this temperature: (optional)"){
		input "emergencyHeatingSetpoint", "decimal", title: "Emergency Min Temp", required: false
	}
    section("Never go above this temperature: (optional)"){
        input "emergencyCoolingSetpoint", "decimal", title: "Emergency Max Temp", required: false
    }
	section("Temperature Threshold (Don't allow heating/cooling to go above or bellow this amount from set temperature)") {
		input "threshold", "decimal", title: "Temperature Threshold", required: false, defaultValue: 1.0
	}
}

def installed()
{
    log.debug "running installed"
    state.deviceID = Math.abs(new Random().nextInt() % 9999) + 1
}

def createDevice() {
    def thermostat
    def label = app.getLabel()
    

    log.debug "create device with id: pmvt$state.deviceID, named: $label" //, hub: $sensor.hub.id"
    try {
        thermostat = addChildDevice("piratemedia/smartthings", "Virtual Thermostat Device", "pmvt" + state.deviceID, null, [label: label, name: label, completedSetup: true])
    } catch(e) {
        log.error("caught exception", e)
    }
    return thermostat
}

def motionDetected(){
    if(motion) {
        for(m in motion) {
            if(m.currentValue('contact') == "open") {
                return true;
            }
        }
    }
    return false;
}


def shouldHeatingBeOn(thermostat) {    
    //if temperature is below emergency setpoint
    if(emergencyHeatingSetpoint && emergencyHeatingSetpoint > getAverageTemperature()) {
    	return true;
    }
    
	//if thermostat isn't set to heat
	if(thermostat.currentValue('thermostatMode') != "heat" && thermostat.currentValue('thermostatMode') != "auto") {
    	return false;
    }
    
    //if any of the contact sensors are open
    if(motionDetected()){
        return false;
    }

    //average temperature across all temperature sensors is above set point
    if (thermostat.currentValue("heatingSetpoint") < getAverageTemperature()) {
    	return false;
    }
    
    return true;
}

def shouldCoolingBeOn(thermostat) {    
    //if temperature is above emergency setpoint
    if(emergencyCoolingSetpoint && emergencyCoolingSetpoint < getAverageTemperature()) {
        return true;
    }
    
    //if thermostat isn't set to cool
    if(thermostat.currentValue('thermostatMode') != "cool" && thermostat.currentValue('thermostatMode') != "auto") {
        return false;
    }
    
    //if any of the contact sensors are open
    if(motionDetected()){
        return false;
    }
    
    //average temperature across all temperature sensors is below set point
    if (thermostat.currentValue("coolingSetpoint") > getAverageTemperature()) {
        return false;
    }
    
    return true;    
}

def getAverageTemperature() {
	def total = 0;
    def count = 0;
    
    //total all sensors temperature
	for(sensor in sensors) {
    	total += sensor.currentValue("temperature")
        thermostat.setIndividualTemperature(sensor.currentValue("temperature"), count, sensor.label)
        count++
    }
    
    //divide by number of sensors
    return total / count
}

def cool() {
    log.debug "cooling outlets on"
    cooling_outlets.on()
    heating_outlets.off()
    thermostat.setThermostatOperatingState('cooling')
}

def heat() {
    log.debug "heating outlets on"
    cooling_outlets.off()
    heating_outlets.on()
    thermostat.setThermostatOperatingState('heating')
}

def off() {
    log.debug "all outlets off"
    cooling_outlets.off()
    heating_outlets.off()
    thermostat.setThermostatOperatingState('idle')
}

def handleChange() {
    def thermostat = getThermostat()
    log.debug "handle change, mode: " + thermostat.currentValue('thermostatMode') + 
    	", temp: " + getAverageTemperature() + 
        ", coolingSetPoint: " + thermostat.currentValue("coolingSetpoint") +
        ", thermostatSetPoint: " + thermostat.currentValue("thermostatSetpoint") +
        ", heatingSetPoint: " + thermostat.currentValue("heatingSetpoint")


    //update device
    thermostat.setVirtualTemperature(getAverageTemperature())

    switch (thermostat.currentValue('thermostatMode')){
        case "heat":
            if(shouldHeatingBeOn(thermostat)) {
                heat()
            } else {
                off()
            }
            break
        case "cool":
            if(shouldCoolingBeOn(thermostat)) {
                cool()
            } else {
                off()
            }
            break
        case "auto":
            if(shouldCoolingBeOn(thermostat)) {
                cool()
            } else if(shouldHeatingBeOn(thermostat)) {
                heat()
            } else {
                off()
            }
            break
        case "off":
        default:
            off()
            break
    }
}


def getThermostat() {
	def child = getChildDevices().find {
    	d -> d.deviceNetworkId.startsWith("pmvt" + state.deviceID)
  	}
    return child
}

def uninstalled() {
    deleteChildDevice("pmvt" + state.deviceID)
}

def updated()
{
    log.debug "running updated: $app.label"
	unsubscribe()
    unschedule()
    
    //get or add thermostat
    def thermostat = getThermostat()
    if(thermostat == null) {
        thermostat = createDevice()
    }
    
    //subscribe to temperature changes
	subscribe(sensors, "temperature", temperatureHandler)
    
    //subscribe to contact sensor changes
	if (motion) {
		subscribe(motion, "contact", motionHandler)
	}
    
    //subscribe to virtual device changes
    subscribe(thermostat, "thermostatSetpoint", thermostatTemperatureHandler)
    subscribe(thermostat, "heatingSetpoint", thermostatTemperatureHandler)
    subscribe(thermostat, "coolingSetpoint", thermostatTemperatureHandler)

    subscribe(thermostat, "thermostatMode", thermostatModeHandler)
    
    //reset some values
    thermostat.clearSensorData()
    thermostat.setVirtualTemperature(getAverageTemperature())
}

def temperatureHandler(evt) {
    handleChange()
}

def motionHandler(evt) {
    handleChange()
}

def thermostatTemperatureHandler(evt) {
	handleChange()
}

def thermostatModeHandler(evt) {
	handleChange()
}