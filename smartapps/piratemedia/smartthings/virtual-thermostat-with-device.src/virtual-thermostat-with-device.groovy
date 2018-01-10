definition(
    name: "Virtual Thermostat With Device",
    namespace: "piratemedia/smartthings",
    author: "Eliot S.",
    description: "Control a heater in conjunction with any temperature sensor, like a SmartSense Multi.",
    category: "Green Living",
    iconUrl: "https://raw.githubusercontent.com/eliotstocker/SmartThings-VirtualThermostat-WithDTH/master/logo-small.png",
    iconX2Url: "https://raw.githubusercontent.com/eliotstocker/SmartThings-VirtualThermostat-WithDTH/master/logo.png"
)

preferences {
	section("Choose a temperature sensor... "){
		input "sensor", "capability.temperatureMeasurement", title: "Sensor"
	}
	section("Select the heater outlet(s)... "){
		input "outlets", "capability.switch", title: "Outlets", multiple: true
	}
	section("Only heat when contact isnt open (optional, leave blank to not require contact sensor)..."){
		input "motion", "capability.contactSensor", title: "Contact", required: false
	}
	section("Never go below this temperature: (optional)"){
		input "emergencySetpoint", "decimal", title: "Emergency Temp", required: false
	}
	section("Temperature Threshold (Don't allow heating to go above or bellow this amount from set temperature)") {
		input "threshold", "decimal", "title": "Temperature Threshold", required: false, defaultValue: 1.0
	}
}

def installed()
{
    log.debug "running installed"
    state.deviceID = Math.abs(new Random().nextInt() % 9999) + 1
	state.lastTemp = null
    state.contact = true
    /*def thermostat = createDevice()
    
	subscribe(sensor, "temperature", temperatureHandler)
	if (motion) {
		subscribe(motion, "motion", motionHandler)
	}
    
    subscribe(thermostat, "thermostatSetpoint", thermostatTemperatureHandler)
    subscribe(thermostat, "thermostatMode", thermostatModeHandler)
    thermostat.setVirtualTemperature(sensor.currentValue("temperature"))*/
}

def createDevice() {
    def thermostat
    def label = app.getLabel()
    log.debug "create device with id: pmvt$state.deviceID, named: $label, hub: $sensor.hub.id"
    try {
        thermostat = addChildDevice("piratemedia/smartthings", "Virtual Thermostat Device", "pmvt" + state.deviceID, null, [label: label, name: label, completedSetup: true])
    } catch(e) {
        log.error("caught exception", e)
    }
    return thermostat
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
    def thermostat = getThermostat()
    if(thermostat == null) {
        thermostat = createDevice()
    }
    state.contact = true
	state.lastTemp = null
	subscribe(sensor, "temperature", temperatureHandler)
	if (motion) {
		subscribe(motion, "contact", motionHandler)
	}
    subscribe(thermostat, "thermostatSetpoint", thermostatTemperatureHandler)
    subscribe(thermostat, "thermostatMode", thermostatModeHandler)
    thermostat.setVirtualTemperature(sensor.currentValue("temperature"))
}

def temperatureHandler(evt)
{
    def thermostat = getThermostat()
    thermostat.setVirtualTemperature(sensor.currentValue("temperature"))
	if (state.contact || emergencySetpoint) {
		evaluate(evt.doubleValue, thermostat.currentValue("thermostatSetpoint"))
        state.lastTemp = evt.doubleValue
	}
	else {
		heatingOff()
	}
}

def motionHandler(evt)
{
    def thermostat = getThermostat()
	if (evt.value == "closed") {
    	state.contact = true
		def thisTemp = sensor.currentTemperature
		if (thisTemp != null) {
			evaluate(thisTemp, thermostat.currentValue("thermostatSetpoint"))
			state.lastTemp = thisTemp
		}
	} else if (evt.value == "open") {
        log.debug "should turn heating off"
    	state.contact = false
	    heatingOff()
	}
}

def thermostatTemperatureHandler(evt) {
	def temperature = evt.doubleValue
    //setpoint = temperature
	log.debug "Desired Temperature set to: $temperature $state.contact"
    
    def thisTemp = sensor.currentTemperature
	if (state.contact) {
		evaluate(thisTemp, temperature)
	}
	else {
		heatingOff()
	}
}

def thermostatModeHandler(evt) {
	def mode = evt.value
	log.debug "Mode Changed to: $mode"
    def thermostat = getThermostat()
    
    def thisTemp = sensor.currentTemperature
	if (state.contact) {
		evaluate(thisTemp, thermostat.currentValue("thermostatSetpoint"))
	}
	else {
		heatingOff()
	}
}

private evaluate(currentTemp, desiredTemp)
{
	log.debug "EVALUATE($currentTemp, $desiredTemp)"
	// heater
	if ( (desiredTemp - currentTemp >= threshold)) {
		heatingOn()
	}
	if ( (currentTemp - desiredTemp >= threshold)) {
		heatingOff()
	}
}

def heatingOn() {
    if(thermostat.currentValue('thermostatMode') == 'heat' || force) {
    	log.debug "Heating on Now"
        outlets.on()
        thermostat.setHeatingStatus(true)
    } else {
        heatingOff(true)
    }
}

def heatingOff(heatingOff) {
	def thisTemp = sensor.currentTemperature
    if (thisTemp <= emergencySetpoint) {
        log.debug "Heating in Emergency Mode Now"
        outlet.on()
        thermostat.setEmergencyMode(true)
    } else {
    	log.debug "Heating off Now"
    	outlets.off()
		if(heatingOff) {
			thermostat.setHeatingOff(true)
		} else {
			thermostat.setHeatingStatus(false)
		}
    }
}