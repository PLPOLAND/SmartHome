$(document).ready(function () {
    $('.device').hover(function() {
            $(this).css('background-color', 'var(--secondaryColor)');
            $(this).children().first().css('background-color', 'var(--secondaryLightColor)');
        },function() {
            $(this).css('background-color', 'var(--primaryColor)');
        $(this).children().first().css('background-color', 'var(--primaryLightColor)');
        })
});