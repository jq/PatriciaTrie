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
        <label for="t">Add Some Stuff</label>
    </div>

    <div>
        <#if success??>
            <#if success == true>
                <code><div><strong>Successfully added the strings.</strong><br />${resultJson}</div></code>
            <#else>
                <code><div><span class="error">${error}</span></div></code>
            </#if>
        </#if>


        <textarea name="t" id="t" class="juicy" rows="6"></textarea>

        <p>
            <input type="submit" class="juicy"/>
        </p>
    </div>
</form>


</body>

</html>