<html>

<head>
    <title>Hello</title>
    <style>
        <#include "style.css">
        li {
            list-style: disc;
            color: white;
            border: 0px !important;
        }
    </style>
</head>

<body>

<div class="center">
    <label>Pick a core, any core</label>
</div>

<ul>
<#list cores as core>
    <li class="juicy"><a href="${core.path}">${core.path}</a></li>
</#list>
</ul>

</body>

</html>