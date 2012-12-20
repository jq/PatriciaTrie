<html>

<head>
    <title>Hello</title>
    <style>
        <#include "style.css">
    </style>
</head>

<body>

<div class="center">
    <label>Pick a core, any core</label>
</div>

<ul>
<#list cores as core>
    <li class="juicy"><a href="${core.contextPath}">${core.contextPath}</a></li>
</#list>
</ul>

</body>

</html>