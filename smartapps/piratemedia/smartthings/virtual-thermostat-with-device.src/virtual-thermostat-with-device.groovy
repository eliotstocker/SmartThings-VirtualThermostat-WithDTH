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
    if(thermostat.currentValue("thermostatOperatingState") != 'cooling') {
	    log.debug "cooling outlets on"
    	cooling_outlets.on()
        if(thermostat.currentValue("thermostatOperatingState") == 'heating') {
    		heating_outlets.off()
        }
    	thermostat.setThermostatOperatingState('cooling')
    }
}

def heat() {
    if(thermostat.currentValue("thermostatOperatingState") != 'heating') {
	    log.debug "heating outlets on"
    	heating_outlets.on()
        if(thermostat.currentValue("thermostatOperatingState") == 'cooling') {
	    	cooling_outlets.off()
        }
    	thermostat.setThermostatOperatingState('heating')
    }
}

def off() {
    if(thermostat.currentValue("thermostatOperatingState") != 'off') {
	    log.debug "off: all outlets off"
        if(thermostat.currentValue("thermostatOperatingState") == 'heating') {
    		heating_outlets.off()
        }
        if(thermostat.currentValue("thermostatOperatingState") == 'cooling') {
	    	cooling_outlets.off()
        }
    	thermostat.setThermostatOperatingState('off')
    }
}

def idle() {
    if(thermostat.currentValue("thermostatOperatingState") != 'idle') {
	    log.debug "idle: all outlets off"
        if(thermostat.currentValue("thermostatOperatingState") == 'heating') {
    		heating_outlets.off()
        }
        if(thermostat.currentValue("thermostatOperatingState") == 'cooling') {
	    	cooling_outlets.off()
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
            ", heatingSetPoint: " + thermostat.currentValue("heatingSetpoint") +
            ", supportedAttributes: " + thermostat.getSupportedAttributes()
            
        def attrs = thermostat.supportedAttributes
		attrs.each {
  		  log.debug "${thermostat.displayName}, attribute: ${it.name}, dataType: ${it.dataType}, value: " + thermostat.currentValue(it.name)
		}

        //update device
        thermostat.setVirtualTemperature(getAverageTemperature())

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
    subscribe(thermostat, "thermostatSetpoint", thermostatSetPointHandler)
    subscribe(thermostat, "heatingSetpoint", heatingSetPointHandler)
    subscribe(thermostat, "coolingSetpoint", coolingSetPointHandler)
    subscribe(thermostat, "thermostatMode", thermostatModeHandler)
    
    //reset some values
    thermostat.clearSensorData()
    thermostat.setVirtualTemperature(getAverageTemperature())
    thermostat.setHeatCoolDelta(heatCoolDelta)
}

def thermostatSetPointHandler(evt) {
    def thermostat = getThermostat()
    def temp = thermostat.currentValue("thermostatSetpoint")
    def cool = thermostat.currentValue("coolingSetpoint")
    def heat = thermostat.currentValue("heatingSetpoint")
    def mode = thermostat.currentValue('thermostatMode')
    log.debug "thermostatSetPointHandler: " + temp + ", thermostatMode: " + mode
    
    if(mode == "heat" && heat != temp) {
		thermostat.setHeatingSetpoint(temp)
    } else if(mode == "cool" && cool != temp) {
		thermostat.setCoolingSetpoint(temp)
    }

	if(cool < temp) {
		thermostat.setCoolingSetpoint(temp)
    } else if(heat > temp) {
    	thermostat.setHeatingSetpoint(temp)
    }
    log.debug "calling handle change"
    handleChange()
}

def autoThermostatSetPoint() {
    def temp = thermostat.currentValue("thermostatSetpoint")
    def cool = thermostat.currentValue("coolingSetpoint")
    def heat = thermostat.currentValue("heatingSetpoint")
    def mode = thermostat.currentValue('thermostatMode')
    if(mode == "auto") {
		def newTemp = (cool + heat) / 2.0
        if(newTemp != temp) {
        	thermostat.setThermostatSetpoint(newTemp.setScale(1, BigDecimal.ROUND_HALF_EVEN))
        }
    }
}

def coolingSetPointHandler(evt) {
    def thermostat = getThermostat()
    def cool = thermostat.currentValue("coolingSetpoint")
	log.debug "coolingSetPointHandler: " + cool + ", heatCoolDelta: " + heatCoolDelta 

	if(thermostat.currentValue('thermostatMode') == "cool") {
		thermostat.setThermostatSetpoint(cool)
	} else if(thermostat.currentValue('thermostatMode') == "auto") {
    	autoThermostatSetPoint()
    }

    def targetHeat = cool - heatCoolDelta
	if(thermostat.currentValue("heatingSetpoint") > targetHeat) {
    	thermostat.setHeatingSetpoint(targetHeat)
    }

    if(thermostat.currentValue("thermostatSetpoint") > cool) {
    	thermostat.setThermostatSetpoint(cool)
    }

	handleChange()
}

def heatingSetPointHandler(evt) {
    def thermostat = getThermostat()
    def heat = thermostat.currentValue("heatingSetpoint")
	log.debug "heatingSetPointHandler: " + heat + ", heatCoolDelta: " + heatCoolDelta 

	if(thermostat.currentValue('thermostatMode') == "heat") {
		thermostat.setThermostatSetpoint(heat)
	} else if(thermostat.currentValue('thermostatMode') == "auto") {
    	autoThermostatSetPoint()
    }

    def targetCool = heat + heatCoolDelta
	if(thermostat.currentValue("coolingSetpoint") < targetCool) {
    	thermostat.setCoolingSetpoint(targetCool)
    }

    if(thermostat.currentValue("thermostatSetpoint") < heat) {
    	thermostat.setThermostatSetpoint(heat)
    }

	handleChange()
}

def temperatureHandler(evt) {
	log.debug "temperatureHandler: " + evt
    handleChange()
}

def motionHandler(evt) {
	log.debug "motionHandler: " + evt
    handleChange()
}

def thermostatModeHandler(evt) {
	log.debug "thermostatModeHandler: " + evt
    autoThermostatSetPoint()
	handleChange()
}
