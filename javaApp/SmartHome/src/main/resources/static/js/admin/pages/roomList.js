$(document).ready(function () {
    $.ajax({
        url: "/admin/api/getRoomsList",
        type: 'post',
        data: {},
        success: function (response) {
            if (response.error == null) {
                console.log(response.obj);
                var list = $(".list");
                response.obj.forEach(element => {
                    list.append(makeRoom(element.nazwa, element.devices.length, element.sensors.length));
                });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
});