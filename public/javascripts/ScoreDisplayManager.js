/**
 * Created by dnwiebe on 3/4/16.
 */

var ScoreDisplayManager = function (div, maxScore, colors) {

    var curDivWidth = 1;
    var curBarHeight = 1;

    function dePx (dimensionString) {
        var noPxString = dimensionString.substring (0, dimensionString.length - 2);
        return parseInt (noPxString);
    }

    function foregroundColor (backgroundColor) {
        var rgb = parseInt (backgroundColor.slice (1), 16);
        var r = (rgb & 0xFF0000) >> 16;
        var g = (rgb & 0x00FF00) >> 8;
        var b = (rgb & 0x0000FF);
        var grayscalex100 = (21 * r) + (72 * g) + (7 * b);
        return (grayscalex100 > 12750) ? "#000000" : "#FFFFFF";
    }

    function flash (flashes, winnerBar) {
        if (flashes.length === 0) {return;}
        winnerBar.style.backgroundColor = flashes.shift ();
        setTimeout (flash, 200, flashes, winnerBar);
    }

    function configureNewScoreBar (element, i, playerState, color) {
        element.style.left = "0px";
        element.style.top = (curBarHeight * i) + "px";
        element.style.height = (curBarHeight) + "px";
        configureExistingScoreBar (element, playerState);
        element.style.backgroundColor = color;
        element.style.color = foregroundColor (color);
        element.innerHTML = playerState.name;
        element.setAttribute ("id", "score-" + playerState.id);
        element.setAttribute ("class", "score-bar");
    }

    function configureExistingScoreBar (element, playerState) {
        element.style.width = Math.floor (playerState.score * curDivWidth / maxScore) + "px";
    }

    function modifyExistingScores (playerStates) {
        playerStates.forEach (function (playerState, i) {
            var element = div.childNodes[i];
            configureExistingScoreBar (element, playerState);
        });
    }

    function showNewScores (playerStates) {
        while (div.firstChild) {div.removeChild (div.firstChild);}
        playerStates.forEach (function (playerState, i) {
            var color = colors[i % colors.length];
            var element = document.createElement ("DIV");
            div.appendChild (element);
            configureNewScoreBar (element, i, playerState, color);
        });
    }

    var self = {};

    self.showScores = function (playerStates) {
        curDivWidth = dePx (div.style.width);
        curBarHeight = Math.floor (dePx (div.style.height) / playerStates.length);
        if (playerStates.length === div.childNodes.length) {
            modifyExistingScores (playerStates);
        }
        else {
            showNewScores (playerStates);
        }
    };

    self.winner = function (id, name) {
        var winnerBar;
        for (var i = 0; i < div.childNodes.length; i++) {
            if (div.childNodes[i].getAttribute ("id") === "score-" + id) {winnerBar = div.childNodes[i];}
        }
        if (!winnerBar) {return;}
        var onColor = winnerBar.style.backgroundColor;
        var offColor = foregroundColor(onColor);
        var flashes = [offColor, onColor, offColor, onColor, offColor, onColor, offColor, onColor, offColor, onColor];
        flash (flashes, winnerBar);
    };

    return self;
};
