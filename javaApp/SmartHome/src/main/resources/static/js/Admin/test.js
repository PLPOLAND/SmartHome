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
                    menu.append(createElement(element.zawartosc,element.link))
                });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show('slow');
            }
        }
    });

});

function createElement(opis, link) {
    var element = $('<a href ="' + link + '"><li>' + opis +'</li>');
    return element;
}