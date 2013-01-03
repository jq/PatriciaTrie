<html>

<head>
    <title>Status: ${core.path}</title>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
    <link rel="stylesheet" href="http://code.jquery.com/ui/1.9.2/themes/base/jquery-ui.css"/>
    <style>
        <#include "style.css">
    </style>

    <style>
        th {
            width: 105px;
            text-align: right;
            padding-right: 20px;
        }

        td, th, textarea {
            vertical-align: top;
            font-family: monospace;
        }

        hr {
            margin: 10px;
        }

        table {
            width: 100%;
            padding: 20px;
        }

        textarea {
            width: 98%;
        }
    </style>
</head>

<body>

<form method="POST" action="/add">
    <div class="center">
        <label>Status: ${core.path}</label>

        <div class="center" style="background-color: white">
            <table width="100%">
                <tr>
                    <th># strings:</th>
                    <td>${size}</td>
                </tr>
            <#if firstKey ??>
                <tr>
                    <th>first key:</th>
                    <td>${firstKey}</td>
                </tr>
            </#if>
            <#if lastKey ??>
                <tr>
                    <th>last key:</th>
                    <td>${lastKey}</td>
                </tr>
            </#if>
                <tr>
                    <td colspan="2">
                        <hr/>
                    </td>
                </tr>
                <tr>
                    <th>up:</th>
                    <td>${upAgo} (${upSec} seconds)</td>
                </tr>
                <tr>
                    <th>since:</th>
                    <td>${upDate}</td>
                </tr>
                <tr>
                    <td colspan="2">
                        <hr/>
                    </td>
                </tr>
                <tr>
                    <th>config file:</th>
                    <td><textarea rows="10" cols="60" readonly="true">${configFile!}</textarea></td>
                </tr>
                <tr>
                    <th>config:</th>
                    <td><textarea rows="20" cols="60" readonly="true">${config}</textarea></td>
                </tr>
                </tbody>
            </table>
        </div>
        <p><br></p>
    </div>
</form>

<#include "footer.ftl">
</body>

</html>