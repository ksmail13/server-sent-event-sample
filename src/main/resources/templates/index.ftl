<!Doctype html>
<html lang="ko">
<head>
    <title>Server push test</title>
    <style>
        .border {
            border: 1px solid #000;
        }

        .scrollable {
            overflow-x: auto;
            overflow-y: scroll;
        }

        #console {
            width: 100%;
            height: 200px;
        }
    </style>
</head>
<body>
<h1>Server push send test</h1>
<hr/>
<div id="console" class="border scrollable">

</div>
</body>
<script type="application/javascript" src="/js/EventReceiver.js"></script>
<script type="application/javascript" src="/js/test.js"></script>
<script>
    var app = Test('console');
    window.addEventListener("load", app.init);
</script>
</html>