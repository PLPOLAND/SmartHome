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
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });

    $.ajax({
        url: "/admin/api/getSensors",
        type: 'post',
        data: {},
        success: function (response) {
            if (response.error == null) {
                console.log(response.obj);
                var list = $(".list");
                response.obj.sort(compareSensors)
                response.obj.forEach(element => {
                    list.append(makeFullSensor(element,rooms));
                });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
});

function compareSensors(a,b) {
    return a.typ.localeCompare(b.typ);
}