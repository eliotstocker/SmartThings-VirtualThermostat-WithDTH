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
	section("Only heat/cool when contact(s) aren't open (optional, leave blank to not require contact sensor)..."){
		input "motion", "capability.contactSensor", title: "Contact", required: false, multiple: true
	}
	section("Never go below this temperature: (optional)"){
		input "emergencyHeatingSetpoint", "decimal", title: "Emergency Min Temp", required: false
	}
    section("Never go above this temperature: (optional)"){
        input "emergencyCoolingSetpoint", "decimal", title: "Emergency Max Temp", required: false
    }
	section("The minimum difference between the heating and cooling setpoint, it's recommended to not put this too low to conserve energy") {
		input "heatCoolDelta", "decimal", title: "Heat / Cool Delta", required: false, defaultValue: 3.0
	}
	section("The amount that the temperature is allowed to dip below the heating setpoint before engaging heating, it's recommended to not put this too low to avoid heaters turning on and off too frequently") {
		input "heatDiff", "decimal", title: "Heat Differential", required: false, defaultValue: 0.3
	}
	section("The amount that the temperature is allowed to go above the cooling setpoint before engaging cooling, it's recommended to not put this too low to avoid coolers turning on and off too frequently") {
		input "coolDiff", "decimal", title: "Cool Differential", required: false, defaultValue: 0.3
	}
}

def installed()
{
    log.debug "running installed"
    state.deviceID = Math.abs(new Random().nextInt() % 9999) + 1
    updated()
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
    def temp = getAverageTemperature()

    //if temperature is below emergency setpoint
    if(emergencyHeatingSetpoint && emergencyHeatingSetpoint > temp) {
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
    if(temp > thermostat.currentValue("adjustedHeatingPoint")) {
	    return false;
    }

    return true;
}

def shouldCoolingBeOn(thermostat) {    
    def temp = getAverageTemperature()
    
    //if temperature is above emergency setpoint
    if(emergencyCoolingSetpoint && emergencyCoolingSetpoint < temp) {
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
    if(temp < thermostat.currentValue("adjustedCoolingPoint")) {
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

def switchOff(switches) {
	for(s in switches) {
    	if(s.currentValue("switch") != 'off'){
        	s.off()
        }
    }
}

def switchOn(switches) {
	for(s in switches) {
    	if(s.currentValue("switch") != 'on'){
        	s.on()
        }
    }
}

def cool() {
	log.debug "cooling outlets on, current value: " + cooling_outlets.currentValue("switch")
    if(thermostat.currentValue("thermostatOperatingState") != 'cooling') {
    	switchOn(cooling_outlets)
        if(thermostat.currentValue("thermostatOperatingState") == 'heating') {
    		switchOff(heating_outlets)
        }
    	thermostat.setThermostatOperatingState('cooling')
    }
}

def heat() {
    if(thermostat.currentValue("thermostatOperatingState") != 'heating') {
	    log.debug "heating outlets on"
    	switchOn(heating_outlets)
        if(thermostat.currentValue("thermostatOperatingState") == 'cooling') {
	    	switchOff(cooling_outlets)
        }
    	thermostat.setThermostatOperatingState('heating')
    }
}

def off() {
    if(thermostat.currentValue("thermostatOperatingState") != 'off') {
	    log.debug "off: all outlets off"
        if(thermostat.currentValue("thermostatOperatingState") == 'heating') {
    		switchOff(heating_outlets)
        }
        if(thermostat.currentValue("thermostatOperatingState") == 'cooling') {
	    	switchOff(cooling_outlets)
        }
    	thermostat.setThermostatOperatingState('off')
    }
}

def idle() {
    if(thermostat.currentValue("thermostatOperatingState") != 'idle') {
	    log.debug "idle: all outlets off"
        if(thermostat.currentValue("thermostatOperatingState") == 'heating') {
    		switchOff(heating_outlets)
        }
        if(thermostat.currentValue("thermostatOperatingState") == 'cooling') {
	    	switchOff(cooling_outlets)
        }
    	thermostat.setThermostatOperatingState('idle')
    }
}

def handleChange() {
    def thermostat = getThermostat()
    if(thermostat) {
        log.debug "handle change, mode: " + thermostat.currentValue('thermostatMode') + 
            ", temp: " + getAverageTemperature() + 
            ", coolingSetPoint: " + thermostat.currentValue("coolingSetpoint") +
            ", thermostatSetPoint: " + thermostat.currentValue("thermostatSetpoint") +
            ", heatingSetPoint: " + thermostat.currentValue("heatingSetpoint")
            
        /*def attrs = thermostat.supportedAttributes
		attrs.each {
  		  log.debug "${thermostat.displayName}, attribute: ${it.name}, dataType: ${it.dataType}, value: " + thermostat.currentValue(it.name)
		}*/

		switch (thermostat.currentValue('thermostatMode')){
            case "heat":
                if(shouldHeatingBeOn(thermostat)) {
                    heat()
                } else {
                    idle()
                }
                break
            case "cool":
                if(shouldCoolingBeOn(thermostat)) {
                    cool()
                } else {
                    idle()
                }
                break
            case "auto":
                if(shouldCoolingBeOn(thermostat)) {
                    cool()
                } else if(shouldHeatingBeOn(thermostat)) {
                    heat()
                } else {
                    idle()
                }
                break
            case "off":
            default:
                off()
                break
        }
	    getThermostat().setVirtualTemperature(getAverageTemperature())
    }
}

def getThermostat() {
    return getChildDevice("pmvt" + state.deviceID)
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
    subscribe(thermostat, "thermostatSetpoint", thermostatSetPointHandler)
    subscribe(thermostat, "heatingSetpoint", heatingSetPointHandler)
    subscribe(thermostat, "coolingSetpoint", coolingSetPointHandler)
    subscribe(thermostat, "thermostatMode", thermostatModeHandler)
    
    //reset some values
    thermostat.clearSensorData()
    thermostat.setVirtualTemperature(getAverageTemperature())
    thermostat.setHeatCoolDelta(heatCoolDelta)
    thermostat.setHeatDiff(heatDiff)
    thermostat.setCoolDiff(coolDiff)
}

def thermostatSetPointHandler(evt) {
    log.debug "thermostatSetPointHandler: ${evt.stringValue}"
    handleChange()
}

def coolingSetPointHandler(evt) {
	log.debug "coolingSetPointHandler: ${evt.stringValue}"
	handleChange()
}

def heatingSetPointHandler(evt) {
	log.debug "heatingSetPointHandler: ${evt.stringValue}"
	handleChange()
}

def temperatureHandler(evt) {
	log.debug "temperatureHandler: ${evt.stringValue}"
//    getThermostat().setVirtualTemperature(getAverageTemperature())
    handleChange()
}

def motionHandler(evt) {
	log.debug "motionHandler: ${evt.stringValue}"
    handleChange()
}

def thermostatModeHandler(evt) {
	log.debug "thermostatModeHandler: ${evt.stringValue}"
	handleChange()
}
