var id=-1; //ID edytowanego przycisku
var rooms; // Pokoje w systemie 
var oldClicks = -1; // stara liczba kliknięć dla edytowanej funkcji w celu poprawnego usunięcia zmienianej funkcji kliknięć
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
                    $("#room").append('<option value="' + element+'"> '+element+' </option >') ;
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
    $("#deviceTypeEdit").change(function () {
        if ($("#deviceTypeEdit").val()==1) {
            $(".showBlind").show();
            $(".showLight").hide();
        } else {
            $(".showBlind").hide();
            $(".showLight").show();
            
        }
    })
    $("#deviceTypeEdit").change();

    $("#addClickFunction").click(function(){addClickFuntion()});
    $("#addClickFunctionWindow").click(function () {
        $("#addClickFunctionWindow").hide("blind", {}, 1000, function () {});
    }).children().click(function (e) {
        return false;
    });
    $("#listClickFunction").click(function(){addClickFuntion()});
    $("#listClickFunctionWindow").click(function () {
        $("#listClickFunctionWindow").hide("blind", {}, 1000, function () {});
    }).children().click(function (e) {
        return false;
    });
    $("#editClickFunctionWindow").click(function () {
        $("#editClickFunctionWindow").hide("blind", {}, 1000, function () {});
    }).children().click(function (e) {
        return false;
    });

    $("#add").click(function(){
        $("#addClickFunctionWindow").show("blind", {}, 1000, function () { })
    })
    $("#list").click(function(){
        $("#listClickFunctionWindow").show("blind", {}, 1000, function () { })
    })

    $("#modifyClickFunction").click(function () {
        saveButtonFunction();
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
                $("#ctrDeviceBEdit option[value='0']").remove();
                // var i = 0;
                response.obj.forEach(element => {
                    $("#ctrDeviceB").append('<option value="' + element.id + '">' + element.id + ' - ' + rooms[element.room] + ' - ' + element.name + ' </option >');
                    $("#ctrDeviceBEdit").append('<option value="' + element.id + '">' + element.id + ' - ' + rooms[element.room] + ' - ' + element.name + ' </option >');
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
                $("#ctrDeviceLEdit option[value='0']").remove();
                // var i = 0;
                response.obj.forEach(element => {
                    $("#ctrDeviceL").append('<option value="' + element.id + '">' + element.id + ' - ' + rooms[element.room] + ' - ' + element.name + ' </option >');
                    $("#ctrDeviceLEdit").append('<option value="' + element.id + '">' + element.id + ' - ' + rooms[element.room] + ' - ' + element.name + ' </option >');
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
    var url = "api/editButton";
    $.ajax({
        url: url,
        type: 'get',
        
        data: { 
            buttonId: id,
            roomName: $("#room").val(),
            name: $("#name").val(),
            boardID: $("#boardID").val(),
            pin: $("#pin").val()
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
function saveButtonFunction() {
    var url = "api/editButtonFunction";
    var device, stan = "";
    if ($("#deviceTypeEdit").val() == 0) {//LIGHT
        device = $("#ctrDeviceLEdit").val();
        stan = "NONE";
    }
    else {
        device = $("#ctrDeviceBEdit").val();
        stan = $("#stan").val();
    }
    $.ajax({
        url: url,
        type: 'get',
        
        data: { 
            buttonId: id,
            deviceId: device,
            state: stan,
            clicks: $("#clicksEdit").val(),
            oldClicks: oldClicks
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
                
                $("#room").val(rooms[response.obj.room])
                

            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
    $.ajax({
        url: "api/getButtonFunction",
        type: 'get',
        data: {
            buttonId: id1
        },
        success: function (response) {
            // console.log(response);

        //     // $("#err-msg").html(response);
            if (response.error == null) {
                console.log("getButtonfunction response");
                console.log(response.obj);
                response.obj.forEach(element => {
                    $("#listClickFunctionWindow").children().append(makeFunctionList(element))
                    
                });
                

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
                $("#msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });

            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
}

function makeFunctionList(obj) {
    var funlist = $("<div class=\"function_list wiersz\">");

    var clicks = $('<div class="clicks">Kliknięć: '+obj.clicks+'</div>');
    var devType = $('<div class="deviceType">Rodzaj urzadzenia: '+obj.device.typ+'</div>');
    var devName = $('<div class="deviceName">Nazwa urządzenia: '+obj.device.name+'</div>');
    var stan = $('<div class="stan">Stan: '+obj.state+'</div>');

    var edit = $("<div class=\"icon\" title=\"Edytuj\"></div>");
    edit.append($("<i class=\"icon-sliders\"></i>"));
    var del = $("<div class=\"icon\" title=\"Usuń\"></div>");
    del.append($("<i class=\"icon-trash\"></i>"));

    edit.click(function () {
        $('#editClickFunctionWindow').show("blind", {}, 1000, function () { });
        oldClicks = obj.clicks;
    })

    del.click(function () {
        $.ajax({
            url: "api/rmButtonClickFunction",
            type: 'get',
            data: {
                buttonID: id,
                clicks: obj.clicks
            },
            success: function (response) {
                // console.log(response);

                //     // $("#err-msg").html(response);
                if (response.error == null) {
                    console.log(response.obj);

                    $("#msg").html(response.obj);
                    $("#msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });

                } else {
                    $("#err-msg").html(response.error);
                    $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
                }
            }
        });
    })



    funlist.append(clicks);
    funlist.append(devType);
    funlist.append(devName);
    funlist.append(stan);

    funlist.append(del);
    funlist.append(edit);

    return funlist;

}