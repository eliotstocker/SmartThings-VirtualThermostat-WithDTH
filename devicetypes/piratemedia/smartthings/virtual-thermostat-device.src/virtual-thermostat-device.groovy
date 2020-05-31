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
		command "setHeatDiff", ["number"]
		command "setCoolDiff", ["number"]

		attribute "temperatureUnit", "string"
        attribute "heatCoolDelta", "number"
        attribute "heatDiff", "number"
        attribute "coolDiff", "number"
        attribute "adjustedHeatingPoint", "number"
        attribute "adjustedCoolingPoint", "number"
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
				attributeState("VALUE_UP", label: '', action: "levelUp")
				attributeState("VALUE_DOWN", label: '', action: "levelDown")
			}
            
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				// valid values are thermostatOperatingState — ["heating", "idle", "pending cool", "vent economizer", "cooling", "pending heat", "fan only"]
				// https://graph.api.smartthings.com/ide/doc/capabilities
                
				attributeState("off",		    backgroundColor: "#cccccc")
				attributeState("idle",		    backgroundColor: "#44B621")
				attributeState("heating",	    backgroundColor: "#e86d13")
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
            
			tileAttribute("device.adjustedHeatingPoint", key: "HEATING_SETPOINT") {
				attributeState("default", label:'${currentValue}')
			}

			tileAttribute("device.adjustedCoolingPoint", key: "COOLING_SETPOINT") {
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
			state("heatingSetpoint", action: "heatbtn", label:'${currentValue}', unit: unitString(), foregroundColor: "#FFFFFF", backgroundColor: "#e86d13")
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
                  //"refresh",
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

def sendThermostatSetpoint(temp) {
	def tsp = device.currentValue("thermostatSetpoint")
    if(temp != tsp) {
    	log.debug "sendThermostatSetpoint from " + tsp + " to " + temp
		sendEvent(name:"thermostatSetpoint", value: temp, unit: unitString())
    }
}

def sendCoolingSetpoint(temp) {
	def csp = device.currentValue("coolingSetpoint")
    if(temp != csp) {
    	log.debug "sendCoolingSetpoint from " + csp + " to " + temp
		sendEvent(name:"coolingSetpoint", value: temp, unit: unitString())
        if(device.currentValue("thermostatOperatingState") == "cooling") {
        	setAdjustedCoolingPoint(temp)
        } else {
        	setAdjustedCoolingPoint(temp + device.currentValue("coolDiff"))
        }
    }
}

def sendHeatingSetpoint(temp) {
	def hsp = device.currentValue("heatingSetpoint")
    if(temp != hsp) {
        log.debug "sendHeatingSetpoint from " + hsp + " to " + temp
		sendEvent(name:"heatingSetpoint", value: temp, unit: unitString())
        if(device.currentValue("thermostatOperatingState") == "heating") {
        	setAdjustedHeatingPoint(temp)
        } else {
        	setAdjustedHeatingPoint(temp - device.currentValue("heatDiff"))
        }
    }
}

def setThermostatSetpoint(temp) {
	def tsp = device.currentValue("thermostatSetpoint")
	log.debug "setThermostatSetpoint from " + tsp + " to " + temp
	if(tsp != temp) {
        def csp = device.currentValue("coolingSetpoint")
        def hsp = device.currentValue("heatingSetpoint")
        def mode = device.currentValue('thermostatMode')
        
        if(mode == "heat" && hsp != temp) {
            sendHeatingSetpoint(temp)
        } else if(mode == "cool" && csp != temp) {
            sendCoolingSetpoint(temp)
        }

        if(csp < temp) {
            sendCoolingSetpoint(temp)
        } else if(heat > temp) {
            setHeatingSetpoint(temp)
        }
		sendThermostatSetpoint(temp)
	}
}

def autoThermostatSetPoint() {
    if(device.currentValue('thermostatMode') == "auto") {
	    def temp = device.currentValue("thermostatSetpoint")
    	def cool = device.currentValue("coolingSetpoint")
    	def heat = device.currentValue("heatingSetpoint")

		def newTemp = (cool + heat) / 2.0
        if(newTemp != temp) {
        	sendThermostatSetpoint(newTemp.setScale(1, BigDecimal.ROUND_HALF_EVEN))
        }
    }
}

def inRange(val, low, high) {
    if(val < low)
    	return low
    else if(val > high)
    	return high
    return val
}

def setHeatingSetpoint(temp) {
    def hsp = device.currentValue("heatingSetpoint");
	log.debug "setHeatingSetpoint from " + hsp + " to " + temp + " within range " + lowRange() + " to " + highRange()
    temp = inRange(temp, lowRange(), highRange())
    
    if(hsp != temp) {
        if(device.currentValue('thermostatMode') == "heat") {
            sendThermostatSetpoint(temp)
        } else if(device.currentValue('thermostatMode') == "auto") {
            autoThermostatSetPoint()
        }

        def targetCool = temp + device.currentValue('heatCoolDelta')
        if(device.currentValue("coolingSetpoint") < targetCool) {
            sendCoolingSetpoint(targetCool)
        }

        if(device.currentValue("thermostatSetpoint") < temp) {
            sendThermostatSetpoint(temp)
        }

		sendHeatingSetpoint(temp)
    }
}

def setCoolingSetpoint(temp) {
	def csp = device.currentValue("coolingSetpoint");
	log.debug "setCoolingSetpoint: from " + csp + " to " + temp + " within range " + lowRange() + " to " + highRange()
    temp = inRange(temp, lowRange(), highRange())
    
	if(csp != temp) {
        if(device.currentValue('thermostatMode') == "cool") {
            sendThermostatSetpoint(temp)
        } else if(device.currentValue('thermostatMode') == "auto") {
            autoThermostatSetPoint()
        }

        def targetHeat = temp - device.currentValue('heatCoolDelta')
        if(device.currentValue("heatingSetpoint") > targetHeat) {
            sendHeatingSetpoint(targetHeat)
        }

        if(device.currentValue("thermostatSetpoint") > temp) {
            sendThermostatSetpoint(temp)
        }

		sendCoolingSetpoint(temp)
	}
}

def heatingSetpointUp() {
	setHeatingSetpoint(device.currentValue("heatingSetpoint") + 0.1)
}

def heatingSetpointDown() {
	setHeatingSetpoint(device.currentValue("heatingSetpoint") - 0.1)
}

def coolingSetpointUp() {
	setCoolingSetpoint(device.currentValue("coolingSetpoint") + 0.1)
}

def coolingSetpointDown() {
	setCoolingSetpoint(device.currentValue("coolingSetpoint") - 0.1)
}

def levelUp() {
	def mode = device.currentValue("thermostatMode")
    switch (mode) {
    	case "heat":
			setHeatingSetpoint(device.currentValue("heatingSetpoint") + 0.5)
            break
        case "cool":
        	setCoolingSetpoint(device.currentValue("coolingSetpoint") + 0.5)
            break
        default:
        	setHeatingSetpoint(device.currentValue("heatingSetpoint") + 0.5)
            setCoolingSetpoint(device.currentValue("coolingSetpoint") + 0.5)
    }
}

def levelDown() {
	def mode = device.currentValue("thermostatMode")
    switch(mode){
    	case "heat":
        	setHeatingSetpoint(device.currentValue("heatingSetpoint") - 0.5)
            break
        case "cool":
        	setCoolingSetpoint(device.currentValue("coolingSetpoint") - 0.5)
            break
        default:
        	setHeatingSetpoint(device.currentValue("heatingSetpoint") - 0.5)
            setCoolingSetpoint(device.currentValue("coolingSetpoint") - 0.5)
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
    	autoThermostatSetPoint()
    	sendEvent(name: "thermostatMode", value: mode)
    }
}

def setVirtualTemperature(temp) {
	sendEvent(name:"temperature", value: temp, unit: unitString(), displayed: true)
}

def setHeatCoolDelta(delta) {
	sendEvent(name:"heatCoolDelta", value: delta, unit: unitString(), displayed: true)
}

def setHeatDiff(diff) {
	sendEvent(name:"heatDiff", value: diff, unit: unitString(), displayed: true)
}

def setCoolDiff(diff) {
	sendEvent(name:"coolDiff", value: diff, unit: unitString(), displayed: true)
}

def setAdjustedHeatingPoint(point) {
	if(device.currentValue("adjustedHeatingPoint") != point) {
		sendEvent(name:"adjustedHeatingPoint", value: point, unit: unitString(), displayed: true)
    }
}

def setAdjustedCoolingPoint(point) {
	if(device.currentValue("adjustedCoolingPoint") != point) {
		sendEvent(name:"adjustedCoolingPoint", value: point, unit: unitString(), displayed: true)
    }
}

def setThermostatOperatingState(operatingState) {
	if(device.currentValue("thermostatOperatingState") != operatingState) {
		sendEvent(name:"thermostatOperatingState", value: operatingState)
        if(operatingState == "heating") {
        	setAdjustedHeatingPoint(device.currentValue("heatingSetpoint"))
        } else {
        	setAdjustedHeatingPoint(device.currentValue("heatingSetpoint") - device.currentValue("heatDiff"))
        }
        if(operatingState == "cooling") {
        	setAdjustedCoolingPoint(device.currentValue("coolingSetpoint"))
        } else {
        	setAdjustedCoolingPoint(device.currentValue("coolingSetpoint") + device.currentValue("coolDiff"))
        }
    }
}
