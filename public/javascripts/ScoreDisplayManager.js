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

    function configureScoreBar (element, i, score, color) {
        element.style.left = "0px";
        element.style.top = (curBarHeight * i) + "px";
        element.style.height = (curBarHeight) + "px";
        element.style.width = Math.floor (score * curDivWidth / maxScore) + "px";
        element.style.backgroundColor = color;
    }

    function modifyExistingScores (scores) {
        scores.forEach (function (score, i) {
            var color = colors[i % colors.length];
            var element = div.childNodes[i];
            configureScoreBar (element, i, score, color);
        });
    }

    function showNewScores (scores) {
        while (div.firstChild) {div.removeChild (div.firstChild);}
        scores.forEach (function (score, i) {
            var color = colors[i % colors.length];
            var element = document.createElement ("DIV");
            div.appendChild (element);
            configureScoreBar (element, i, score, color);
        });
    }

    var self = {};

    self.showScores = function (scores) {
        curDivWidth = dePx (div.style.width);
        curBarHeight = Math.floor (dePx (div.style.height) / scores.length);
        if (scores.length === div.childNodes.length) {
            modifyExistingScores (scores);
        }
        else {
            showNewScores (scores);
        }
    };

    return self;
};
