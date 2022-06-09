$(document).ready(function () {
    
});

function addDevice(obj) {
    
    //TODO dodać inne rodzaje ikonek w znależności od rodzaju urzadzenia
    var deviceTMP = $("<div class=\"device\"></div >");
    deviceTMP.attr("id", obj.id);
    var deviceIcon = $("<div class=\"deviceIcon\" ></div >");
    if (obj.typ==="LIGHT") {
        deviceIcon.append($("<i class=\"icon-lightbulb\"></i>"));
    }
    else if (obj.typ ==="BLIND") {
        deviceIcon.append($("<i class=\"icon-server\"></i>"));
    }
    $(deviceTMP).append(deviceIcon);

    var deviceDescribe = $("<div class=\"deviceDescribe\"></div>");
    deviceDescribe.append($("<span class=\"deviceDescribeText\">"+obj.name+"</span>"));
    deviceDescribe.append($("<br>"));
    if (obj.typ === "LIGHT") {
        deviceDescribe.append($("<span class=\"deviceDescribeState\">"+(obj.stan ? "ON" : "OFF" )+"</span>"));
        deviceTMP.attr("state", obj.stan ? "on" : "off");
    }
    else if (obj.typ === "BLIND") {
        if (obj.stan === "DOWN") {
            deviceDescribe.append($("<span class=\"deviceDescribeState\">OPUSZCZONE</span>"));
            deviceTMP.attr("state", "off");
        }
        else{
            deviceDescribe.append($("<span class=\"deviceDescribeState\">PODNIESIONE</span>"));
            deviceTMP.attr("state", "on");
        }
        
    }
    $(deviceTMP).append(deviceDescribe);


    var deviceState = "<div class=\"deviceState\"></div>";
    var deviceStateIcon = $(deviceState);
    if (obj.stan) {
        deviceStateIcon.append($("<i class=\"icon-toggle-on\"></i>"));
    }
    else{
        deviceStateIcon.append($("<i class=\"icon-toggle-off\"></i>"));
    }
    $(deviceTMP).append(deviceStateIcon);

    var device = deviceTMP;

    showDeviceState(device, obj.stan, obj.typ);
    
    device.click(function () {
        clickDevice(this,obj.id, obj.room, obj.typ);
    })
    if (obj.typ === "BLIND") {
        device.dblclick(function() {
            //TODO STOP BLIND
        })
    }

    return device;
}
function clickDevice(mee, id, room, typ) {
    var me = $(mee);
    if (me.attr("state") == "on") {
        console.debug("me on");
        
    }
    else{
        console.debug("me off");
    }

    if (typ === "BLIND") {
        $.ajax({
            url: "/admin/api/changeBlindStateByRoomID",
            type: 'post',
            data: {
                "roomID": room,
                "idUrzadzenia": id,
                "pozycja": me.attr("state") == "on" ? false : true
            },
            success: function (response) {
                console.log(response);
                if (response.error == null) {
                    showDeviceState(me, me.attr("state") == "on" ? false : true, "BLIND")
                } else {
                    $("#err-msg").html("Error: " + response.error);
                    $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
                }
            }
        });
    }
    else if(typ==="LIGHT"){
        $.ajax({
            url: "/admin/api/changeLightStateByRoomID",
            type: 'post',
            data: {
                "roomID": room,
                "idUrzadzenia": id,
                "stan": me.attr("state") == "on"? false : true
            },
            success: function (response) {
                console.log(response);
                if (response.error == null) {
                    showDeviceState(me, me.attr("state") == "on" ? false : true, "LIGHT")
                } else {
                    $("#err-msg").html("Error: " +  response.error);
                    $("#err-msg").show( "bounce", {}, 1000, function(){hideAfter(this, 10000)} );
                }
            }
        });
    }
    else{
        alert("Nie zaprogramowane!");
    }
}

function showDeviceState(device, stan, deviceType){
    if (deviceType === "LIGHT") {
        if (stan) {
            device.removeClass("deviceOFF");
            device.children().first().removeClass("deviceIconOFF");
            device.children().last().children().first().removeClass("icon-toggle-off");
            device.children().last().children().first().addClass("icon-toggle-on");
            device.children().eq(1).children().eq(2).text("ON")
            device.attr("state", "on");
        }
        else{
            device.addClass("deviceOFF");
            device.children().first().addClass("deviceIconOFF");
            device.children().last().children().first().removeClass("icon-toggle-on");
            device.children().last().children().first().addClass("icon-toggle-off");
            device.children().eq(1).children().eq(2).text("OFF")
            device.attr("state", "off");   
        }
    } else {
        if (stan) {
            device.removeClass("deviceOFF");
            device.children().first().removeClass("deviceIconOFF");
            device.children().last().children().first().removeClass("icon-toggle-off");
            device.children().last().children().first().addClass("icon-toggle-on");
            device.children().eq(1).children().eq(2).text("PODNIESIONA")
            device.attr("state", "on");
        }
        else {
            device.addClass("deviceOFF");
            device.children().first().addClass("deviceIconOFF");
            device.children().last().children().first().removeClass("icon-toggle-on");
            device.children().last().children().first().addClass("icon-toggle-off");
            device.children().eq(1).children().eq(2).text("OPUSZCZONA")
            device.attr("state", "off");
        }
    }
}