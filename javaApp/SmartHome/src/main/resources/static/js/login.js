$(document).ready(function () {
    $("#subbutton").click(function () {
        var username = $("#login").val();
        var password = $("#pass").val();
        
        if (username != "" && password != "") {
            $.ajax({
                url: '/api/login',
                type: 'post',
                data: { nick: username, pass: password },
                success: function (response) {
                    console.log(response);
                    
                    $("#err-msg").html(response);
                    var msg = "";
                    if (response != "") {
                        window.location = response;
                    } else {
                        msg = "Podano błędny login lub hasło";
                        $("#err-msg").html(msg);
                        $("#err-msg").show('slow');
                        $("#login").val("");
                        $("#pass").val("");
                    }
                }
            });
        }
    });
    $(".login-container").click(function () {
        $("#err-msg").hide('slow');
    })
    $("#err-msg").hide();
});