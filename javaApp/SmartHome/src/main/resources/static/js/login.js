
$(document).ready(function () {
    $("#subbutton").click(function () {
        var username = $("#login").val();
        var password = $("#pass").val();
         var urle = window.location.href;

        if (urle.search("admin")>0 )
            urle = "/admin/api/login";
        else
            urle = "/api/login"
        if (username != "" && password != "") {
            $.ajax({
                url: urle,
                type: 'post',
                data: { nick: username, pass: password , link: link},
                success: function (response) {
                    console.log(response);
                    
                    $("#err-msg").html(response);
                    var msg = "";
                    if (response.error == null) {
                        window.location = response.obj;
                    } else {
                        msg = "Podano błędny login lub hasło";
                        $("#err-msg").html(msg);
                        $("#err-msg").show( "bounce", {}, 1000, function(){hideAfter(this, 10000)} );
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