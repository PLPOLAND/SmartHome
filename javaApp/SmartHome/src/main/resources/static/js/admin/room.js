function makeRoom(name, devicesNum, sensorsNum) {
    var room = $("<div class=\"room\"></div>");
    var roomName = $("<div class=\"roomName\">"+name+"</div>");
    var devices = $("<div class=\"deviceNum\">Urządzeń: " + devicesNum+"</div>");
    var sensors = $("<div class=\"deviceNum\">Sensorów: " + sensorsNum+"</div>");
    var edit = $("<div class=\"icon\" title=\"Edytuj\"></div>");
    edit.append($("<i class=\"icon-sliders\"></i>"));
    var del = $("<div class=\"icon\" title=\"Usuń\"></div>");
    del.append($("<i class=\"icon-trash\"></i>"));
    
    edit.click(function () {
        document.location.href = "/admin/editRoom?roomName="+ name;
    })
    
    del.click(function(){
        document.location.href = "/admin/removeRoom?roomName="+ name;

    })
    
    
    
    room.append(roomName);
    room.append(devices);
    room.append(sensors);
    room.append(edit);
    room.append(del);


    return room;
}