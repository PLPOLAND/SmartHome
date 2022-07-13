function makeFullFunction(obj, rooms) {
    var fun = $("<div class=\"fullSensor\"></div>");

    var id = $("<div class=\"id\"></div>");
    id.append(obj.id);

    var name = $("<div class=\"name\"></div>");
    name.html(obj.name);

    var actions = $("<div class=\"actions\"></div>");
    actions.html("Akcji: " + obj.actions.length);
    
    var stan = $("<div class=\"stan\"></div>");
    stan.html("Stan: " + obj.active);

    

    var deleteMe = $('<div class="icon" title="Usuń"><i class="icon-trash"></i></div>');
    var editMe = $('<div class="icon" title="Edytuj"><i class="icon-sliders"></i></div>');

    editMe.click(function () {
        // //TODO
        // if (obj.typ === 'BUTTON') {
        //     document.location.href = "/admin/editButton?id=" + obj.id;
        // }
        // else
        //     document.location.href = "/admin/editThermometr?id=" + obj.id;
    })

    deleteMe.click(function () {
        //TODO
        // document.location.href = "/admin/api/removeDeviceByID?id=" + obj.id;
        // $.ajax({
            // url: "/admin/api/removeSensorByID",
            // type: 'get',
            // data: {
            //     id: obj.id,
            // },
            // success: function (response) {
            //     // console.log(response);

            //     // $("#err-msg").html(response);
            //     if (response.error == null) {
            //         console.log(response.obj);
            //         $("#msg").html(response.obj);
            //         $("#msg").show("bounce", {}, 1000, function () { hideAfter(this, 5000) });
            //         $(this).parent().remove()
            //     } else {
            //         $("#err-msg").html(response.error);
            //         $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            //     }
            // }
        // });
    })

    device.append(sensorIcon);
    device.append(sensorName);
    device.append(roomName);
    device.append(slaveAddr);
    if (obj.typ === 'THERMOMETR') {
        device.append(adress);
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