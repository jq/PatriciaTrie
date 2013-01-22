<html>

<head>
    <title>AutoComplete: ${core.path}</title>
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
                            ui.item.extra :
                            "Nothing selected, input was " + this.value);
                },
                close: function (event, ui) {
                    $("#s").removeClass("autoOpen");
                },
                open: function (event, ui) {
                    $("#s").addClass("autoOpen");
                },
                response: function( event, ui ) {
                    var content = ui.content;
                    var num = content.length;

                    if (num > 0) {
                        for (var i = 0; i < num; i++) {
                            ui.content[i].value = ui.content[i].label = ui.content[i].s;
                            ui.content[i].extra = "string: " + ui.content[i].s;
                            if (ui.content[i].h) {
                                ui.content[i].extra += "\nhash: " + ui.content[i].h;
                            }
                        }
                    }
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
        <label for="s">Auto-Complete: ${core.path}</label>

        <div>
            <input id="s" type="text" name="s" class="juicy"/>
        </div>
    </form>
</div>

<#include "footer.ftl">
</body>

</html>