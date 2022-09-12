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
    getUser();
});

function getUser() {
    if (id<0) {
        alert("Błędne ID usera");
    }
    else{
        $.ajax({
            url: "/admin/api/getUser",
            type: 'get',

            data: {
                id: id
            },
            success: function (response) {
                // console.log(response);

                // $("#err-msg").html(response);
                if (response.error == null) {
                    console.log(response.obj);
                    
                    $("#name").val(response.obj.imie)
                    $("#surname").val(response.obj.nazwisko)
                    $("#nick").val(response.obj.nick)
                    $("input[name=email]").val(response.obj.email)
                    $("#password").val("xxx")
                    $("#admin").prop('checked', response.obj.uprawnienia.admin)
                    $("#color").val(response.obj.opcje.themeSciezka)


                } else {
                    $("#err-msg").html(response.error);
                    $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
                }
            }
        });
    }
}


function isEmail(email) {
    var regex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
    return regex.test(email);
}
function save() {
    $("#err-msg").click()
    $("#msg").click()
    
    const url = "/admin/api/editUser";
    var isAnyEmpty = false;

    $("input").each(function () {
        if ($(this).val() == "" || $(this).val() == null){
            isAnyEmpty = true;
        }
    })
    // if($("#color").val() == null){
    //     $("#err-msg").html("Wybierz kolor interfejsu!");
    //     $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 5000) });
    //     return; 
    // }

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
    

    $.ajax({
        url: url,
        type: 'get',
        
        data: { 
            id:id,
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