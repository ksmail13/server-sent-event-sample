var EventReceiver = (function () {

    function log(msg) {
        if (console.log) {
            console.log(msg);
        }
    }

    var xhrPoll = function (url, option) {
        function newXhrObject() {
            if (window.XMLHttpRequest) {
                // code for modern browsers
                return new XMLHttpRequest();
            } else {
                // code for old IE browsers
                return new ActiveXObject("Microsoft.XMLHTTP");
            }
        }

        function init() {
            var request = newXhrObject();
            request.open("POST", url, true);
            return request;
        }

        return {
            then: function (callback) {
                var request = init();
                request.onreadystatechange = function stateCallback() {
                    if (this.readyState !== 4) {
                        return;
                    }
                    if (this.status / 100 === 2) {
                        var event = {};
                        try {
                            event = JSON.parse(this.responseText);
                        } catch (e) {
                            var bodyList = this.responseText.split('\n');
                            for (var i = 0; i < bodyList.length; i++) {
                                var kv = bodyList[i];
                                log(kv);
                                if (kv.indexOf('data:') !== 0) {
                                    continue;
                                } else {
                                    event = JSON.parse(kv.replace('data:', ''));
                                    break;
                                }
                            }
                        }

                        if (event.event !== 'ping') {
                            callback(event);
                        } else if (this._ping) {
                            this._ping(event);
                        } else {
                            log("receive ping in xhrPoll " + event.time);
                        }

                        var req = init();
                        req.onreadystatechange = stateCallback;
                        req.send();
                    } else {
                        var errData = {
                            code: this.status,
                            value: this.responseText
                        };
                        if (this._error) {
                            this._error(errData);
                        } else {
                            log("event request fail in xhrPoll " + errData.code + " / " + errData.value);
                        }
                    }
                }
                request.send();
                return this;
            },
            ping: function (handler) {
                this._ping = handler;
                return this;
            },
            error: function (handler) {
                this._error = handler;
                return this;
            },
            close: function (){}
        };
    };

    var eventPoll = function (url, option) {
        "use strict";

        var eventReceiver = null;
        var pingHandler = null;
        var errorHandler = null;
        return {
            then: function (callback) {
                eventReceiver = new EventSource(url, option);

                eventReceiver.onmessage = function (e) {
                    log("triggered session " + e);
                    var parsed = JSON.parse(e.data);
                    callback(parsed);
                };

                eventReceiver.addEventListener("ping", function (e) {
                    var parsed = JSON.parse(e.data);
                    if (pingHandler) {
                        pingHandler(parsed);
                    } else {
                        log("receive ping in eventPoll " + parsed.time);
                    }
                });

                eventReceiver.onerror = function (e) {
                    log("event request fail in eventPoll");
                }

                return this;
            },
            ping: function (handler) {
                eventReceiver.addEventListener("ping", function (e) {
                    var parsed = JSON.parse(e.data);
                    if (handler) {
                        handler(parsed);
                    } else {
                        log("receive ping in eventPoll " + parsed.time);
                    }
                });
                return this;
            },
            error: function (handler) {
                eventReceiver.onerror = handler;
                return this;
            },
            close: function () {
                if (eventReceiver != null) {
                    eventReceiver.close();
                }
            }
        };
    };

    if (window.EventSource) {
        return eventPoll;
    } else {
        return xhrPoll;
    }
})();
