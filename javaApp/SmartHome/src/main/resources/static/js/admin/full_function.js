function makeFullFunction(obj, rooms) {
    var fun = $("<div class=\"fullFunction\"></div>");

    var id = $("<div class=\"id\"></div>");
    id.append(obj.id);

    var name = $("<div class=\"name\"></div>");
    name.append(obj.name);

    var actions = $("<div class=\"actions\"></div>");
    actions.append("Akcji: " + obj.actions.length);
    
    var stan = $("<div class=\"stan\"></div>");
    stan.append(obj.active == true ? "Stan: Aktywny" : "Stan: Nieaktywny");

    fun.append(id);
    fun.append(name);
    fun.append(actions);
    fun.append(stan);

    switch (obj.type) {
        case "BUTTON":
            var button = $("<div class=\"button\"></div>");
            button.append("Przycisk: " + rooms[obj.button.room] + " - " + obj.button.name);
            fun.append(button);
            var clicks = $("<div class=\"clicks\"></div>");
            clicks.append("Przyciśnięć: " + obj.clicks);
            fun.append(clicks);
            var clickType = $("<div class=\"clickType\"></div>");
            clickType.append("Rodzaj Przyciśnięcia: " + obj.clickType);
            fun.append(clickType);
            break;
        case "AUTOMATION":
            var conditions = $("<div class=\"conditions\">Warunki: "+obj.conditions.length+"</div>");
            var oneWay = $("<div class=\"oneWay\">OneWay: "+obj.oneWay == true ? "TAK":"NIE"+"</div>");
            fun.append(conditions);
            fun.append(oneWay);
            break;
        case "USER":
            var room = $("<div class=\"room\">Pokoj: "+obj.room.name+"</div>");
            var user = $("<div class=\"user\">Użytkownik: "+obj.user.name+"</div>"); //TODO sprawdzić
            var private = $("<div class=\"private\">Prywatna: "+obj.private == true ? "TAK":"NIE"+"</div>");
            fun.append(room);
            fun.append(user);
            fun.append(private);
            break;
    
        default:
            break;
    }


    var deleteMe = $('<div class="icon" title="Usuń"><i class="icon-trash"></i></div>');
    var editMe = $('<div class="icon" title="Edytuj"><i class="icon-sliders"></i></div>');

    editMe.click(function () {
        // //TODO
        document.location.href = "/admin/editFunction?id=" + obj.id;
    })

    deleteMe.click(function () {
        //TODO
        // document.location.href = "/admin/api/removeDeviceByID?id=" + obj.id;
        $.ajax({
            url: "/admin/api/removeFunctionByID",
            type: 'get',
            data: {
                id: obj.id,
            },
            success: function (response) {
                // console.log(response);

                // $("#err-msg").html(response);
                if (response.error == null) {
                    console.log(response.obj);
                    $("#msg").html(response.obj);
                    $("#msg").show("bounce", {}, 1000, function () { hideAfter(this, 5000) });
                    fun.remove();
                } else {
                    $("#err-msg").html(response.error);
                    $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
                }
            }
        });
    })

    fun.append(deleteMe);
    fun.append(editMe);

    return fun;

}