<html>

<head>
    <title>Add Strings: ${core.path}</title>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
    <link rel="stylesheet" href="http://code.jquery.com/ui/1.9.2/themes/base/jquery-ui.css"/>
    <style>
        <#include "style.css">
    </style>
</head>

<body>

<form method="POST" action="${core.addUrl}">
    <div class="center">
        <label for="t">Add Strings: ${core.path}</label>
    </div>

    <div>
        <#if success??>
            <#if success == true>
                <code><div><strong>Success!!!</strong><br /><br />${resultJson}</div></code>
            <#else>
                <code><div><span class="error">${error}</span></div></code>
            </#if>
        </#if>


        <textarea name="t" id="t" class="juicy stdtxt" rows="10"></textarea>

        <p>
            <input type="submit" class="juicy" value="Submit"/>
        </p>
    </div>
</form>

<#include "footer.ftl">
</body>

</html>