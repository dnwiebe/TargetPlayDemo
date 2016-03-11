/**
 * Created by dnwiebe on 3/3/16.
 */

describe ("A PlayingFieldManager with a div and a mock RNG", function () {
    var div = null;
    var target = null;
    var subject = null;
    var score = 0;

    beforeEach (function () {
        jasmine.clock ().install ();
        makeDocument ();
        div = document.getElementById ("sample-div");
        target = document.getElementById ("sample-target");
        subject = PlayingFieldManager (div, target);
        subject.setScoreCallback (function (points) {score += points;});
        var numbers = [0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0,
            1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.0];
        subject.rng = function () {return numbers.shift ();}
    });

    afterEach (function () {
        removeDocument ();
        jasmine.clock ().uninstall ();
    });

    describe ("instructed to start", function () {
        beforeEach (function () {
            subject.start ();
        });

        it ("places the target at (100, 200)", function () {
            expect (target.style.position).toBe ("relative");
            expect (target.style.visibility).toBe ("visible");
            expect (target.style.left).toBe ("100px");
            expect (target.style.top).toBe ("200px");
        });

        describe ("and clicked in the center of the target", function () {
            beforeEach (function () {
                var event = new MouseEvent ("click", {
                    clientX: 50,
                    clientY: 50,
                    buttons: 1
                });
                target.dispatchEvent (event);
            });

            it ("adds eleven points to the score and moves the target", function () {
                expect (score).toBe (11);
                expect (target.style.position).toBe ("relative");
                expect (target.style.visibility).toBe ("visible");
                expect (target.style.left).toBe ("300px");
                expect (target.style.top).toBe ("400px");
            });
        });

        describe ("and clicked in various places on the target", function () {
            var parameterSets = [
                {oclock: 12, distance: 0, expectedScore: 11, moveTarget: true},
                {oclock: 1, distance: 5, expectedScore: 10, moveTarget: true},
                {oclock: 3, distance: 6, expectedScore: 9, moveTarget: true},
                {oclock: 3, distance: 10, expectedScore: 9, moveTarget: true},
                {oclock: 6, distance: 11, expectedScore: 8, moveTarget: true},
                {oclock: 5, distance: 15, expectedScore: 8, moveTarget: true},
                {oclock: 6, distance: 16, expectedScore: 7, moveTarget: true},
                {oclock: 7, distance: 20, expectedScore: 7, moveTarget: true},
                {oclock: 8, distance: 21, expectedScore: 6, moveTarget: true},
                {oclock: 9, distance: 25, expectedScore: 6, moveTarget: true},
                {oclock: 9, distance: 26, expectedScore: 5, moveTarget: true},
                {oclock: 10, distance: 30, expectedScore: 5, moveTarget: true},
                {oclock: 12, distance: 31, expectedScore: 4, moveTarget: true},
                {oclock: 12, distance: 35, expectedScore: 4, moveTarget: true},
                {oclock: 12, distance: 36, expectedScore: 3, moveTarget: true},
                {oclock: 2, distance: 40, expectedScore: 3, moveTarget: true},
                {oclock: 3, distance: 41, expectedScore: 2, moveTarget: true},
                {oclock: 4, distance: 45, expectedScore: 2, moveTarget: true},
                {oclock: 5, distance: 46, expectedScore: 1, moveTarget: true},
                {oclock: 6, distance: 50, expectedScore: 1, moveTarget: true},
                {oclock: 7, distance: 51, expectedScore: 0, moveTarget: false},
                {oclock: 4, distance: 60, expectedScore: 0, moveTarget: false}
            ];

            parameterSets.forEach (function (parameterSet) {
                checkScore (parameterSet.oclock, parameterSet.distance, parameterSet.expectedScore,
                    parameterSet.moveTarget);
            });

            function checkScore (oclock, distance, expectedScore, moveTarget) {
                it ("scores " + expectedScore + " at " + distance + " pixels from target center at " + oclock +
                    " o'clock and " + (moveTarget ? "moves" : "leaves") + " target", function () {
                    var hit = toRect(oclock, distance);
                    var event = new MouseEvent("click", {
                        clientX: hit.x,
                        clientY: hit.y,
                        buttons: 1
                    });
                    score = 0;
                    var beforeX = target.style.left;
                    var beforeY = target.style.top;

                    target.dispatchEvent(event);

                    expect(score).toBe(expectedScore);
                    if (moveTarget) {
                        expect(target.style.left).not.toEqual(beforeX);
                        expect(target.style.top).not.toEqual(beforeY);
                    }
                    else {
                        expect(target.style.left).toEqual(beforeX);
                        expect(target.style.top).toEqual(beforeY);
                    }
                });
            }
        });

        describe ("and not clicked for a second", function () {
            var beforeX = null;
            var beforeY = null;

            beforeEach (function () {
                beforeX = target.style.left;
                beforeY = target.style.top;
                score = 0;
                jasmine.clock ().tick (1000);
            });

            it ("moves the target anyway", function () {
                expect(score).toBe(0);
                expect(target.style.left).not.toEqual(beforeX);
                expect(target.style.top).not.toEqual(beforeY);
            });
        });

        describe ("and instructed to stop", function () {

            beforeEach (function () {
                subject.stop ();
            });

            it ("makes the target invisible", function () {
                expect (target.style.visibility).toBe ("hidden");
            });

            describe ("and the target is clicked", function () {
                var beforeScore;
                var beforeLeft;
                var beforeTop;

                beforeEach (function () {
                    var event = new MouseEvent ("click", {
                        offsetX: 50,
                        offsetY: 50,
                        buttons: 1
                    });
                    beforeScore = 0;
                    beforeLeft = target.style.left;
                    beforeTop = target.style.top;
                    target.dispatchEvent (event);
                });

                it ("no score is reported", function () {
                    expect (score).toBe (beforeScore);
                });

                it ("the target is still invisible and has not moved", function () {
                    expect (target.style.visibility).toBe ("hidden");
                    expect (target.style.left).toBe (beforeLeft);
                    expect (target.style.top).toBe (beforeTop);
                });
            });
        });
    });

    function makeDocument () {
        var div = document.createElement ("DIV");
        div.innerHTML =
            '<div id="sample-div" style="width:1100px; height:1100px; background-color:#202020;">\n' +
            '  <img id="sample-target" src="../../public/images/target.png">\n' +
            '</div>\n';
        var body = document.getElementsByTagName ("BODY")[0];
        body.appendChild (div);
    }

    function removeDocument () {
        var div = document.getElementById ("sample-div");
        div.parentNode.removeChild (div);
    }

    function toRect (oclock, distance) {
        var degreesCWFromTop = 30 * oclock;
        var degreesCCWFromRight = 90 - degreesCWFromTop;
        var radians = degreesCCWFromRight * 2.0 * Math.PI / 360.0;
        var x = Math.sin (radians) * distance;
        var y = Math.cos (radians) * distance;
        return {x: x + 50, y: y + 50};
    }
});
