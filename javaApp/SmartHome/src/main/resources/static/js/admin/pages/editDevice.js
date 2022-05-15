var id=-1;
$(document).ready(function () {
    $("#clear").click(function() {
        clear();
    })
    $("#save").click(function() {
        save();
    })

    $("#deviceType").change(function(){
        if ($(this).val()==="BLIND") {
            $(".toHide").show('blind', {}, 1000, function () { });
        }
        else{
            $(".toHide").hide('blind', {}, 1000, function () { });
        }
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
                    $("#room").append('<option value="' + i+'"> '+element+' </option >') ;
                    i++;
                });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
    $.ajax({
        url: "api/getDeviceTypes",
        type: 'get',
        data: {
        },
        success: function (response) {
            // console.log(response);

            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                $("#deviceType option[value='0']").remove();
                var i = 0;
                response.obj.forEach(element => {
                    $("#deviceType").append('<option value="'+element+'"> '+element+' </option >') ;
                    i++;
                });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });

    onload(id);

});

function save() {

    var url = "api/";
    var deviceType = $("#deviceType").val();
    if (deviceType === "LIGHT") {
        url += "editSwiatlo"
    }
    else if(deviceType === "BLIND"){
        url += "editRoleta"
    }
    else {
        $("#err-msg").html("Urządzenie jeszcze nie zostało zaprogramowane w systemie!");
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
            deviceId: id,
            pin: $("#pin1").val(),
            pinDown: $("#pin2").val()
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
    // $("input").val("")//TODO delete device
}

function onload(id1) {
    $.ajax({
        url: "api/getDeviceById",
        type: 'get',
        data: {
            id: id1
        },
        success: function (response) {
            // console.log(response);

        //     // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                
                $("#deviceType").val(response.obj.typ);
                $("#room").val(response.obj.room)
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
}