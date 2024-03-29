$(document).ready(function () {
    
});

function makeFullDevice(obj) {
    var device = $("<div class=\"fullDevice\"></div>");

    var deviceIcon = $('<div class="deviceIcon"></div>');

    if (obj.typ ==='LIGHT') {
        deviceIcon.append($('<i class="icon-lightbulb"></i>'))
    }
    else if (obj.typ ==='BLIND') {
        deviceIcon.append($('<i class="icon-server"></i>'))
    }

    var deviceName = $('<div class="deviceName">'+obj.name+'</div>');
    var roomName = $('<div class="roomName">Room: '+rooms[obj.room]+'</div>');
    var slaveAddr = $('<div class="slave">Slave: '+ obj.slaveID+'</div>')
    var device_pin1;
    var device_pin2;
    var deviceState;
    if (obj.typ === 'LIGHT') {
        device_pin1 = $('<div class="pin">Pin: ' + obj.pin + '</div>')
        if (obj.stan) {
            deviceState = $('<div class="stan">Stan: OFF</div>')
        }
        else{
            deviceState = $('<div class="stan">Stan: ON</div>')
        }
    }
    else if (obj.typ === 'BLIND') {
        device_pin1 = $('<div class="pin">Pin UP: ' + obj.switchDown.pin + '</div>')
        device_pin2 = $('<div class="pin">Pin DOWN: ' + obj.switchUp.pin + '</div>')
        if (obj.stan ==='UP') {
            deviceState = $('<div class="stan">Stan: Podniesiona</div>')
        }
        else {
            deviceState = $('<div class="stan">Stan: Opuszczona</div>')
        }
    }



    var deleteMe = $('<div class="icon" title="Usuń"><i class="icon-trash"></i></div>');
    var editMe = $('<div class="icon" title="Edytuj"><i class="icon-sliders"></i></div>');

    editMe.click(function () {
        document.location.href = "/admin/editDevice?deviceID=" + obj.id;
    })

    deleteMe.click(function () {
        // document.location.href = "/admin/api/removeDeviceByID?id=" + obj.id;
        $.ajax({
            url: "/admin/api/removeDeviceByID",
            type: 'get',
            data: {
                id: obj.id,
            },
            success: function (response) {
                // console.log(response);

                // $("#err-msg").html(response);
                if (response.error == null) {
                    console.log(response.obj);
                    $("#msg").html(response.obj);
                    $("#msg").show("bounce", {}, 1000, function () { hideAfter(this, 5000) });
                    $(this).parent().remove()
                } else {
                    $("#err-msg").html(response.error);
                    $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
                }
            }
        });
    })

    device.append(deviceIcon);
    device.append(deviceName);
    device.append(roomName);
    device.append(deviceState);
    device.append(slaveAddr);
    device.append(device_pin1);
    device.append(device_pin2);
    device.append(deleteMe);
    device.append(editMe);

    return device;

}