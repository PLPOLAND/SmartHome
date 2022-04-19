$(document).ready(function () {
    $("#clear").click(function() {
        clear();
    })
    $("#rm").click(function() {
        remove();
    })
});

function remove() {
    $.ajax({
        url: "api/removeRoom",
        type: 'get',
        data: {
            name: $("#roomName").val()
        },
        success: function (response) {
            // console.log(response);

            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                $("#msg").html(response.obj);
                $("#msg").show("bounce", {}, 1000, function () { hideAfter(this, 5000) });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
}
function clear() {
    $("input").val("")
}