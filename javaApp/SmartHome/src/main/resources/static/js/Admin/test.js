$(document).ready(function () {
    $.ajax({
        url: "/api/menu/pozycje",
        type: 'post',
        data: {},
        success: function (response) {
            // console.log(response);

            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                var menu = $("#topMenu");
                response.obj.forEach(element => {
                    if (element.dropdown == true) {
                        menu.append(createRozwijaneMenu(element.dropdownMenu, element.zawartosc, element.link));
                    } else {
                        menu.append(createElement(element.zawartosc,element.link));
                    }
                });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show('slow');
            }
        }
    });

});

function createElement(opis, link) {
    var element = $('<li><a href ="' + link + '">' + opis +'</a></li>'); 
    return element;
}
function createRozwijaneMenu(tab , opis, link) {
    var menu = '<li><a href ="' + link + '">' + opis +'</a><ul>';
    tab.forEach(element => {
        menu += '<li><a href ="' + element.link + '">' + element.zawartosc +'</a></li>';
    });
    menu += "</ul></li>";
    return $(menu);
}