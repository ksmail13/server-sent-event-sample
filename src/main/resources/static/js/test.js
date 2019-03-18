var Test = function (panelId) {
    var nativeEventSource = window.EventSource !== undefined;
    var panel = document.getElementById(panelId);

    if (panel == null) {
        alert("panel not found");
        return null;
    }

    function loggingPanel(msg) {
        if (!panel.innerHTML) {
            panel.innerHTML = '';
        }
        panel.innerHTML = panel.innerHTML + msg + "<br>";
    }

    return {
        init: function () {
            loggingPanel("init");
            loggingPanel('event source is ' + (nativeEventSource ? "enable" : 'disable'));

            var receiver = new EventReceiver('/push?session=test');
            receiver.then(function (data){
                loggingPanel('data: ' + JSON.stringify(data));
            }).ping(function (ping) {
                loggingPanel('ping: ' + ping.time);
            });
        }
    };
};