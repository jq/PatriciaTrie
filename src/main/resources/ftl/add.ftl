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

        <div>
            <textarea name="t" id="t" class="juicy" cols="75" rows="6"></textarea>

            <p>
                <input type="submit" class="juicy"/>
            </p>
        </div>
    </form>
</div>


</body>

</html>