/**
 * Created by dnwiebe on 3/3/16.
 */

var PlayingFieldManager = function (div, target) {
    var scoreCallback = function () {};
    var timeout = null;

    function clickHandler (event) {
        var targetRect = target.getBoundingClientRect ();
        var x = event.clientX - targetRect.left - 50;
        var y = event.clientY - targetRect.top - 50;
        var distance = Math.sqrt ((x * x) + (y * y));
        var score = 10 - Math.floor ((distance - 1.0) / 5);
        if (score > 0) {
            scoreCallback(score);
            clearTimeout(timeout);
            moveTarget();
        }
    }

    function moveTarget () {
        var divRect = div.getBoundingClientRect ();
        var targetRect = target.getBoundingClientRect ();
        var availableWidth = divRect.width - targetRect.width;
        var availableHeight = divRect.height - targetRect.height;
        var x = self.rng () * availableWidth;
        var y = self.rng () * availableHeight;
        target.style.position = "relative";
        target.style.visibility = "visible";
        target.style.left = x.toFixed (0) + 'px';
        target.style.top = y.toFixed (0) + 'px';
        timeout = setTimeout (moveTarget, 1000);
    }

    var self = {};

    self.rng = function () {return Math.random ();};

    self.start = function () {
        target.addEventListener ("click", clickHandler, false);
        moveTarget ();
    };

    self.stop = function () {
        clearTimeout (timeout);
        target.removeEventListener ("click", clickHandler, false);
        target.style.visibility = "hidden";
    };

    self.setScoreCallback = function (callback) {
        scoreCallback = callback;
    };

    return self;
};
