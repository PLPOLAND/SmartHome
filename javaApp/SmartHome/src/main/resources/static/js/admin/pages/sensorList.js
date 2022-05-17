$(document).ready(function () {
    $.ajax({
        url: "/admin/api/getSensors",
        type: 'post',
        data: {},
        success: function (response) {
            if (response.error == null) {
                console.log(response.obj);
                var list = $(".list");
                response.obj.sort(compareSensors)
                response.obj.forEach(element => {
                    list.append(makeFullSensor(element));
                });
            } else {
                $("#err-msg").html(response.error);
                $("#err-msg").show("bounce", {}, 1000, function () { hideAfter(this, 10000) });
            }
        }
    });
});

function compareSensors(a,b) {
    return a.typ.localeCompare(b.typ);
}