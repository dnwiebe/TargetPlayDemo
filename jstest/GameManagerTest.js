/**
 * Created by dnwiebe on 3/5/16.
 */

describe ("A GameManager with playing field and score display mocked", function () {
    var url = "http://blah";
    var subject = null;
    var playingField = null;
    var scoreDisplay = null;
    var webSocket = null;

    beforeEach (function () {
        playingField = {};
        playingField.setScoreCallback = jasmine.createSpy ("setScoreCallback");
        playingField.start = jasmine.createSpy ("start");
        playingField.stop = jasmine.createSpy ("stop");
        scoreDisplay = {};
        scoreDisplay.showScores = jasmine.createSpy ("showScores");
        webSocket = {};
        webSocket.send = jasmine.createSpy ("send");
        spyOn (Utils, "websocket").and.returnValue (webSocket);
        subject = GameManager (playingField, scoreDisplay);
    });

    describe ("instructed to start a game", function () {
        var scoreCallback = null;

        beforeEach (function () {
            subject.start (url);
            scoreCallback = playingField.setScoreCallback.calls.mostRecent ().args[0];
        });

        it ("sets the playing field's score callback", function () {
            expect (scoreCallback).not.toBeNull ();
        });

        it ("passes on the instruction to the playing field", function () {
            expect (playingField.start).toHaveBeenCalled ();
        });

        it ("initializes the web socket", function () {
            expect (Utils.websocket).toHaveBeenCalled ();
            expect (Utils.websocket.calls.mostRecent ().args[0]).toBe (url);
        });

        describe ("and, during the ensuing game", function () {
            var scoreHandler = null;
            var stopHandler = null;

            beforeEach(function () {
                var websocketCallbacks = Utils.websocket.calls.mostRecent ().args[1].events;
                scoreHandler = websocketCallbacks["progress"];
                stopHandler = websocketCallbacks["stop"];
            });

            describe ("directed to report a set of scores by the back end", function () {

                beforeEach (function () {
                    scoreHandler ([34, 45, 56]);
                });

                it ("relays the set of scores to the score display", function () {
                    expect (scoreDisplay.showScores).toHaveBeenCalledWith ([34, 45, 56]);
                });
            });

            describe("given a score by the playing field", function () {

                beforeEach(function () {
                    scoreCallback(3);
                });

                it("sends the score to the back end", function () {
                    expect(webSocket.send).toHaveBeenCalledWith("score", {increment: 3});
                });
            });

            describe("instructed to stop", function () {

                beforeEach (function () {
                    stopHandler();
                });

                it ("stops the playing field", function () {
                    expect (playingField.stop).toHaveBeenCalled ();
                });
            });
        });
    });
});
