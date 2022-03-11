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

$(document).ready(function () {
    $.ajax({
        url: "/api/menu/usrNickName",
        type: 'post',
        data: {},
        success: function (response) {
            // console.log(response);

            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                var menu = $("#nickname");
                menu.html(response.obj);
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show('slow');
            }
        }
    });

});

$(document).ready(function () {
    $.ajax({
        url: "/api/menu/usrAvatar",
        type: 'post',
        data: {},
        success: function (response) {
            // console.log(response);

            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                var menu = $("#userAvatar");
                menu.attr("src", response.obj);
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show('slow');
            }
        }
    });


    //Dodawanie pozycji w menu pod miniaturką usera
    var el;
    el = $('<ul><li><a href="./userSettings">Ustawienia</a></li></ul>');
    $('#userLi').append(el);
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