/*
    Sofabaton X1S
	Copyright 2025 Hubitat Inc. All Rights Reserved

	2025-03-22 maxwell
		-initial publication in github repo

    2025-03-28 Gassgs
        -Made Button Count a preferences & changed parse to just send the number from the body as the button press
	2025-03-30 SViel
		-Added recognition of on/off button
		-Added the option to store the name of an activity if included in the button press

	*simple example driver for Sofabaton X1S remote, allows mapping X1S remote buttons to Hubitat button events

	*driver configuration
	-set a static DHCP reservation for the XS1 hub
	-use that reserved IP in this drivers preference setting

	
	*mobile app configuration on the X1S side for this specific driver instance:
	-click add devices in devices tab, select Wi-Fi
	-click link at bottom "Create a virtual device for IP control"
	-enter   http://my hubs IP address:39501/ 
	-set PUT as the request method, for the Content Type leave blank, for Headers leave blank, for the Body, enter just the button number.


*/

metadata {
    definition (name: "Sofabaton X1S", namespace: "hubitat", author: "Mike Maxwell") {
        capability "Actuator"
        capability "PushableButton"
        capability "Switch"
        preferences {
            input name:"ip", type:"text", title: "X1S IP Address"
            input name:"buttonCount", type: "number",title:"Button Count", description: "Set button count to suit your needs", defaultValue: 10
            input name:"logEnable", type: "bool", title: "Enable debug logging", defaultValue: false
            input name:"txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: true
        }
        attribute "Activity","String"
    }
}

void logsOff(){
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

void updated(){
    log.info "updated..."
    log.warn "debug logging is: ${logEnable == true}"
    log.warn "description logging is: ${txtEnable == true}"
    if (logEnable) runIn(1800,logsOff)
    if (ip) {
        device.deviceNetworkId = ipToHex(IP)
        sendEvent(name:"numberOfButtons",value:settings.buttonCount)
    }
}

void parse(String description) {
    msg = parseLanMessage(description)
    if (logEnable) log.debug "String is: $msg"
    if (logEnable) log.debug "String Header is: $msg.header"
    if (logEnable) log.debug "String Body is: $msg.body"
    if (txtEnable) log.info "$device.label Button $msg.body Pushed"
    def data = msg.body
    
    //Recognize if the button name was "On" or "Off" and set the swtich instead of the button.
    if (data.equalsIgnoreCase("on")){
        sendEvent(name:"switch", value:"on")
    } else if (data.equalsIgnoreCase("off")){
        sendEvent(name:"switch", value:"off")
    } else {
	    sendEvent(name:"pushed", value:data,isStateChange: true)
        sendEvent(name: "Activity", value: "$data", isStateChange: true)
	}
}

void push(data) {
    if (txtEnable) log.info "$device.label Button $data Pushed"
    sendEvent(name:"pushed", value:data,isStateChange: true)
}

String ipToHex(IP) {
    List<String> quad = ip.split(/\./)
    String hexIP = ""
    quad.each {
        hexIP+= Integer.toHexString(it.toInteger()).padLeft(2,"0").toUpperCase()
    }
    return hexIP
}
