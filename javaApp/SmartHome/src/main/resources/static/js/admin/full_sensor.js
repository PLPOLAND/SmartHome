// $(document).ready(function () { 
//     $.ajax({
//         url: "api/getRoomsNamesList",
//         type: 'get',
//         data: {
//         },
//         success: function (response) {
//             // console.log(response);

//             // $("#err-msg").html(response);
//             if (response.error == null) {
//                 console.log(response.obj);
//                 rooms = response.obj;
//                 $.ajax({
//                     url: "/admin/api/getTmpTermometr",
//                     type: 'post',
//                     data: {},
//                     success: function (sensorsResponse) {
//                         if (sensorsResponse.error == null) {
//                             console.log(sensorsResponse.obj);
//                             var list = $(".list");
//                             sensorsResponse.obj.sort(compareSensors)
//                             sensorsResponse.obj.forEach(element => {
//                                 list.append(makeFullSensor(element, rooms));
//                             });
//                         } else {
//                             $("#err-msg").html(sensorsResponse.error);
//                             $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
//                         }
//                     }
//                 });
//             } else {
//                 $("#err-msg").html(response.error);
//                 $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
//             }
//         }
//     });
// });
function makeFullSensor(obj, rooms) {
    var device = $("<div class=\"fullSensor\"></div>");

    var sensorIcon = $('<div class="sensorIcon"></div>');

    if (obj.typ === 'THERMOMETR') {
        sensorIcon.append($('<i class="icon-thermometer"></i>'))
    }
    else if (obj.typ === 'BUTTON') {
        sensorIcon.append($('<i class="icon-button"></i>'))
    }

    var sensorName = $('<div class="sensorName">' + obj.name + '</div>');
    var roomName = $('<div class="roomName">Room: ' + rooms[obj.room] + '</div>');
    var slaveAddr = $('<div class="slave">Slave: ' + obj.slaveID + '</div>')
    var device_pin;
    var clikcFunctions;
    var holdFunctions;
    var value;
    var adress;
    if (obj.typ === 'THERMOMETR') {
        var i = 0;
        var addresVal="";
        for (const element of obj.addres) {
            addresVal += element + " ";
            
        }
        adress = $('<div class="adress">Adres: '+addresVal+'</div>');
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
        $.ajax({
            url: "/admin/api/removeSensorByID",
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