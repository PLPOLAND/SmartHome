var rooms;
$(document).ready(function () {

    $.ajax({
        url: "api/getRoomsNamesList",
        type: 'get',
        data: {
        },
        success: function (response) {
            // console.log(response);

            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                rooms = response.obj;
                $.ajax({
                    url: "/admin/api/getSensors",
                    type: 'post',
                    data: {},
                    success: function (sensorsResponse) {
                        if (sensorsResponse.error == null) {
                            console.log(sensorsResponse.obj);
                            var list = $(".list");
                            sensorsResponse.obj.sort(compareUsers)
                            sensorsResponse.obj.forEach(element => {
                                list.append(makeFullSensor(element, rooms));
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

    
});

function compareUsers(a,b) {
    return a.typ.localeCompare(b.typ);
}