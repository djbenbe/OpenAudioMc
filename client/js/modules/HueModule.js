class HueModule {

    constructor(main) {
        this.hue = jsHue();
        this.bridges = [];
        this.isSsl = (document.location.href.startsWith("https://"));
        this.isLinked = false;
        this.currentBridge = null;
        this.currentUser = null;
        this.color = net.brehaut.Color;

        this.lights = [];

        const that = this;
        this.hue.discover().then(bridges => {
            bridges.forEach(bridge => {
                that.bridges.push(bridge);
                that.onDiscover();
            });
        }).catch(e => console.log('Error finding bridges', e));

        if (this.isSsl) {
            main.log("Failed to initiate PhilipsHue integration since this web page is served over ssl. The user will be promted to downgrade to HTTP when a user interaction is made that is related to Hue");
        }
    }

    onDiscover() {
        if (this.bridges.length !== 0) {
            //bridges found
            openAudioMc.log(this.bridges.length + " hue bridges found");
            document.getElementById("hue-bridge-menu-button").style.display = "";
        } else {
            openAudioMc.log("No hue bridges found");
            document.getElementById("bridge-list").innerHTML += "<tr>\n" +
                "                    <td>Christina Berglund</td>\n" +
                "                    <td>Sweden</td>\n" +
                "                </tr>";
        }
    }

    startSetup() {
        const that = this;
        this.bridges.forEach(bridge => {
            that.linkBridge(bridge.internalipaddress);
        })
    }

    onConnect() {
        document.getElementById("select-bridge").innerHTML = "<p>Preparing user..</p>";
        this.currentUser.getGroups().then(groups => {
            document.getElementById("select-bridge").innerHTML = "<p>You are now connected with your Philips Hue Lights! " +
                "Please select your group (you can always change this later) and click 'player' in the left bottem corner to return to the home menu.</p>" +
                "<select oninput='openAudioMc.getHueModule().selectGroup(this.value)' class=\"blue-select\" id='brige-list'><option value=\"\" disabled selected>Select a group</option></select> <br/> <p><i>(tip, click the test button to blink the ligs to check the setup)</i></p>";
            for (var key in groups) {
                document.getElementById("brige-list").innerHTML += "<option>" + groups[key].name + "</option>"
            }
        });
    }


    selectGroup(value) {
        const that = this;
        this.currentUser.getGroups().then(groups => {
            for (var key in groups) {
                if (groups[key].name == value) {
                    that.lights = [];
                    for (var id in groups[key].lights) {
                        id++;
                        that.lights.push(id);
                        that.setLight(id, "rgba(58,50,238,0.5)");
                    }
                }
            }
        });
    }

    colorToHueHsv(color) {
        const jqc = this.color(color).toHSV();
        return {
            "on": (((jqc.alpha * 2) * 127.5) !== 0),
            "hue": Math.floor(65535 * jqc.hue / 360),
            "sat": Math.floor(jqc.saturation * 255),
            "bri": Math.round((jqc.alpha * 2) * 127.5)
        }
    }

    setLight(id, rgb) {
        let query = [];
        if (typeof id == "object") {
            id.forEach(target => {
                query.push(this.lights[target-1]);
            });
        } else {
            query.push(this.lights[id-1]);
        }
        query.forEach(light => {
            this.currentUser.setLightState(light, this.colorToHueHsv(rgb)).then(data => {});
        });
    }

    linkBridge(bridgeIp) {
        document.getElementById("select-bridge").innerHTML = "<p>Preparing setup..</p>";
        this.currentBridge = this.hue.bridge(bridgeIp);
        if (this.currentBridge == null) {
            openAudioMc.log("Invalid bridge IP");
            return;
        }

        const that = this;
        let linkAttempts = 0;
        let linkTask = -1;

        linkTask = setInterval(() => {
            function cancel() {
                clearInterval(linkTask);
            }

            linkAttempts++;
            if (linkAttempts > 60) {
                cancel();
                document.getElementById("select-bridge").innerHTML = "<p>Could not connect to your hue bridge after 60 seconds, did you press the link button?</p><span class=\"button\" onclick=\"openAudioMc.getHueModule().startSetup();\" style=\"color: white;\">Click here to try again</span>";
                openAudioMc.log("Failed to authenticate with bridge in 60 seconds.");
                return;
            }

            document.getElementById("select-bridge").innerHTML = "<p>Press the link button on your hue bridge within " + (60 - linkAttempts) + " seconds to connect.</p>";

            that.currentBridge.createUser("OpenAudioMc#WebClient")
                .then(data => {
                    if (data[0].error != null) {
                        if (data[0].error.type === 101) {
                            //link button not pressed
                        } else {
                            //unexpected error
                            cancel();
                            openAudioMc.log("Unexpected error while connecting: " + data[0].error.type);
                        }
                    } else if (data[0].success != null) {
                        that.currentUser = that.currentBridge.user(data[0].success.username);
                        openAudioMc.log("Linked with hue bridge after " + linkAttempts + " attempt(s).");
                        that.isLinked = true;
                        that.onConnect();
                        cancel();
                    }
                });
        }, 1000);
    }

}