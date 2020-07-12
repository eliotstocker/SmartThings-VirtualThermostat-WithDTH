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
	section("Select the heater outlet(s)... (optional, leave blank if heating not required)"){
		input "heating_outlets", "capability.switch", title: "Heating Outlets", multiple: true, required: false
	}
    section("Select the cooling outlet(s)... (optional, leave blank if cooling not required)"){
        input "cooling_outlets", "capability.switch", title: "Cooling Outlets", multiple: true, required: false
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
		input "heatCoolDelta", "decimal", title: "Heat / Cool Delta", defaultValue: 3.0
	}
	section("The amount that the temperature is allowed to dip below the heating setpoint before engaging heating, it's recommended to not put this too low to avoid heaters turning on and off too frequently") {
		input "heatDiff", "decimal", title: "Heat Differential", defaultValue: 0.3
	}
	section("The amount that the temperature is allowed to go above the cooling setpoint before engaging cooling, it's recommended to not put this too low to avoid coolers turning on and off too frequently") {
		input "coolDiff", "decimal", title: "Cool Differential", defaultValue: 0.3
	}

    section("Fix for unreliable switches to automatically turn them on/off again, if it seems like turning them on/off did not work based on the temperature (Experimental)") {
        input "unreliableSwitchFix", "bool", title: "Unreliable switch fix", defaultValue: false
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
	if(switches) {
        log.debug "switching off: ${switches}, current values: " + switches.currentValue("switch")
        for(s in switches) {
            s.off()
        }
        log.debug "done switching off: ${switches}, current values: " + switches.currentValue("switch")
    } else {
    	log.debug "nothing to switch off"
    }
}

def switchOn(switches) {
	if(switches) {
        log.debug "switching on: ${switches}, current values: " + switches.currentValue("switch")
        for(s in switches) {
            s.on()
        }
        log.debug "done switching on: ${switches}, current values: " + switches.currentValue("switch")
    } else {
    	log.debug "nothing to switch on"
    }
}

//set the expected direction (heat/cool/none) to be able to monitor if it's working
def setExpectedDirection(direction) {
    log.debug "direction change to ${direction}"
    state.expectedDirection = direction
    state.tempAtDirectionChange = state.curTemp
    state.directionChangeTime = new Date().getTime()
}

def temperatureHandler(evt) {
    state.curTemp = getAverageTemperature()
	def now = new Date().getTime()
    def minSinceDirectionChange = (now - state.directionChangeTime)/(1000*60)
    log.debug "temperatureHandler: ${evt.stringValue}, curTemp: ${state.curTemp}" +
    	", expectedDirection: ${state.expectedDirection}" +
    	", minSinceDirectionChange: ${minSinceDirectionChange}, now: ${now}, directionChangeTime: ${state.directionChangeTime}, tempAtDirectionChange: ${state.tempAtDirectionChange}" 

    if(state.expectedDirection != 'none') {
    	def directionChangeWorked = false;
        if(state.expectedDirection == 'cool') { 
        	directionChangeWorked = state.curTemp < state.tempAtDirectionChange
        }
        if(state.expectedDirection == 'heat') { 
           	directionChangeWorked = state.curTemp > state.tempAtDirectionChange
        }

        if(minSinceDirectionChange > 8 && minSinceDirectionChange < 16){
        	//If we at any point in the in the period 8-16 min after a direction change see that the temperature is not trending in the right direction, we try to press the button again to ensure that it's truly pressed.
            //This is specifically to fix unreliable switches such as switchbot
			if(!directionChangeWorked) {        	
                if(!unreliableSwitchFix) {
                    log.debug "direction change did not work within 8 min, but since 'Unreliable Switch Fix' is off, nothing will be done. Minutes since direction change: ${minSinceDirectionChange}"                
                } else {
                    log.debug "direction change did not work within 8 min, try flipping the switch again and reset the timer. Minutes since direction change: ${minSinceDirectionChange}"
                    state.directionChangeTime = new Date().getTime()
                    def oState = thermostat.getOperatingState()
                    if(state.expectedDirection == 'cool') {
                        if(oState == 'cooling') {
                            switchOn(cooling_outlets)
                        } else {
                            switchOff(heating_outlets)
                        }
                    }
                    if(state.expectedDirection == 'heat') {
                        if(oState == 'heating') {
                            switchOn(heating_outlets)
                        } else {
                            switchOff(cooling_outlets)
                        }
                    }
                }
            }
        }
    }

    handleChange()
}


def cool() {
	//log.debug "cooling outlets on, current value: " + cooling_outlets.currentValue("switch")
    def oState = thermostat.getOperatingState()
    if(oState != 'cooling') {
        setExpectedDirection('cool')
    	thermostat.setThermostatOperatingState('cooling')
       	switchOn(cooling_outlets)
        if(oState == 'heating') {
		    switchOff(heating_outlets)
        }
    }
}

def heat() {
	//log.debug "heating outlets on, current value: " + heating_outlets.currentValue("switch")
    def oState = thermostat.getOperatingState()
    if(oState != 'heating') {
        setExpectedDirection('heat')
    	thermostat.setThermostatOperatingState('heating')
        switchOn(heating_outlets)
        if(oState == 'cooling') {
        	switchOff(cooling_outlets)
        }
    }
}

def off() {
	//log.debug "off, all outlets off, current value heating: " + heating_outlets.currentValue("switch") + ", cooling: " + cooling_outlets.currentValue("switch")
    def oState = thermostat.getOperatingState()
    if(oState != 'off') {
    	thermostat.setThermostatOperatingState('off')
        setExpectedDirection('none')
        if(oState == 'heating') {
        	switchOff(heating_outlets)
        } else if(oState == 'cooling') {
       		switchOff(cooling_outlets)
        }
    }
}

def idle() {
	//log.debug "idle, all outlets off, current value heating: " + heating_outlets.currentValue("switch") + ", cooling: " + cooling_outlets.currentValue("switch")
    def oState = thermostat.getOperatingState()
    if(oState != 'idle') {
    	thermostat.setThermostatOperatingState('idle')
        if(oState == 'heating') {
            setExpectedDirection('cool')
            switchOff(heating_outlets)
        } else if(oState == 'cooling') {
            setExpectedDirection('heat')
            switchOff(cooling_outlets)
        }
    }
}

def handleChange() {
    def thermostat = getThermostat()
    if(thermostat) {
        log.debug "handle change, mode: " + thermostat.currentValue('thermostatMode') + 
            ", operatingState: " + thermostat.currentValue("thermostatOperatingState") + 
            ", temp: " + getAverageTemperature() +
            ", coolingSetPoint: " + thermostat.currentValue("coolingSetpoint") +
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
    subscribe(thermostat, "heatingSetpoint", heatingSetPointHandler)
    subscribe(thermostat, "coolingSetpoint", coolingSetPointHandler)
    subscribe(thermostat, "thermostatMode", thermostatModeHandler)
    
    //reset some values
    setExpectedDirection('none')
    thermostat.clearSensorData()
    thermostat.setVirtualTemperature(getAverageTemperature())
    thermostat.setHeatCoolDelta(heatCoolDelta)
    thermostat.setHeatDiff(heatDiff)
    thermostat.setCoolDiff(coolDiff)
}

def coolingSetPointHandler(evt) {
	log.debug "coolingSetPointHandler: ${evt.stringValue}"
	handleChange()
}

def heatingSetPointHandler(evt) {
	log.debug "heatingSetPointHandler: ${evt.stringValue}"
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
