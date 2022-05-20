
function makeFullSensor(obj) {
    var device = $("<div class=\"fullSensor\"></div>");

    var sensorIcon = $('<div class="sensorIcon"></div>');

    if (obj.typ === 'THERMOMETR') {
        sensorIcon.append($('<i class="icon-thermometer"></i>'))
    }
    else if (obj.typ === 'BUTTON') {
        sensorIcon.append($('<i class="icon-button"></i>'))
    }

    var sensorName = $('<div class="sensorName">' + obj.name + '</div>');
    var slaveAddr = $('<div class="slave">Slave: ' + obj.slaveID + '</div>')
    var device_pin;
    var clikcFunctions;
    var holdFunctions;
    var value;
    if (obj.typ === 'THERMOMETR') {
        value = $('<div class="value">Temperatura: '+obj.temperatura+'&deg;C</div>')
        
    }
    else if (obj.typ === 'BUTTON') {
        device_pin = $('<div class="pin">Pin: ' + obj.pin + '</div>')
        clikcFunctions = $('<div class="clickFunctionNum">Fun. Kliknięcia: ' + obj.funkcjeKlikniec.length + '</div>')
        holdFunctions = $('<div class="holdFunctionNum">Fun. Przytrzymania: ' + 0 + '</div>') // TODO dodać po dodaniu funkcjonalności
        
    }



    var deleteMe = $('<div class="icon" title="Usuń"><i class="icon-trash"></i></div>');
    var editMe = $('<div class="icon" title="Edytuj"><i class="icon-sliders"></i></div>');

    editMe.click(function () {
        //TODO
        if (obj.typ === 'BUTTON') {
            document.location.href = "/admin/editButton?id=" + obj.id;
        }
        else
            document.location.href = "/admin/editThermometr?id=" + obj.id;
    })

    deleteMe.click(function () {
        //TODO
        // document.location.href = "/admin/api/removeDeviceByID?id=" + obj.id;
        // $.ajax({
        //     url: "/admin/api/removeDeviceByID",
        //     type: 'get',
        //     data: {
        //         id: obj.id,
        //     },
        //     success: function (response) {
        //         // console.log(response);

        //         // $("#err-msg").html(response);
        //         if (response.error == null) {
        //             console.log(response.obj);
        //             $("#msg").html(response.obj);
        //             $("#msg").show("bounce", {}, 1000, function () { hideAfter(this, 5000) });
        //             $(this).parent().remove()
        //         } else {
        //             $("#err-msg").html(response.error);
        //             $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
        //         }
        //     }
        // });
    })

    device.append(sensorIcon);
    device.append(sensorName);
    device.append(slaveAddr);
    if (obj.typ === 'THERMOMETR') {
        device.append(value);
    }
    else if (obj.typ === 'BUTTON') {
        device.append(device_pin);
        device.append(clikcFunctions);
        device.append(holdFunctions);
    }
    device.append(deleteMe);
    device.append(editMe);

    return device;

}