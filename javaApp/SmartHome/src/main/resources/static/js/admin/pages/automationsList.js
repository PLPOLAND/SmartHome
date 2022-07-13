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
                // $.ajax({
                //     url: "/admin/api/getDevices",
                //     type: 'post',
                //     data: {},
                //     success: function (response) {
                //         if (response.error == null) {
                //             console.log(response.obj);
                //             var list = $(".list");
                //             response.obj.forEach(element => {
                //                 list.append(makeFullDevice(element));
                //             });
                //         } else {
                //             $("#err-msg").html(response.error);
                //             $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
                //         }
                //     }
                // });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
    
});