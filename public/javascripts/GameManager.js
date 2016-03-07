/**
 * Created by dnwiebe on 3/5/16.
 */

var GameManager = function (playingField, scoreDisplay) {

    var webSocket = null;

    var scoreCallback = function (scoreIncrement) {
        webSocket.send ("score", {increment: scoreIncrement})
console.log ("sent score increment of " + scoreIncrement + " to back end");
    };

    var reportProgress = function (scores) {
console.log ("received score update: " + scores);
        scoreDisplay.showScores (scores);
    };

    var stopHandler = function () {
console.log ("received stop directive");
        playingField.stop ();
    };

    var self = {};

    self.start = function (url) {
        webSocket = Utils.websocket (url, {events: {
            progress: reportProgress,
            stop: stopHandler
        }});
        playingField.setScoreCallback (scoreCallback);
        playingField.start ();
    };

    return self;
};
