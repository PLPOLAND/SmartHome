var action_id = 0;//id kolejnego elementu w tabeli akcji
var condition_id = 0;//id kolejnego elementu w tabeli warunków

var devices;
var states;
var rooms;
var buttons;
var id = -1;
$(document).ready(function () {
    $("#clear").click(function() {
        clear();
    })
    $("#save").click(function() {
        save();
    })

    $("#addAction").click(function() {
    
        var row = addAction(devices, states);
        $("#actionsTable").append(row);
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
                rooms = response.obj;
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
        },
        async: false
    });
    $.ajax({
        url: "api/getFunctionTypes",
        type: 'get',
        data: {
        },
        success: function (response) {
            // console.log(response);

            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                $("#functionType option[value='0']").remove();
                var i = 0;
                response.obj.forEach(element => {
                    $("#functionType").append('<option value="'+element+'"> '+element+' </option >') ;
                    i++;
                });

                $("#functionType").change(function () {
                    if ($("#functionType").val() == "AUTOMATION") {
                        $(".automationF").show();
                        $(".buttonF").hide();
                        $(".userF").hide();
                    }
                    else if ($("#functionType").val() == "BUTTON") {
                        $(".automationF").hide();
                        $(".buttonF").show();
                        $(".userF").hide();
                    }
                    else if ($("#functionType").val() == "USER") {
                        $(".automationF").hide();
                        $(".buttonF").hide();
                        $(".userF").show();
                    }
                    else if ($("#functionType").val() == "NOTKNOWN") {
                        $(".automationF").hide();
                        $(".buttonF").hide();
                        $(".userF").hide();
                    }
                })
                $("#functionType").change();

            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        },
        async: false
    });

    $.ajax({
        url: "api/getDevices",
        type: 'get',
        data: {},
        success: function (response) {
            // console.log(response);

            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                devices = response.obj;
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
    $.ajax({
        url: "api/getDeviceStates",
        type: 'get',
        data: {},
        success: function (response) {
            // console.log(response);

            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                states = response.obj;
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });

    $.ajax({
        url: "api/getButtons",
        type: 'get',
        data: {},
        success: function (response) {
            // console.log(response);
            buttons = response.obj;
            $("#button option[value='-1']").remove();
            response.obj.forEach(element => {
                $("#button").append('<option value="'+element.id+'"> '+element.name +" - "+rooms[element.room]+' </option >') ;
            });
        }
    });

    $.ajax({
        url: "api/getButtonClickTypes",
        type: 'get',
        data: {},
        success: function (response) {
            console.log(response);
            $("#clickType option[value='-1']").remove();
            response.obj.forEach(element => {
                $("#clickType").append('<option value="'+element+'"> '+element+' </option >') ;
            }
            );
        },
        async: false
        });
    $.ajax({
        url: "api/getFunction",
        type: 'get',
        data: {
            id: id
        },
        success: function (response) {
            if (response.error == null) {
                console.log(response.obj);
                $("#name").val(response.obj.name);
                $("#functionType").val(response.obj.type);
                $("#functionType").change();
                $("#room").val(response.obj.room);
                $("#button").val(response.obj.button.id);
                $("#clickType").val(response.obj.clickType);
                $("#clicks").val(response.obj.clicks);
                var rownum = 0;
                response.obj.actions.forEach(element => {
                    var row = addAction(devices, states);
                    $("#actionsTable").append(row);
                    $($("#actionsTable tr[num = " + rownum + "] #device")).val(element.device.id);
                    $($("#actionsTable tr[num = " + rownum + "] #state")).val(element.activeDeviceState);
                    if (element.allowReverse) {
                        $($("#actionsTable tr[num = " + rownum + "] #revers")).prop("checked", true);
                    } else {
                        $($("#actionsTable tr[num = " + rownum + "] #revers")).prop("checked", false);
                    }
                    rownum++;
                });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
});

function save() {

    var akcje = [];
    var isOk = false;
    

    for (let index = 0; index < $("#actionsTable tr").length - 1; index++) {
        var device;
        var state;
        var reversable;
        device = $($("#actionsTable tr[num = " + index + "] #device")).val();
        state = $($("#actionsTable tr[num = " + index + "] #state")).val();
        reversable = $($("#actionsTable tr[num = " + index + "] #revers")).prop('checked');
        console.log(device, state, reversable);
        var action = { device: device, activeDeviceState: state, allowReverse: reversable };
        $.ajax({
            url: "api/checkAction",
            type: 'post',
            dataType: 'json',
            data: {
                action: JSON.stringify(action)
            },
            async: false,
            success: function (response) {
                console.log(response);
                if (response.error == null) {
                    if (response.obj == true) {
                        isOk = true;
                    }
                    else {
                        isOk = false;
                    }
                } else {
                    $("#err-msg").html(response.error);
                    $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
                }
            }
        })

        if (isOk == true) {
            akcje.push(action);
        }
        else {
            akcje = [];
            $("#err-msg").html("Akcja nr " + (index+1) + " z urządzeniem " + device + " na stan " + state + " jest niepoprawna");
            $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            break;
        }
    }


    var url = "api/";
    var functionType = $("#functionType").val();
    if (functionType === "BUTTON") {
        url += "editButtonGlobalFunction"
    }
    else {
        $("#err-msg").html("Ten typ Funckji nie został jeszcze zaprogramowany, wybierz inny!");
        $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 5000) });
        return;
    }

    $.ajax({
        url: url,
        type: 'post',
        data: { 
            functionId: id,
            name: $("#name").val(),
            buttonId: $("#button").val(),
            clicks: $("#clicks").val(),
            clickType: $("#clickType").val(),
            actions: JSON.stringify(akcje)
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

function addAction(devices, states){
    
        // <tr id="1">
        //     <td>
        //         <select id="device">
        //             <option value="0">Brak</option>
        //         </select>
        //     </td>
        //     <td>
        //         <select id="state">
        //             <option value="0">Brak</option>
        //         </select>
        //     </td>
        //     <td>
        //         <input type="checkbox">
        //     </td>
        // </tr>

    var row = $("<tr num = \""+action_id+"\"></tr>");
    action_id = action_id+1;
    var device = $("<td></td>");
    var selectDev = $("<select id=\"device\"></select>");
    devices.forEach(element => {
        selectDev.append('<option value="'+element.id+'"> ['+element.id+'] '+element.name+" - "+rooms[element.room]+" - "+element.typ+' </option >') ;
    });
    device.append(selectDev);
    var state = $("<td></td>");
    var selectDev = $("<select id=\"state\"></select>");
    states.forEach(element => {
        selectDev.append('<option value="'+element+'"> '+element+' </option >') ;
    });
    state.append(selectDev);
    var checkbox = $("<td></td>");
    var check = $("<input id=\"revers\"class=\"checkbox\" type=\"checkbox\">");
    checkbox.append(check);
    var deleteButton = $("<td></td>");
    var deleteMe = $('<div class="icon iconDelete" title="Usuń"><i class="icon-trash"></i></div>');
    deleteMe.click( function(){
        row.remove();
    });
    deleteButton.append(deleteMe);


    row.append(device);
    row.append(state);
    row.append(checkbox);
    row.append(deleteButton);
    return row;
}   