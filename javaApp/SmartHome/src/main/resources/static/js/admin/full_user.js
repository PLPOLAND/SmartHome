function makeFullUser(obj, rooms) {
    var row = $("<tr></tr>");
    row.attr("uid", obj.id);

    var img = $("<img></img>");
    img.attr("src", obj.opcje.lokalnaSciezka);
    img.attr("class", "uimg");

    var nick = $("<td></td>");
    nick.append(obj.nick);

    var imie = $("<td></td>");
    imie.append(obj.imie);

    var nazwisko = $("<td></td>");
    nazwisko.append(obj.nazwisko);

    var email = $("<td></td>");
    email.append(obj.email);

    var admin = $("<td></td>");
    admin.append("<input type='checkbox' id=\"admin\" " + (obj.uprawnienia.admin ? "checked" : "") + " disabled>");


    var deleteMe = $('<td ><i class="icon icon-trash" title="Usuń"></i></td>');
    var editMe = $('<td ><i class="icon icon-sliders" title="Edytuj"></i></td>');

    editMe.click(function () {
        document.location.href = "/admin/editUser?id=" + obj.id;
    })

    deleteMe.click(function () {
        //TODO
        // document.location.href = "/admin/api/removeDeviceByID?id=" + obj.id;
        $.ajax({
            url: "/admin/api/removeUserByID",
            type: 'get',
            data: {
                id: obj.id,
            },
            success: function (response) {
                if (response.error == null) {
                    console.log(response.obj);
                    $("#msg").html(response.obj);
                    $("#msg").show("bounce", {}, 1000, function () { hideAfter(this, 5000) });
                    deleteMe.parent().remove()
                } else {
                    $("#err-msg").html(response.error);
                    $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
                }
            }
        });
    })

    row.append(img);
    row.append(nick);
    row.append(imie);
    row.append(nazwisko);
    row.append(email);
    row.append(admin);
    row.append(editMe);
    row.append(deleteMe);
    return row;

}