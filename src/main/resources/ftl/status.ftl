<html>

<head>
    <title>Give Patricia a Spin</title>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
    <link rel="stylesheet" href="http://code.jquery.com/ui/1.9.2/themes/base/jquery-ui.css"/>
    <style>
        <#include "style.css">
    </style>
</head>

<body>

<form method="POST" action="/add">
    <div class="center">
        <label>Status</label>

        <code>
            <div>
                <strong>${size}</strong> keys
            </div>
        </code>
    </div>
</form>

<#include "footer.ftl">
</body>

</html>