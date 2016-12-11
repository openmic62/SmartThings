/**
 *  My First SmartApp
 *
 *  Copyright 2016 Mike Rocha
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "A First SmartApp",
    namespace: "openmic62",
    author: "Mike Rocha",
    description: 'This SmartApp originated from the "Writing Your First SmartApp" tutorial found at http://docs.smartthings.com/en/latest/getting-started/first-smartapp.html.',
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page(name: "pageOne", title: "Setup the motion stuff.", nextPage: "pageTwo", uninstall: true) {
        section("Turn on when motion detected:") {
            input "themotion", "capability.motionSensor", required: true, title: "Where?"
        }
        section("Turn of when there's been no movement for:") {
            input "minutes", "number", required: true, title: "Minutes?"
        }
        section("Turn on/off this switch:") {
            input "theswitch", "capability.switch", required: true
        }
    }
    page(name: "pageTwo", title: "Mess with prefs.", install: true) {
    	section("Name this app") {
            label title: "Assign a name", required: false
            mode title: "Set for specific mode(s)", required: false
		}
        section("Demo paragraphs") {
            paragraph "This is how you can make a paragraph element"
            paragraph image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
                      title: "Second paragraph",
                      required: true,
                      "This is a seconde paragraph in which I explain how to do Qigong ..."
        }
        section("Demo icons") {
             icon(title: "required is true", required: true)
        }
        section("Demo hrefs") {
             href(name: "hrefNotRequired",
                 title: "SmartThings",
                 required: false,
                 style: "external",
                 url: "http://smartthings.com/",
                 description: "tap to view SmartThings website in mobile browser")
            href(name: "hrefWithImage", title: "This element has an image and a long title.",
                 description: "tap to view SmartThings website inside SmartThings app",
                 required: false,
                 image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
                 url: "http://smartthings.com/")
       }
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(themotion, "motion.active", motionDetectedHandler)
    subscribe(themotion, "motion.inactive", motionStoppedHandler)
}

def motionDetectedHandler(evt) {
	log.debug("motionDetectedHandler called: $evt")
    theswitch.on()
}

def motionStoppedHandler(evt) {
	log.debug("motionStoppedHandler called: $evt")
    runIn(60 * minutes, checkMotion)
}

def checkMotion() {
    log.debug "In checkMotion scheduled method"

    // get the current state object for the motion sensor
    def motionState = themotion.currentState("motion")

    if (motionState.value == "inactive") {
            // get the time elapsed between now and when the motion reported inactive
        def elapsed = now() - motionState.date.time

        // elapsed time is in milliseconds, so the threshold must be converted to milliseconds too
        def threshold = 1000 * 60 * minutes

            if (elapsed >= threshold) {
            log.debug "Motion has stayed inactive long enough since last check ($elapsed ms):  turning switch off"
            theswitch.off()
            } else {
            log.debug "Motion has not stayed inactive long enough since last check ($elapsed ms):  doing nothing"
        }
    } else {
            // Motion active; just log it and do nothing
            log.debug "Motion is active, do nothing and wait for inactive"
    }
}