var id=-1;
var rooms;
$(document).ready(function () {
    $("#clear").click(function() {
        clear();
    })
    $("#save").click(function() {
        save();
    })

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
                $("#room option[value='0']").remove();
                var i = 0;
                response.obj.forEach(element => {
                    $("#room").append('<option value="' + i+'"> '+element+' </option >') ;
                    i++;
                });

                getLights();
                getBlinds();

            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
    $("#deviceType").change(function () {
        if ($("#deviceType").val()==1) {
            $(".showBlind").show();
            $(".showLight").hide();
        } else {
            $(".showBlind").hide();
            $(".showLight").show();
            
        }
    })
    $("#deviceType").change();

    $("#addClickFunction").click(function(){addClickFuntion()});
    $("#addClickFunctionWindow").click(function () {
        $("#addClickFunctionWindow").hide("blind", {}, 1000, function () {});
    }).children().click(function (e) {
        return false;
    });

    $("#add").click(function(){
        $("#addClickFunctionWindow").show("blind", {}, 1000, function () { })
    })
    $("#list").click(function(){
        $("#err-msg").html("TODO");
        $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000); });

    })
    onload(id);

});

function getBlinds() {
    $.ajax({
        url: "api/getBlindsList",
        type: 'get',
        data: {},
        success: function (response) {
            // console.log(response);
            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                response.obj.sort((a, b) => a.id > b.id);
                $("#ctrDeviceB option[value='0']").remove();
                // var i = 0;
                response.obj.forEach(element => {
                    $("#ctrDeviceB").append('<option value="' + element.id + '">' + element.id + ' - ' + rooms[element.room] + ' - ' + element.name + ' </option >');
                    // i++;
                });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000); });
            }
        }
    });
}

function getLights() {
    $.ajax({
        url: "api/getLightList",
        type: 'get',
        data: {},
        success: function (response) {
            // console.log(response);
            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                response.obj.sort((a, b) => a.id > b.id);
                $("#ctrDeviceL option[value='0']").remove();
                // var i = 0;
                response.obj.forEach(element => {
                    $("#ctrDeviceL").append('<option value="' + element.id + '">' + element.id + ' - ' + rooms[element.room] + ' - ' + element.name + ' </option >');
                    // i++;
                });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000); });
            }
        }
    });
}

function save() {
    return;
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
        url: "api/getSensorById",
        type: 'get',
        data: {
            id: id1
        },
        success: function (response) {
            // console.log(response);

        //     // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                
                $("#room").val(response.obj.room)
                

            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
}

function addClickFuntion() {
    var device,stan="";
    if ($("#deviceType").val()==0) {//LIGHT
        device = $("#ctrDeviceL").val();
        stan = "NONE";
    }
    else{
        device = $("#ctrDeviceB").val();
        stan = $("#stan").val();
    }


    $.ajax({
        url: "api/addButtonClickFunction",
        type: 'get',
        data: {
            buttonID: id,
            deviceID: device,
            state: stan,
            clicks: $("#clicks").val()
        },
        success: function (response) {
            // console.log(response);

            //     // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);

                $("#msg").html(response.obj);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });

            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
}