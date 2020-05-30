metadata {
	definition (name: "Virtual Thermostat Device",
    namespace: "piratemedia/smartthings",
    author: "Eliot S. + Steffen N.",
    mnmn: "SmartThings", 
    vid: "generic-thermostat-1",
    executeCommandsLocally: true,
    ocfDeviceType: "oic.d.thermostat") {
		capability "Temperature Measurement"
		capability "Thermostat"
		capability "Thermostat Mode"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Cooling Setpoint"
		capability "Thermostat Setpoint"
		capability "Thermostat Operating State"
		capability "Configuration"
		capability "Refresh"

		command "refresh"
		command "poll"
        
		command "offbtn"
		command "coolbtn"
		command "heatbtn"
		command "autobtn"
		command "levelUp"
		command "levelDown"
		command "heatingSetpointUp"
		command "heatingSetpointDown"
		command "coolingSetpointUp"
		command "coolingSetpointDown"
		command "setVirtualTemperature", ["number"]
		command "setHeatCoolDelta", ["number"]

		attribute "temperatureUnit", "string"
        attribute "heatCoolDelta", "number"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"temperature", type:"thermostat", width:6, height:4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'${currentValue}°', unit: unitString())
			}
            
			tileAttribute("device.thermostatSetpoint", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "levelUp")
				attributeState("VALUE_DOWN", action: "levelDown")
			}
            
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				// valid values are thermostatOperatingState — ["heating", "idle", "pending cool", "vent economizer", "cooling", "pending heat", "fan only"]
				// https://graph.api.smartthings.com/ide/doc/capabilities
                
				attributeState("off",		    backgroundColor: "#cccccc")
				attributeState("idle",		    backgroundColor: "#44B621")
				attributeState("heating",	    backgroundColor: "#f5dcec")
				attributeState("cooling",	    backgroundColor: "#00a0dc")
				attributeState("pending heat",	backgroundColor: "#ffd19c")
				attributeState("pending cool",	backgroundColor: "#85b3d6")
			}
            
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("cool", label:'Cool')
				attributeState("heat", label:'Heat')
				attributeState("auto", label:'Auto')
				attributeState("off", label:'Off')
			}
            
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
				attributeState("default", label:'${currentValue}')
			}

			tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
				attributeState("default", label:'${currentValue}')
			}
		}
        
		valueTile("temp2", "device.temperature", width: 2, height: 2, decoration: "flat") {
			state("default", label:'${currentValue}°', icon:"https://raw.githubusercontent.com/eliotstocker/SmartThings-VirtualThermostat-WithDTH/master/device.png",
					backgroundColors: getTempColors(), canChangeIcon: true)
		}
        
		standardTile("thermostatMode", "device.thermostatMode", width:6, height:2, decoration: "flat") {
			state("off", label: '${name}')
			state("heat", label: '${name}')
			state("cool", label: '${name}')
			state("auto", label: '${name}')
		}
        
		standardTile("offBtn", "device.thermostatMode", width:2, height:4, decoration: "flat") {
			state("", action: "offbtn", icon: "https://raw.githubusercontent.com/steffennissen/SmartThings-VirtualThermostat-WithDTH/master/images/unit_on.png", default: true)
			state("off", action: "offbtn", icon: "https://raw.githubusercontent.com/steffennissen/SmartThings-VirtualThermostat-WithDTH/master/images/unit_off.png")
		}
        
		standardTile("heatBtn", "device.thermostatMode", width:2, height:2, decoration: "flat") {
			state("", action: "heatbtn", icon: "https://raw.githubusercontent.com/steffennissen/SmartThings-VirtualThermostat-WithDTH/master/images/heat_off.png", default: true)
			state("heat", icon: "https://raw.githubusercontent.com/steffennissen/SmartThings-VirtualThermostat-WithDTH/master/images/heat_on.png")
		}

		standardTile("coolBtn", "device.thermostatMode", width:2, height:2, decoration: "flat") {
			state("", action: "coolbtn", icon: "https://raw.githubusercontent.com/steffennissen/SmartThings-VirtualThermostat-WithDTH/master/images/cool_off.png", default: true)
            state("cool", icon: "https://raw.githubusercontent.com/steffennissen/SmartThings-VirtualThermostat-WithDTH/master/images/cool_on.png")
		}

		standardTile("autoBtn", "device.thermostatMode", width:2, height:2, decoration: "flat") {
			state("", action: "autobtn", icon: "https://raw.githubusercontent.com/steffennissen/SmartThings-VirtualThermostat-WithDTH/master/images/auto_off.png", default: true)
            state("auto", icon: "https://raw.githubusercontent.com/steffennissen/SmartThings-VirtualThermostat-WithDTH/master/images/auto_on.png")
		}

		standardTile("refresh", "device.refresh", width:1, height:1, decoration: "flat") {
			state "Refresh", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
		valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2) {
			state("heatingSetpoint", action: "heatbtn", label:'${currentValue}', unit: unitString(), foregroundColor: "#FFFFFF", backgroundColor: "#f5dcec")
			state("disabled", label: '', foregroundColor: "#FFFFFF", backgroundColor: "#FFFFFF")
		}
        
		standardTile("heatingSetpointUp", "device.thermostatMode", width: 2, height: 1, canChangeIcon: true, decoration: "flat") {
			state "auto", label: '', action:"heatingSetpointUp", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_up.png"
			state "heat", label: '', action:"heatingSetpointUp", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_up.png"
			state "cool", label: ''
			state "off", label: ''
		}
        
		standardTile("heatingSetpointDown", "device.thermostatMode",  width: 2, height: 1, canChangeIcon: true, decoration: "flat") {
			state "auto", label:'', action:"heatingSetpointDown", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_down.png"
			state "heat", label:'', action:"heatingSetpointDown", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_down.png"
			state "cool", label: ''
			state "off", label: ''
		}
        
		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 2, width: 2, range: getRange()) {
			state "default", action:"setHeatingSetpoint", backgroundColor:"#e86d13"
		}

		valueTile("coolingSetpoint", "device.coolingSetpoint", width: 2, height: 2) {
			state("coolingSetpoint", action: "coolbtn", label:'${currentValue}', unit: unitString(), foregroundColor: "#FFFFFF", backgroundColor: "#00a0dc")
			state("disabled", label: '', foregroundColor: "#FFFFFF", backgroundColor: "#FFFFFF")
		}

		standardTile("coolingSetpointUp", "device.thermostatMode", width: 2, height: 1, canChangeIcon: true, decoration: "flat") {
			state "default", label: '', action:"coolingSetpointUp", icon:"https://raw.githubusercontent.com/racarmichael/SmartThings-VirtualThermostat-WithDTH/master/images/cool_arrow_up.png"
			state "heat", label: ''
			state "off", label: ''
		}

		standardTile("coolingSetpointDown", "device.thermostatMode",  width: 2, height: 1, canChangeIcon: true, decoration: "flat") {
			state "default", label:'', action:"coolingSetpointDown", icon:"https://raw.githubusercontent.com/racarmichael/SmartThings-VirtualThermostat-WithDTH/master/images/cool_arrow_down.png"
			state "heat", label: ''
			state "off", label: ''
		}

		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 4, range: getRange(), inactiveLabel: true) {
			state "default", action:"setCoolingSetpoint", backgroundColor:"#0022ff"
			state "", label: ''
		}

        //standardTile("upButtonControl", "device.thermostatSetpoint", width: 2, height: 2, inactiveLabel: false) {
		//	state "setpoint", action:"raiseSetpoint", icon:"st.thermostat.heat"
		//}  

		main("temp2")
        
		details( ["temperature", 
                  "coolingSetpointUp", "heatingSetpointUp", 
        		  "offBtn",
        		  "coolingSetpoint", 
                  "heatingSetpoint", 
          		  //"heatSliderControl", 
				  "coolingSetpointDown", "heatingSetpointDown", 
                  "thermostatMode",
                  "coolBtn","heatBtn", 
                  "autoBtn",
				  //"coolSliderControl"
                  "refresh"
                ] )
	}
}

def thermostatModes() { 
	return ['cool', 'heat', 'auto', 'off']
}

def shouldReportInCentigrade() {
	try {
    	def ts = getTemperatureScale();
    	return ts == "C"
    } catch (e) {
    	log.error e
    }
    return true;
}

def installed() {
    log.trace "Executing 'installed'"
    initialize()
}

def configure() {
    log.trace "Executing 'configure'"
    initialize()
}

private initialize() {
    log.trace "Executing 'initialize'"
    
	setCoolingSetpoint(defaultTemp()+2.0)
	setHeatingSetpoint(defaultTemp()-2.0)
	setThermostatSetpoint(defaultTemp())
    setVirtualTemperature(defaultTemp())
	setThermostatOperatingState("off")
    setThermostatMode("off")
    sendEvent(name:"supportedThermostatModes", value: thermostatModes(), displayed: false)

	state.tempScale = "C"
}

def getTempColors() {
	def colorMap
        //getTemperatureScale() == "C"   wantMetric()
	if(shouldReportInCentigrade()) {
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

def unitString() {  return shouldReportInCentigrade() ? "C": "F" }
def defaultTemp() { return shouldReportInCentigrade() ? 23 : 73 }
def lowRange() { return shouldReportInCentigrade() ? 7.0 : 45.0 }
def highRange() { return shouldReportInCentigrade() ? 45.0 : 113.0 }
def getRange() { return "${lowRange()}..${highRange()}" }

def getTemperature() {
	return device.currentValue("temperature")
}

def setThermostatSetpoint(temp) {
	def tsp = device.currentValue("thermostatSetpoint")

	log.debug "setThermostatSetpoint from " + tsp + " to " + temp
	if(tsp != temp) {
		sendEvent(name:"thermostatSetpoint", value: temp, unit: unitString())
	}
}

def setHeatingSetpoint(temp) {
	log.debug "setHeatingSetpoint: " + temp
    def hsp = device.currentValue("heatingSetpoint");

    if(hsp != temp) {
        sendEvent(name:"heatingSetpoint", value: temp, unit: unitString())
    }
}

def heatingSetpointUp() {
	def hsp = device.currentValue("heatingSetpoint")
	if(hsp + 0.1 > highRange()) return;
	setHeatingSetpoint(hsp + 0.1)
}

def heatingSetpointDown() {
	def hsp = device.currentValue("heatingSetpoint")
	if(hsp - 0.1 < lowRange()) return;
	setHeatingSetpoint(hsp - 0.1)
}

def setCoolingSetpoint(temp) {
	log.debug "setCoolingSetpoint: " + temp

	def csp = device.currentValue("coolingSetpoint");

	if(csp != temp) {
		sendEvent(name:"coolingSetpoint", value: temp, unit: unitString())
	}
}

def coolingSetpointUp() {
	def csp = device.currentValue("coolingSetpoint")
	if(csp + 0.1 > highRange()) return;
	setCoolingSetpoint(csp + 0.1)
}

def coolingSetpointDown() {
	def csp = device.currentValue("coolingSetpoint")
	if(csp - 0.1 < lowRange()) return;
	setCoolingSetpoint(csp - 0.1)
}

def levelUp() {
	def mode = device.currentValue("thermostatMode")
    switch (mode) {
    	case "heat":
        	heatingSetpointUp()
            break
        case "cool":
        	coolingSetpointUp()
            break
        default:
        	heatingSetpointUp()
            coolingSetpointUp()
    }
}

def levelDown() {
	def mode = device.currentValue("thermostatMode")
    switch(mode){
    	case "heat":
        	heatingSetpointDown()
            break
        case "cool":
        	coolingSetpointDown()
            break
        default:
        	heatingSetpointDown()
            coolingSetpointDown()
    }
}

def parse(data) {
    log.debug "parse data: $data"
}

def refresh() {
    log.trace "Executing refresh"
    configure()
}

def getThermostatMode() {
	return device.currentValue("thermostatMode")
}

def getOperatingState() {
	return device.currentValue("thermostatOperatingState")
}

def getThermostatSetpoint() {
	return device.currentValue("thermostatSetpoint")
}

def getHeatingSetpoint() {
	return device.currentValue("heatingSetpoint")
}

def getCoolingSetpoint() {
	return device.currentValue("coolingSetpoint")
}

def poll() {
}

def offbtn() {
	log.debug "offbtn, lastmode=" + state.lastMode
	if(device.currentValue("thermostatMode") != "off") {
		state.lastMode = device.currentValue("thermostatMode")
		setThermostatMode("off")
    } else {
    	if(state.lastMode) {
        	setThermostatMode(state.lastMode)	
        } else {
        	setThermostatMode("auto")
        }
    }
}

def coolbtn() {
	setThermostatMode("cool")
    setThermostatSetpoint(device.currentValue("coolingSetpoint")) 
}

def heatbtn() {
	setThermostatMode("heat")
    setThermostatSetpoint(device.currentValue("heatingSetpoint")) 
}

def autobtn() {
	log.debug "autobtn"
	setThermostatMode("auto")
}

def setThermostatMode(mode) {
	log.trace "setting thermostat mode $mode"
	if(device.currentValue("thermostatMode") != mode) {
    	sendEvent(name: "thermostatMode", value: mode)
    }
}

def setVirtualTemperature(temp) {
	sendEvent(name:"temperature", value: temp, unit: unitString(), displayed: true)
}

def setHeatCoolDelta(delta) {
	sendEvent(name:"heatCoolDelta", value: delta, unit: unitString(), displayed: true)
}

def setThermostatOperatingState(string) {
	if(device.currentValue("thermostatOperatingState") != string) {
		sendEvent(name:"thermostatOperatingState", value: string)
    }
}
