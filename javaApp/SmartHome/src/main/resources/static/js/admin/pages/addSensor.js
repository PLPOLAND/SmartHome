$(document).ready(function () {
    $("#clear").click(function() {
        clear();
    })
    $("#save").click(function() {
        save();
    })

    $("#deviceType").change(function(){
        
        //TODO dodać pola i obsługę Termometru
    });

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
                $("#room option[value='0']").remove();
                var i = 0;
                response.obj.forEach(element => {
                    $("#room").append('<option value="' + element+'"> '+element+' </option >') ;
                    i++;
                });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
    $.ajax({
        url: "api/getSensorTypes",
        type: 'get',
        data: {
        },
        success: function (response) {
            // console.log(response);

            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                $("#sensorType option[value='0']").remove();
                var i = 0;
                response.obj.forEach(element => {
                    $("#sensorType").append('<option value="'+element+'"> '+element+' </option >') ;
                    i++;
                });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });

    $("#sensorType").change(function() {
        if ($("#sensorType").val()!== "BUTTON") {
            $("#err-msg").html("Ten typ sensora nie został jeszcze zaprogramowany, wybierz inny!");
            $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
        }
    })

});

function save() {
    var url = "api/";
    var sensorType = $("#sensorType").val();
    if (sensorType === "BUTTON") {
        url += "addPrzycisk"
    }
    else {
        $("#err-msg").html("Ten typ sensora nie został jeszcze zaprogramowany, wybierz inny!");
        $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 5000) });
        return;
    }

    $.ajax({
        url: url,
        type: 'get',
        
        data: { 
            name: $("#name").val(),
            roomName: $("#room").val(),
            boardID: $("#boardID").val(),
            pin: $("#pin").val(),
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