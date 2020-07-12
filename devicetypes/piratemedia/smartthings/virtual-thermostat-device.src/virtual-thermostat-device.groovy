metadata {
	definition (name: "Virtual Thermostat Device",
    namespace: "piratemedia/smartthings",
    author: "Eliot S. + Steffen N.",
    mnmn: "SmartThings", 
    vid: "generic-thermostat-1",
    executeCommandsLocally: true,
    ocfDeviceType: "oic.d.thermostat") {
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Thermostat"
		capability "Thermostat Mode"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Cooling Setpoint"
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
		command "smartCoolDown"
		command "smartHeatUp"
		command "heatingSetpointUp"
		command "heatingSetpointDown"
		command "coolingSetpointUp"
		command "coolingSetpointDown"
		command "setVirtualTemperature", ["number"]
		command "setVirtualHumidity", ["number"]
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
            
			tileAttribute("device.thermostatOperatingState", key: "VALUE_CONTROL") {
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
            
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
     		   attributeState("humidity", label:'${currentValue}%', unit:"%", defaultState: true)
    		}
		}
        
		valueTile("tempmain", "device.temperature", width: 2, height: 2, decoration: "flat") {
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
        
   		standardTile("smartCool", "device.thermostatMode", width:3, height:2, decoration: "flat") {
			state "default", label:'Smart Cool Down', action:"smartCoolDown", icon:"https://raw.githubusercontent.com/racarmichael/SmartThings-VirtualThermostat-WithDTH/master/images/cool_arrow_down.png"
		}

   		standardTile("smartHeat", "device.thermostatMode", width:3, height:2, decoration: "flat") {
			state "default", label:'Smart Heat Up', action:"smartHeatUp", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_up.png"
		}

		main("tempmain")
        
		details( ["temperature", 
                  "coolingSetpointUp", "heatingSetpointUp", 
        		  "offBtn",
        		  "coolingSetpoint", 
                  "heatingSetpoint", 
          		  //"heatSliderControl", 
				  "coolingSetpointDown", "heatingSetpointDown", 
                  //"thermostatMode",
                  "smartCool", "smartHeat",
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
    
    setHeatCoolDelta(0)
    setHeatDiff(0)
    setCoolDiff(0)
	sendCoolingSetpoint(defaultTemp()+2.0)
	sendHeatingSetpoint(defaultTemp()-2.0)
	setThermostatOperatingState("off")
    setThermostatMode("off")
    setVirtualTemperature(defaultTemp())
    setVirtualHumidity(50)
    sendEvent(name:"supportedThermostatModes", value: thermostatModes(), displayed: false)

	state.tempScale = "C"
}

def getTempColors() {
	if(shouldReportInCentigrade()) {
		return [
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
		return [
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

def roundTemp(temp) {
	return Math.round(temp * 100) / 100;
}

def sendCoolingSetpoint(temp) {
	def csp = device.currentValue("coolingSetpoint")
    if(temp != csp) {
    	temp = roundTemp(temp);
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
    	temp = roundTemp(temp);
        log.debug "sendHeatingSetpoint from " + hsp + " to " + temp
		sendEvent(name:"heatingSetpoint", value: temp, unit: unitString())
        if(device.currentValue("thermostatOperatingState") == "heating") {
        	setAdjustedHeatingPoint(temp)
        } else {
        	setAdjustedHeatingPoint(temp - device.currentValue("heatDiff"))
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
        def targetCool = temp + device.currentValue('heatCoolDelta')
        if(device.currentValue("coolingSetpoint") < targetCool) {
            sendCoolingSetpoint(targetCool)
        }

		sendHeatingSetpoint(temp)
    }
}

def setCoolingSetpoint(temp) {
	def csp = device.currentValue("coolingSetpoint");
	log.debug "setCoolingSetpoint: from " + csp + " to " + temp + " within range " + lowRange() + " to " + highRange()
    temp = inRange(temp, lowRange(), highRange())
    
	if(csp != temp) {
        def targetHeat = temp - device.currentValue('heatCoolDelta')
        if(device.currentValue("heatingSetpoint") > targetHeat) {
            sendHeatingSetpoint(targetHeat)
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

def levelChange(diff) {
	def mode = device.currentValue("thermostatMode")
    switch (mode) {
    	case "heat":
			setHeatingSetpoint(device.currentValue("heatingSetpoint") + diff)
            break
        case "cool":
        	setCoolingSetpoint(device.currentValue("coolingSetpoint") + diff)
            break
        default:
        	setHeatingSetpoint(device.currentValue("heatingSetpoint") + diff)
            setCoolingSetpoint(device.currentValue("coolingSetpoint") + diff)
    }
}

def levelUp() {
	levelChange(0.5)
}

def levelDown() {
	levelChange(-0.5)
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

def getHeatingSetpoint() {
	return device.currentValue("heatingSetpoint")
}

def getCoolingSetpoint() {
	return device.currentValue("coolingSetpoint")
}

def getHeatDiff() {
	return device.currentValue("heatDiff")
}

def getCoolDiff() {
	return device.currentValue("coolDiff")
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
}

def heatbtn() {
	setThermostatMode("heat")
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

def setVirtualHumidity(humidity) {
	sendEvent(name:"humidity", value: humidity, unit: unitString(), displayed: true)
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

//The idea behind the smart cool and heat is to change the setpoint, so that if it's not cooling/heating now it will start so immediately
// and likewise if it's already cooling/heating it will stop. The idea is to change the setpoint enough for this change to happen right now
// and that it will stay that way at least for a little while. Without moving the setpoint too much.
// Good example of use is of the thermostat is set to cool and it starts cooling, but you don't think it hot enough that it should start right now,
// so you would click smartHeatUp. Alternatively if your thermostat is set to cool, but it's not cooling right now and you would want it to, you would
// press smartCoolDown
def smartCoolDown(){
	log.debug "smartCoolDown => thermostatMode: ${getThermostatMode()}, operatingState: ${getOperatingState()}"
    def diff = getCoolDiff().max(0.3) // if diff is too small, this is not doing much

	if(getOperatingState() == "heating") {
        def setPointToStopHeating = getTemperature() - 0.1;
	    def targetFromSetPoint = getHeatingSetpoint() - (diff*2)
        def target = setPointToStopCooling.min(targetFromSetPoint)
    	def targetDiff = target - getHeatingSetpoint();
	    log.debug "smartCoolDown while heating => temp: ${getTemperature()}, heatSetPoint: ${getHeatingSetpoint()}, diff: ${diff}, targetFromSetPoint: ${targetFromSetPoint}, setPointToStopHeating: ${setPointToStopHeating}, target: ${target}, targetDiff: ${targetDiff}"
    	levelChange(targetDiff)
        return;
	}
    
    if(getThermostatMode() == "heat") {
    	log.debug "smartCoolDown while in heat mode, but not actually heating, simple move the setpoint down"
        levelChange(-diff)
        return
    }

    // Change the thermostat mode, so it's either cool or auto
	if(getThermostatMode() == "off") {
    	if(state.lastMode && state.lastMode != "heat") {
       		setThermostatMode(state.lastMode)
       	} else {
	       	setThermostatMode("cool")
        }
	}
        
	if(getOperatingState() == "cooling" || getTemperature() > getCoolingSetpoint()){
		log.debug "smartCoolDown already in a state where it should be cooling, lower the setpoint by ${diff}"	
    	levelChange(-diff)
        return
    }    

    def setPointToStartCooling = getTemperature() - diff - 0.1; 
    def targetFromSetPoint = getCoolingSetpoint() - (diff*2)    
    def target = targetFromSetPoint.min(setPointToStartCooling)
    def targetDiff = target - getCoolingSetpoint()
    log.debug "smartCoolDown => temp: ${getTemperature()}, coolSetPoint: ${getCoolingSetpoint()}, diff: ${diff}, targetFromSetPoint: ${targetFromSetPoint}, setPointToStartCooling: ${setPointToStartCooling}, target: ${target}, targetDiff: ${targetDiff}"
    levelChange(targetDiff)
}

def smartHeatUp(){
	log.debug "smartHeatUp => thermostatMode: ${getThermostatMode()}, operatingState: ${getOperatingState()}"
    def diff = getHeatDiff().max(0.3) // if diff is too small, this is not doing much

	if(getOperatingState() == "cooling") {
        def setPointToStopCooling = getTemperature() + 0.1;
	    def targetFromSetPoint = getCoolingSetpoint() + (diff*2)
        def target = setPointToStopCooling.max(targetFromSetPoint)
    	def targetDiff = target - getCoolingSetpoint();
	    log.debug "smartHeatUp while cooling => temp: ${getTemperature()}, coolSetPoint: ${getCoolingSetpoint()}, diff: ${diff}, targetFromSetPoint: ${targetFromSetPoint}, setPointToStopCooling: ${setPointToStopCooling}, target: ${target}, targetDiff: ${targetDiff}"
    	levelChange(targetDiff)
        return;
	}
    
    if(getThermostatMode() == "cool") {
    	log.debug "smartHeatUp while in cool mode, but not actually cooling, simple move the setpoint up"
        levelChange(diff)
        return
    }

    // Change the thermostat mode, so it's either heat or auto
	if(getThermostatMode() == "off") {
    	if(state.lastMode && state.lastMode != "cool") {
       		setThermostatMode(state.lastMode)
       	} else {
	       	setThermostatMode("heat")
        }
	}
        
	if(getOperatingState() == "heating" || getTemperature() < getHeatingSetpoint()){
		log.debug "smartHeatUp already in a state where it should be heating, increase the setpoint by ${diff}"	
    	levelChange(diff)
        return
    }    

    def setPointToStartHeating = getTemperature() + diff + 0.1; 
    def targetFromSetPoint = getHeatingSetpoint() + (diff*2)    
    def target = targetFromSetPoint.max(setPointToStartHeating)
    def targetDiff = target - getHeatingSetpoint();
    log.debug "smartHeatUp => temp: ${getTemperature()}, heatSetPoint: ${getHeatingSetpoint()}, diff: ${diff}, targetFromSetPoint: ${targetFromSetPoint}, setPointToStartHeating: ${setPointToStartHeating}, target: ${target}, targetDiff: ${targetDiff}"
    levelChange(targetDiff)
}