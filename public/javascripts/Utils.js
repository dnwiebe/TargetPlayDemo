/**
 * Created by dnwiebe on 3/6/16.
 */

Utils = function () {

    var self = {};

    // for internal use
    self.makeWebSocket = function (url) {
        return new WebSocket (url);
    };

    self.websocket = function (url, params) {
        var ws = {};

        var socket = self.makeWebSocket (url);

        var onMessage = function (event) {
            var msg = null;
            try {
                msg = JSON.parse (event.data);
            }
            catch (error) {
                console.log ('Discarding non-JSON WebSocket message: "' + event.data + '"');
                return;
            }
            var type = msg.type;
            var data = msg.data;
            var handler = params.events[type];
            if (!handler) {
                console.log ('Discarding unexpected WebSocket message: "' + event.data + '"');
                return;
            }
            handler (data);
        };

        socket.onopen = params.open;
        socket.onclose = params.close;
        if (params.events) {socket.onmessage = onMessage;}

        ws.send = function (type, data) {
            var msg = JSON.stringify ({type: type, data: data});
            try {
                socket.send(msg);
                return true;
            }
            catch (e) {
                console.log (e.message);
                return false;
            }
        };

        ws.replaceEvents = function (events) {
            params.events = events;
        };

        return ws;
    };

    return self;
} ();
