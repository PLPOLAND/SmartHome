$(document).ready(function () {
    $("#clear").click(function() {
        clear();
    })
    $("#save").click(function() {
        save();
    })
    $("input[name=email]").change(function() {
        if(!isEmail($(this).val())){
            $(this).addClass("invalid");
        }
        else{
            $(this).removeClass("invalid");
        }
    });
    
});
function isEmail(email) {
    var regex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
    return regex.test(email);
}
function save() {

    var url = "api/addUser";
    var isAnyEmpty = false;

    $("input").each(function () {
        if($(this).val()==""){
            isAnyEmpty = true;
        }
    })

    if (isAnyEmpty == true) {
        $("#err-msg").html("Najpierw uzupełnij pozostałe puste pola");
        $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 5000) });
        return;    
    }
    else if (isEmail($("input[name=email]").val()) != true) {
        $("#err-msg").html("Email jest nie prawidłowy!");
        $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 5000) });
        return;    
    }
    else if ($("input[name=password]").val()!= $("input [name=rePassword]").val()) {
        $("#err-msg").html("Hasła się nie zgadzają!");
        $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 5000) });
        return;    
    }

    $.ajax({
        url: url,
        type: 'get',
        
        data: { 
            name: $("#name").val(),
            surname: $("#surname").val(),
            nick: $("#nick").val(),
            email: $("input[name=email]").val(),
            pass: $("#password").val(),
            admin: $("#admin").prop('checked')==true?"true":"false",
            color: $("#color").val()
        },
        success: function (response) {
            // console.log(response);

            // $("#err-msg").html(response);
            if (response.error == null) {
                console.log(response.obj);
                $("#msg").html(response.obj);
                $("#msg").show("bounce", {}, 1000, function () { hideAfter(this, 5000) });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
}
function clear() {
    $("input").val("")
}