metadata {
	definition (name: "Virtual Thermostat Device", namespace: "piratemedia/smartthings", author: "Eliot S.") {
		capability "Actuator"
		capability "Refresh"
		capability "Sensor"
		capability "Thermostat"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
		capability "Thermostat Setpoint"
		capability "Temperature Measurement"

		command "refresh"
		command "poll"
        
		command "offbtn"
		command "heatbtn"
		command "setThermostatMode", ["string"]
		command "levelUpDown"
		command "levelUp"
		command "levelDown"
        command "heatingSetpointUp"
		command "heatingSetpointDown"
		command "log"
		command "changeMode"
        command "setVirtualTemperature", ["number"]
        command "setHeatingStatus", ["boolean"]
        command "setEmergencyMode", ["boolean"]
        
		attribute "temperatureUnit", "string"
		attribute "targetTemp", "string"
		attribute "debugOn", "string"
		attribute "safetyTempMin", "string"
		attribute "safetyTempMax", "string"
		attribute "safetyTempExceeded", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"temperature", type:"thermostat", width:6, height:4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'${currentValue} °C', unit: '°C')
			}
			tileAttribute("device.thermostatSetpoint", key: "VALUE_CONTROL") {
				attributeState("default", action: "levelUpDown")
				attributeState("VALUE_UP", action: "levelUp")
				attributeState("VALUE_DOWN", action: "levelDown")
			}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle",			backgroundColor:"#44B621")
				attributeState("heating",		 backgroundColor:"#FFA81E")
			}
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("off", label:'off')
				attributeState("heat", label:'heat')
			}
			tileAttribute("device.thermostatSetpoint", key: "HEATING_SETPOINT") {
				attributeState("default", label:'${currentValue}')
			}
		}
		valueTile("temp2", "device.temperature", width: 2, height: 2, decoration: "flat") {
			state("default", label:'${currentValue}°', icon:"https://raw.githubusercontent.com/eliotstocker/SmartThings-VirtualThermostat-WithDTH/master/device.png",
					backgroundColors: getTempColors(), canChangeIcon: true)
		}
		standardTile("thermostatMode", "device.thermostatMode", width:2, height:2, decoration: "flat") {
			state("off", 	action:"changeMode", nextState: "updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/off_btn_icon.png")
			state("heat", 	action:"changeMode", nextState: "updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_btn_icon.png")
			state("updating", label:"", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cmd_working.png")
		}
        
		standardTile("offBtn", "device.off", width:1, height:1, decoration: "flat") {
			state("default", action: "offbtn", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/off_btn_icon.png")
		}
		standardTile("heatBtn", "device.canHeat", width:1, height:1, decoration: "flat") {
			state("true", action: "heatbtn", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_btn_icon.png")
			state "false", label: ''
		}
		standardTile("refresh", "device.refresh", width:2, height:2, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/refresh_icon.png"
		}
		valueTile("heatingSetpoint", "device.thermostatSetpoint", width: 1, height: 1) {
			state("heatingSetpoint", label:'${currentValue}', unit: "°C", foregroundColor: "#FFFFFF",
				backgroundColors: [ [value: 0, color: "#FFFFFF"], [value: 7, color: "#FF3300"], [value: 15, color: "#FF3300"] ])
			state("disabled", label: '', foregroundColor: "#FFFFFF", backgroundColor: "#FFFFFF")
		}
		standardTile("heatingSetpointUp", "device.thermostatSetpoint", width: 1, height: 1, canChangeIcon: true, decoration: "flat") {
			state "default", label: '', action:"heatingSetpointUp", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_up.png"
			state "", label: ''
		}
		standardTile("heatingSetpointDown", "device.thermostatSetpoint",  width: 1, height: 1, canChangeIcon: true, decoration: "flat") {
			state "default", label:'', action:"heatingSetpointDown", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_down.png"
			state "", label: ''
		}
		controlTile("heatSliderControl", "device.thermostatSetpoint", "slider", height: 1, width: 3, range: getRange(), inactiveLabel: false) {
			state "default", action:"setHeatingSetpoint", backgroundColor:"#FF3300"
			state "", label: ''
		}

		main("temp2")
		details( ["temperature", "thermostatMode",
				"heatingSetpointDown", "heatingSetpoint", "heatingSetpointUp",
				"heatSliderControl", "offBtn", "heatBtn", "refresh"] )
	}
	/*preferences {
		input "resetHistoryOnly", "bool", title: "Reset History Data", description: "", displayDuringSetup: false
		input "resetAllData", "bool", title: "Reset All Stored Event Data", description: "", displayDuringSetup: false
	}*/
}

def compileForC() {
	def retVal = true   // if using C mode, set this to true so that enums and colors are correct (due to ST issue of compile time evaluation)
	return retVal
}

def installed() {
    log.trace "Executing 'installed'"
    initialize()
    done()
}

def configure() {
    log.trace "Executing 'configure'"
    initialize()
    done()
}

private initialize() {
    log.trace "Executing 'initialize'"

    sendEvent(name:"temperature", value: 20.0, unit: "°C")
    sendEvent(name:"thermostatSetpoint", value: 20.0, unit: "°C")
  	sendEvent(name:"thermostatOperatingState", value: "heating")
    sendEvent(name:"thermostatMode", value: "heat")
}

def getTempColors() {
	def colorMap
//getTemperatureScale() == "C"   wantMetric()
	if(compileForC()) {
		colorMap = [
			// Celsius Color Range
			[value: 0, color: "#153591"],
			[value: 7, color: "#1e9cbb"],
			[value: 15, color: "#90d2a7"],
			[value: 23, color: "#44b621"],
			[value: 29, color: "#f1d801"],
			[value: 33, color: "#d04e00"],
			[value: 36, color: "#bc2323"]
			]
	} else {
		colorMap = [
			// Fahrenheit Color Range
			[value: 40, color: "#153591"],
			[value: 44, color: "#1e9cbb"],
			[value: 59, color: "#90d2a7"],
			[value: 74, color: "#44b621"],
			[value: 84, color: "#f1d801"],
			[value: 92, color: "#d04e00"],
			[value: 96, color: "#bc2323"]
		]
	}
}

def lowRange() { return compileForC() ? 9 : 50 }
def highRange() { return compileForC() ? 32 : 90 }
def getRange() { return "${lowRange()}..${highRange()}" }

def getTemperature() {
	return currentValue("temperature")
}

def setHeatingSetpoint(temp) {
    log.debug "setting temp to: $temp"
	sendEvent(name:"thermostatSetpoint", value: temp, unit: "°C")
}

def levelUp() {
	def hsp = device.currentValue("thermostatSetpoint")
    setHeatingSetpoint(hsp + 1.0)
}

def levelDown() {
    def hsp = device.currentValue("thermostatSetpoint")
    setHeatingSetpoint(hsp - 1.0)
}

private void done() {
    log.trace "---- DONE ----"
}

def ping() {
    log.trace "Executing ping"
    refresh()
}
def parse(data) {
    log.debug "parse data: $data"
}
def refresh() {
    log.trace "Executing refresh"
    sendEvent(name: "thermostatMode", value: getThermostatMode())
    sendEvent(name: "thermostatOperatingState", value: getOperatingState())
    sendEvent(name: "thermostatSetpoint", value: getThermostatSetpoint(), unit: "°C")
    sendEvent(name: "temperature", value: getTemperature(), unit: "°C")
    done()
}
def getThermostatMode() {
	return device.currentValue("thermostatMode")
}
def getOperatingState() {
	return device.currentValue("thermostatOperatingState")
}
def thermostatSetpoint() {
	return device.currentValue("thermostatSetpoint")
}
def getHeatingSetpoint() {
	return device.currentValue("heatingSetpoint")
}
def poll() {
}
def offbtn() {
	sendEvent(name: "thermostatMode", value: "off")
}
def heatbtn() {
	sendEvent(name: "thermostatMode", value: "heat")
}
def setThermostatMode(mode) {
    sendEvent(name: "thermostatMode", value: mode)
}
def levelUpDown() {
}
def log() {
}
def changeMode() {
	def val = device.currentValue("thermostatMode") == "off" ? "heat" : "off"
	sendEvent(name: "thermostatMode", value: val)
    return val
}
def setVirtualTemperature(temp) {
	sendEvent(name:"temperature", value: temp, unit: "°C")
}
def setHeatingStatus(bool) {
	sendEvent(name:"thermostatOperatingState", value: bool ? "heating" : "idle")
}
def setEmergencyMode(bool) {
    sendEvent(name: "thermostatOperatingState", value: bool ? "emergency" : "idle")
}