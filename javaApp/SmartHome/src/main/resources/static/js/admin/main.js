$(document).ready(function () {
    $("#err-msg").click(function () {
        $(this).hide('blind',{},1000,function(){});
    })

    $.ajax({
        url: "/api/menu/menuGlowne",
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
                controlMenu();
                $(window).resize(function () {
                    controlMenu();
                });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show( "bounce", {}, 1000, function(){hideAfter(this, 10000)} );
            }
        }
    });

    $.ajax({
        url: "/api/menu/menuUsera",
        type: 'post',
        data: {},
        success: function (response) {
            // console.log(response);

            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                //Dodawanie pozycji w menu pod miniaturką usera
                var el;
                el = $('<ul></ul>');
                $('#userLi').append(el);
                var menu = $("#userLi ul");
                response.obj.forEach(element => {
                    if (element.dropdown == true) {
                        menu.append(createRozwijaneMenu(element.dropdownMenu, element.zawartosc, element.link));
                    } else {
                        menu.append(createElement(element.zawartosc,element.link));
                    }
                });
                controlMenu();
                $(window).resize(function () {
                    controlMenu();
                });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show( "bounce", {}, 1000, function(){hideAfter(this, 10000)} );
            }
        }
    });

    $.ajax({
        url: "/api/menu/usrNickName",
        type: 'post',
        data: {},
        success: function (response) {
            // console.log(response);

            // $("#err-msg").html(response);
            if (response.error == null) {
                var menu = $("#nickname");
                menu.html(response.obj);
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show( "bounce", {}, 1000, function(){hideAfter(this, 10000)} );
            }
        }
    });

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
                $("#err-msg").show( "bounce", {}, 1000, function(){hideAfter(this, 10000)} );
            }
        }
    });
    $.ajax({
        url: "/api/themePath",
        type: 'post',
        data: {},
        success: function (response) {
            // console.log(response);

            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                $("<link/>", {
                    rel: "stylesheet",
                    type: "text/css",
                    href: response.obj
                }).appendTo("head");
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show( "bounce", {}, 1000, function(){hideAfter(this, 10000)} );
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
function controlMenu() {
    // if ($(document).width() < 1220) {
    //     var menu = $(".menu");
    //     $("body").append(menu.first());
    // }
    // if ($(document).width() >= 1220) {
    //     var menu = $(".menu");
    //     $(".banner").first().append(menu.first());
    // }
}

function hideAfter(me, time) {
    setTimeout(function () {
        $(me).click()
    },time);
}