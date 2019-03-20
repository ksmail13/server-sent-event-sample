var Test = function (option) {
    "use strict";
    var nativeEventSource = window.EventSource !== undefined;
    var panel = document.getElementById(option.panel);
    var sessionLabel = document.getElementById(option.sessionLabel);

    if (panel == null) {
        alert("panel not found");
        return null;
    }

    function loggingPanel(msg) {
        if (!panel.innerHTML) {
            panel.innerHTML = '';
        }

        var lines = panel.innerHTML.split('<br>');
        // limit for memory save
        var take = Math.min(1000, lines.length);

        var html = '';
        for(var i = 0; i < take; i++) {
            html += lines[i] + '<br>';
        }

        panel.innerHTML = msg + "<br>" + html;
    }

    // https://gist.github.com/jcxplorer/823878
    function uuid() {
        var uuid = "", i, random;
        for (i = 0; i < 8; i++) {
            random = Math.random() * 16 | 0;

            if (i == 8 || i == 12 || i == 16 || i == 20) {
                uuid += "-";
            }
            uuid += (i == 12 ? 4 : (i == 16 ? (random & 3 | 8) : random)).toString(16);
        }
        return uuid;
    }

    return {
        init: function () {
            loggingPanel("init");
            loggingPanel('event source is ' + (nativeEventSource ? "enable" : 'disable'));
            var session = uuid();
            sessionLabel.innerHTML = session;

            var receiver = new EventReceiver('/push?session='+session);
            loggingPanel('enable session ' + session);
            receiver.then(function (data){
                loggingPanel('data('+new Date().toString()+'): ' + JSON.stringify(data));
            }).ping(function (ping) {
                loggingPanel('ping: ' + ping.time);
            }).error(function (error) {
                loggingPanel('error:' + error);
            });
        }
    };
};

window.Test = Test;

var app = new Test({panel:'console', sessionLabel:'session'});
window.addEventListener("load", app.init);