<html>

<head>
    <title>Give Patricia a Spin</title>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
    <link rel="stylesheet" href="http://code.jquery.com/ui/1.9.2/themes/base/jquery-ui.css"/>
    <style>
        <#include "style.css">
    </style>

    <script>
        <#include "jquery-ui.js">

        $(function () {
            $("#s").autocomplete({
                source: "${core.apiUrl}",
                autoFocus: true,
                delay: 10,
                minLength: 1,
                select: function (event, ui) {
                    alert(ui.item ?
                            "Selected: " + ui.item.value :
                            "Nothing selected, input was " + this.value);
                },
                close: function (event, ui) {
                    $("#s").removeClass("autoOpen");
                },
                open: function (event, ui) {
                    $("#s").addClass("autoOpen");
                }
            });
        });

        function addString(frm) {
            if (frm.s.value) {
                $.ajax({
                    type: "PUT",
                    url: "${core.apiUrl}",
                    data: {s: frm.s.value}
                }).done(
                    function (msg) {
                        alert(frm.s.value + " queued for addition.");
                    }
                );
            }

            return false;
        }
    </script>
</head>

<body>

<div class="center">
    <form method="GET" action="#" onsubmit="return addString(this)">
        <label for="s">Auto-Complete: ${core.contextPath}</label>

        <div>
            <input id="s" type="text" name="s" class="juicy"/>
        </div>
    </form>
</div>

<#include "footer.ftl">
</body>

</html>