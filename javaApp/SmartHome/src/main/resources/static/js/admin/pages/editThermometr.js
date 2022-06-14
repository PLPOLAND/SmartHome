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

            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });

    onload(id);

});


function save() {
    var url = "api/editButton";//TODO
    // $.ajax({
    //     url: url,
    //     type: 'get',
        
    //     data: { 
    //         buttonId: id,
    //         roomName: $("#room").val(),
    //         name: $("#name").val(),
    //         boardID: $("#boardID").val(),
    //         pin: $("#pin").val()
    //     },
    //     success: function (response) {
    //         // console.log(response);

    //         // $("#err-msg").html(response);
    //         if (response.error == null) {
    //             console.log(response.obj);
    //             $("#msg").html(response.obj);
    //             $("#msg").show("bounce", {}, 1000, function () { hideAfter(this, 5000) });
    //         } else {
    //             $("#err-msg").html(response.error);
    //             $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
    //         }
    //     }
    // });
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
}