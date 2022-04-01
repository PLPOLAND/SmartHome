$(document).ready(function () {
    // $('.device').hover(function() {
    //     $(this).css('background-color', 'var(--primaryLightColor)');
    //     $(this).children().first().css('background-color', 'var(--primaryLightColor)');
    //     },function() {
    //         $(this).css('background-color', 'var(--primaryColor)');
    //     $(this).children().first().css('background-color', 'var(--primaryLightColor)');
    //     })
    $('.device').hover(function() {
        $(this).children().css('background-color', '#FFFFFF22');
        $(this).children().first().css('background-color', '#FFFFFF44');
        },function() {
        $(this).children().css('background-color', '#FFFFFF00');
        $(this).children().first().css('background-color', '#FFFFFF22');
        });
});

function addDevice(obj) {
    
    //TODO dodać inne rodzaje ikonek w znależności od rodzaju urzadzenia
    var deviceTMP = $("<div class=\"device\"></div >");
    var deviceIcon = "<div class=\"deviceIcon\" ></div >";
    var tmp = $(deviceIcon);
    tmp.append($("<i class=\"icon-lightbulb\"></i>"));
    $(deviceTMP).append(tmp);

    var deviceDescribe = "<div class=\"deviceDescribe\"></div>";
    var tmp = $(deviceDescribe);
    tmp.append($("<span class=\"deviceDescribeText\">"+obj.name+"</span>"));
    tmp.append($("<br>"));
    tmp.append($("<span class=\"deviceDescribeState\">"+(obj.stan ? "ON" : "OFF" )+"</span>"));
    $(deviceTMP).append(tmp);


    var deviceState = "<div class=\"deviceState\"></div>";
    var tmp = $(deviceState);
    if (obj.stan) {
        tmp.append($("<i class=\"icon-toggle-on\"></i>"));
    }
    else{
        tmp.append($("<i class=\"icon-toggle-off\"></i>"));
    }
    $(deviceTMP).append(tmp);

    var device = deviceTMP;

    showDeviceState(device, obj.stan);
    
    device.click(function () {
        clickDevice(this,obj.id, obj.room);
    })



    $(device).hover(function() {
        if ($(this).attr("state") != "off") {

            $(this).css('background-color', 'var(--primaryDarkColor)');
            $(this).children().first().css('background-color', 'var(--primaryColor)');
        }
        else{
            $(this).css('background-color', 'var(--deviceOffDarkColor)');
            $(this).children().first().css('background-color', 'var(--deviceOffColor)');
        }
        },function() {
            $(this).css('background-color', '');
            $(this).children().first().css('background-color', '');
        })
    // $('.device').hover(function () {
    //     $(this).children().css('background-color', '#FFFFFF22');
    //     $(this).children().first().css('background-color', '#FFFFFF44');
    // }, function () {
    //     $(this).children().css('background-color', '#FFFFFF00');
    //     $(this).children().first().css('background-color', '#FFFFFF22');
    // });

    return device;
}
function clickDevice(mee, id, room) {
    var me = $(mee);
    if (me.attr("state") == "on") {
        console.debug("me on");
        
    }
    else{
        console.debug("me off");
    }

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
                showDeviceState(me, me.attr("state") == "on" ? false : true)
            } else {
                $("#err-msg").html("Error: " +  response.error);
                $("#err-msg").show( "bounce", {}, 1000, function(){hideAfter(this, 10000)} );
            }
        }
    });
    
}

function showDeviceState(device, stan){
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
}