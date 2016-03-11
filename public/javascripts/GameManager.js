/**
 * Created by dnwiebe on 3/5/16.
 */

var GameManager = function (playingField, scoreDisplay, playerName) {

    var webSocket = null;
    var id = null;

    var handleInvitation = function (invitation) {
        id = invitation.id;
        playingField.start ();
    };

    var scoreCallback = function (scoreIncrement) {
        webSocket.send ("score", {increment: scoreIncrement})
    };

    var reportProgress = function (scores) {
        scoreDisplay.showScores (scores);
    };

    var winnerHandler = function (data) {
        playingField.stop ();
        scoreDisplay.winner (data.id, data.name);
    };

    var self = {};

    self.start = function (url) {
        webSocket = Utils.websocket (url, {
            events: {
                invitation: handleInvitation,
                progress: reportProgress,
                winner: winnerHandler
            },
            open: function () {
                webSocket.send ("joinRequest", {"name": playerName});
            }
        });
        playingField.setScoreCallback (scoreCallback);
    };

    return self;
};
