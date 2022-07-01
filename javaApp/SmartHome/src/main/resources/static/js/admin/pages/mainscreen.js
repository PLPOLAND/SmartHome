var delay = 1000;
$(document).ready(function () {
    $.ajax({
        url: "/admin/api/getSystemData",
        type: 'get',
        data: {},
        success: function (response) {
            // console.log(response);

            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj.devices);
                var body = $("#main-body");

                response.obj.roomsArrayList.forEach(room => {
                    body.append(makeRoom(room.nazwa, room.devices.length, room.sensors.length))
                    var tmp = $("<div class=\"devList\"></div>")
                    tmp.attr("roomID",room.id);
                    // tmp.hide('blind', {}, 1000, function () { });
                    room.devices.forEach(element=>{
                        tmp.append(addDevice(element));
                    })
                    body.append(tmp);

                    

                });
                $.ajax({
                    url: "/admin/api/getTermometers",
                    type: 'post',
                    data: {},
                    success: function (sensorsResponse) {
                        if (sensorsResponse.error == null) {
                            console.log(sensorsResponse.obj);
                            var list = $(".devList");
                            // sensorsResponse.obj.sort(compareSensors)
                            sensorsResponse.obj.forEach(element => {
                                var room = $(".devList[roomid=" + element.room + "]")
                                room.append(addsensor(element));
                            });
                        } else {
                            $("#err-msg").html(sensorsResponse.error);
                            $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
                        }
                    }
                });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });

    setTimeout(function(){
        updateDevices();
    },delay);
    
});
function updateDevices() {
    $.ajax({
        url: "/admin/api/getDevices",
        type: 'get',
        data: {},
        success: function (response) {
            // console.log(response);

            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);

                

                    response.obj.forEach(element => {
                        var tmp = $("#" + element.id);
                        if (element.typ === "LIGHT") {
                            showDeviceState(tmp, element.stan, element.typ)
                        }
                        else if(element.typ === "BLIND"){
                            // console.log("BLIND");
                            if (element.stan === "UP") {
                                showDeviceState(tmp, true, element.typ)
                                // console.log("BLIND UP");
                            } else if (element.stan === "DOWN") {
                                showDeviceState(tmp, false, element.typ)
                                // console.log("BLIND DOWN");
                                
                            }
                        }
                    })
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });

    setTimeout(function () {
        updateDevices();
    }, delay);
}