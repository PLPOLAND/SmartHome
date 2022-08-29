$(document).ready(function () {

});

function addsensor(obj) {
    
    //TODO dodać inne rodzaje ikonek w znależności od rodzaju urzadzenia
    var sensorTMP = $("<div class=\"sensor\"></div >");
    sensorTMP.attr("id", obj.id);
    var sensorIcon = $("<div class=\"sensorIcon\" ></div >");
    if (obj.typ === "THERMOMETR") {
        sensorIcon.append($("<i class=\"icon-thermometer\"></i>"));
    }
    $(sensorTMP).append(sensorIcon);

    var sensorDescribe = $("<div class=\"sensorDescribe\"></div>");
    sensorDescribe.append($("<span class=\"sensorDescribeText\">"+obj.temperatura+" &deg;C</span>"));
    // sensorDescribe.append($("<br>"));
    if (obj.typ === "THERMOMETR") {
        sensorDescribe.append($("<span class=\"sensorDescribeRoomName\">"+obj.name+"</span>"));
        // sensorTMP.attr("state", obj.stan ? "on" : "off");
    } 
    $(sensorTMP).append(sensorDescribe);


    // var sensorState = "<div class=\"sensorState\"></div>";
    // var sensorStateIcon = $(sensorState);
    // sensorStateIcon.html(obj.temperatura+"&deg;C");
    // $(sensorTMP).append(sensorStateIcon);

    var sensor = sensorTMP;

    showsensorState(sensor, obj.stan, obj.typ);
    
    sensor.click(function () {
        clicksensor(this,obj.id, obj.room, obj.typ);
    })

    return sensor;
}
function clicksensor(mee, id, room, typ) {
    var me = $(mee);
    // alert("Nie zaprogramowane!");
}

function showsensorState(sensor, stan, sensorType){
    var id = sensor.attr("id");
    console.log(id);

    $.ajax({
        url: "/admin/api/getTermometrByID",
        type: 'post',
        data: {
            id: id
        },
        success: function (sensorsResponse) {
            if (sensorsResponse.error == null) {
                console.log("update of termometr id:"+id);
                console.log(sensorsResponse.obj);
                sensor.children(".sensorDescribe").children(".sensorDescribeText").html(sensorsResponse.obj)
            } else {
                $("#err-msg").html(sensorsResponse.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });

    //TODO!
    //jquery by id, id from sensor
    setTimeout(function () {
        showsensorState(sensor,stan,sensorType);
    },10000);
}