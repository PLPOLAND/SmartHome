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
                response.obj.devices.forEach(element => {
                    body.append(addDevice(element));
                });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
});