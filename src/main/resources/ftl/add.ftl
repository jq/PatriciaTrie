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

<div class="center">
    <form method="POST" action="/add">
        <label for="t">Add Some Stuff</label>
        <em>Put each string on its own line</em>

        <div>
            <textarea id="t" cols="75" rows="10" name="t"></textarea>
            <p>
                <input type="submit" />
            </p>
        </div>
    </form>
</div>


</body>

</html>