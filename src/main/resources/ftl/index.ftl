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
                source: "${core.contextPath}/api/",
                autoFocus: true,
                delay: 10,
                minLength: 1,
                select: function (event, ui) {
                    alert(ui.item ?
                            "Selected: " + ui.item.value :
                            "Nothing selected, input was " + this.value);
                },
                close: function( event, ui ) {
                    $("#s").removeClass("autoOpen");
                },
                open: function( event, ui ) {
                    $("#s").addClass("autoOpen");
                }
            });
        });
    </script>
</head>

<body>

<div class="center">
    <form method="GET" action="/api" onsubmit="return false">
        <label for="s">Test out the Auto-Complete</label>

        <div>
            <input id="s" type="text" name="s" class="juicy"/>
        </div>
    </form>
</div>

<#include "footer.ftl">
</body>

</html>