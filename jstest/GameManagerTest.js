/**
 * Created by dnwiebe on 3/5/16.
 */

describe ("A GameManager with playing field and score display mocked", function () {
    var expectedUrl = "http://blah";
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
        scoreDisplay.winner = jasmine.createSpy ("winner");
        webSocket = {};
        webSocket.send = jasmine.createSpy ("send");
        spyOn (Utils, "websocket").and.returnValue (webSocket);
        subject = GameManager (playingField, scoreDisplay, "Pudge");
    });

    describe ("instructed to start a game", function () {
        var scoreCallback = null;
        var actualUrl = null;
        var actualOpen = null;

        beforeEach (function () {
            subject.start (expectedUrl);
            var websocketArgs = Utils.websocket.calls.mostRecent ().args;
            actualUrl = websocketArgs[0];
            actualOpen = websocketArgs[1].open;
            scoreCallback = playingField.setScoreCallback.calls.mostRecent ().args[0];
        });

        it ("sets the playing field's score callback", function () {
            expect (scoreCallback).not.toBeNull ();
        });

        it ("does not pass on the instruction to the playing field", function () {
            expect (playingField.start).not.toHaveBeenCalled ();
        });

        it ("initializes the web socket", function () {
            expect (Utils.websocket).toHaveBeenCalled ();
            expect (actualUrl).toBe (expectedUrl);
        });

        describe ("passes an open handler that, when called", function () {

            beforeEach (function () {
                actualOpen ();
            });

            it ("sends a join request to the back end", function () {
                expect (webSocket.send).toHaveBeenCalledWith ("joinRequest", {name: "Pudge"});
            });
        });

        describe ("and being in receipt of an invitation", function () {
            var websocketCallbacks = null;

            beforeEach(function () {
                websocketCallbacks = Utils.websocket.calls.mostRecent().args[1].events;
                websocketCallbacks["invitation"] (1234, "Pudge");
            });

            it("passes on the instruction to the playing field", function () {
                expect(playingField.start).toHaveBeenCalled();
            });

            describe("and, during the ensuing game", function () {
                var scoreHandler = null;
                var winnerHandler = null;

                beforeEach(function () {
                    scoreHandler = websocketCallbacks["progress"];
                    winnerHandler = websocketCallbacks["winner"];
                });

                describe("directed to report a set of scores by the back end", function () {

                    beforeEach(function () {
                        scoreHandler([
                            {id: 1, name: "Freddie", score: 34},
                            {id: 2, name: "Billy", score: 45},
                            {id: 3, name: "Stanley", score: 56}
                        ]);
                    });

                    it("relays the set of scores to the score display", function () {
                        expect(scoreDisplay.showScores).toHaveBeenCalledWith([
                            {id: 1, name: "Freddie", score: 34},
                            {id: 2, name: "Billy", score: 45},
                            {id: 3, name: "Stanley", score: 56}
                        ]);
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

                describe("informed of a winner", function () {

                    beforeEach(function () {
                        winnerHandler({id: 123, name: "Moochie"});
                    });

                    it("stops the playing field", function () {
                        expect(scoreDisplay.winner).toHaveBeenCalledWith(123, "Moochie");
                        expect(playingField.stop).toHaveBeenCalled ();
                    });
                });
            });
        });
    });
});
