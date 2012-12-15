<html>

<head>
    <title>Give Patricia a Spin</title>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
    <link rel="stylesheet" href="http://code.jquery.com/ui/1.9.2/themes/base/jquery-ui.css"/>
    <style>
        <#include "style.css">
    </style>

    <style>
        strong {
            display: inline-block;
            width: 105px;
            text-align: right;
        }
    </style>
</head>

<body>

<form method="POST" action="/add">
    <div class="center">
        <label>Status</label>

        <code>
            <div>
                <strong>total strings:</strong> ${size}
            <#if firstKey ??>
                <strong>first key:</strong> ${firstKey}
            </#if>
            <#if lastKey ??>
                <strong>last key:</strong> ${lastKey}
            </#if>
                <hr>
                <strong>up:</strong> ${upAgo} (${upSec} seconds)
                <strong>since:</strong> ${upDate}
            </div>
        </code>
    </div>
</form>

<#include "footer.ftl">
</body>

</html>