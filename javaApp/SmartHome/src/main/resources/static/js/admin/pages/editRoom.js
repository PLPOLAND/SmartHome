var originalRoomName;
$(document).ready(function () {
    originalRoomName = $("#roomName").val();


    $("#save").click(function () {
        save();
    })

});


function save() {
    $.ajax({
        url: "api/editRoom",
        type: 'get',
        data: {
            originalName: originalRoomName,
            name: $("#roomName").val()
        },
        success: function (response) {
            // console.log(response);

            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                $("#msg").html(response.obj);
                $("#msg").show("bounce", {}, 1000, function () { hideAfter(this, 5000) });
                setTimeout(function () {
                    document.location.href = "/admin/roomsList";
                }, 5000);
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });

}