/**
 * Created by dnwiebe on 3/6/16.
 */
describe ("Utils.websocket, when its environment is mocked out", function () {
    var websocket = null;
    var mockWebSocket = null;

    beforeEach (function () {
        mockWebSocket = {send: jasmine.createSpy ()};
        spyOn (Utils, "makeWebSocket").and.returnValue (mockWebSocket);
    });

    describe ("and it is called without an events parameter", function () {

        beforeEach (function () {
            subject = Utils.websocket ("Throckmorton", {
                open: "open handler",
                close: "close handler"
            });
        });

        it ("applies the supplied open handler to the underlying WebSocket", function () {
            expect (mockWebSocket.onopen).toBe ("open handler");
        });

        it ("applies the supplied close handler to the underlying WebSocket", function () {
            expect (mockWebSocket.onclose).toBe ("close handler");
        });

        it ("leaves the underlying WebSocket's onmessage handler undefined", function () {
            expect (mockWebSocket.onmessage).toBeUndefined ();
        });

        describe ("and used to send a message", function () {

            beforeEach (function () {
                subject.send ("opcode", {name: "value"});
            });

            it ("delegates properly to the underlying WebSocket", function () {
                expect (mockWebSocket.send).toHaveBeenCalledWith (JSON.stringify ({type: "opcode", data: {name: "value"}}))
            });
        });
    });

    describe ("and it is called with just an events parameter", function () {
        var output = null;

        beforeEach (function (){
            subject = Utils.websocket ("Throckmorton", {events: {
                birthday: function (data) {output = data.message},
                christmas: function (data) {output = data.message}
            }});
        });

        it ("leaves the underlying WebSocket's open handler undefined", function () {
            expect (mockWebSocket.onopen).toBeUndefined ();
        });

        it ("leaves the underlying WebSocket's close handler undefined", function () {
            expect (mockWebSocket.onclose).toBeUndefined ();
        });

        describe ("and the underlying WebSocket receives one kind of expected message", function () {

            beforeEach (function () {
                mockWebSocket.onmessage ({data: JSON.stringify ({type: "birthday", data: {message: "Happy birthday to me!"}})});
            });

            it ("its event handler operates as expected", function () {
                expect (output).toBe ("Happy birthday to me!");
            });
        });

        describe ("and the underlying WebSocket receives another kind of expected message", function () {

            beforeEach (function () {
                mockWebSocket.onmessage ({data: JSON.stringify ({type: "christmas", data: {message: "Merry Christmas!"}})});
            });

            it ("its event handler operates as expected", function () {
                expect (output).toBe ("Merry Christmas!");
            });
        });

        describe ("and the underlying WebSocket receives a non-JSON message", function () {

            beforeEach (function () {
                spyOn (window.console, "log");
                mockWebSocket.onmessage ({data: "Nobody expects the Spanish Inquisition!"});
            });

            it ("ignores the message and logs about it", function () {
                expect (window.console.log).toHaveBeenCalledWith ('Discarding non-JSON WebSocket message: "Nobody expects the Spanish Inquisition!"');
            });
        });

        describe ("and the event handlers are replaced", function () {

            var replacementEvents = null;

            beforeEach (function () {
                replacementEvents = {
                    chanukah: function (data) {output = data.message}
                };

                subject.replaceEvents (replacementEvents);
            });

            describe ("and an event that would have been handled previously is received", function () {

                beforeEach (function () {
                    spyOn (window.console, "log");
                    mockWebSocket.onmessage ({data: JSON.stringify ({type: "christmas", data: {message: "Merry Christmas!"}})});
                });

                it ("logs about the message", function () {
                    expect (window.console.log).toHaveBeenCalledWith ('Discarding unexpected WebSocket message: "' +
                        JSON.stringify ({type: "christmas", data: {message: "Merry Christmas!"}}) + '"');
                });
            });

            describe ("and an event handled by the new event handlers is received", function () {

                beforeEach (function () {
                    mockWebSocket.onmessage ({data: JSON.stringify ({type: "chanukah", data: {message: "Happy Chanukah!"}})});
                });

                it ("calls the new handler", function () {
                    expect (output).toBe ("Happy Chanukah!");
                });
            });
        });
    });
});
