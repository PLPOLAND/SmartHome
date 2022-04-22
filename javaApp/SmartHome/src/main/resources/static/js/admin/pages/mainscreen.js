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
                    room.devices.forEach(element=>{
                        tmp.append(addDevice(element));
                    })
                    body.append(tmp);
                });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
});