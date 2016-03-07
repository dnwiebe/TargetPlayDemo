/**
 * Created by dnwiebe on 3/4/16.
 */

describe ("A ScoreDisplayManager with a div and a bunch of colors", function () {
    var div = null;
    var subject = null;

    beforeEach (function () {
        makeDocument ();
        div = document.getElementById ("sample-div");
        subject = ScoreDisplayManager (div, 100, ["#123456", "#234567", "#345678"]);
    });

    afterEach (function () {
        removeDocument ();
    });

    describe ("when directed to show three scores", function () {

        beforeEach (function () {
            subject.showScores ([34, 45, 56]);
        });

        it ("shows three score bars of the appropriate color, size, and position", function () {
            checkScoreBar (div.childNodes[0], "0px", "166px", "170px", "rgb(18, 52, 86)");
            checkScoreBar (div.childNodes[1], "166px", "166px", "225px", "rgb(35, 69, 103)");
            checkScoreBar (div.childNodes[2], "332px", "166px", "280px", "rgb(52, 86, 120)");
        });

        describe ("then three different scores", function () {

            beforeEach(function () {
                subject.showScores([45, 56, 78]);
            });

            it("shows three score bars of the appropriate color, size, and position", function () {
                checkScoreBar(div.childNodes[0], "0px", "166px", "225px", "rgb(18, 52, 86)");
                checkScoreBar(div.childNodes[1], "166px", "166px", "280px", "rgb(35, 69, 103)");
                checkScoreBar(div.childNodes[2], "332px", "166px", "390px", "rgb(52, 86, 120)");
            });

            describe("then four scores", function () {

                beforeEach(function () {
                    subject.showScores([34, 45, 56, 78]);
                });

                it("shows four score bars of the appropriate color, size, and position", function () {
                    checkScoreBar(div.childNodes[0], "0px", "125px", "170px", "rgb(18, 52, 86)");
                    checkScoreBar(div.childNodes[1], "125px", "125px", "225px", "rgb(35, 69, 103)");
                    checkScoreBar(div.childNodes[2], "250px", "125px", "280px", "rgb(52, 86, 120)");
                    checkScoreBar(div.childNodes[3], "375px", "125px", "390px", "rgb(18, 52, 86)");
                });

                describe("then two scores", function () {

                    beforeEach(function () {
                        subject.showScores([34, 45]);
                    });

                    it("shows two score bars of the appropriate color, size, and position", function () {
                        checkScoreBar(div.childNodes[0], "0px", "250px", "170px", "rgb(18, 52, 86)");
                        checkScoreBar(div.childNodes[1], "250px", "250px", "225px", "rgb(35, 69, 103)");
                    });
                });
            });
        });
    });

    function checkScoreBar (bar, top, height, width, color) {
        expect (bar.style.left).toBe ("0px");
        expect (bar.style.top).toBe (top);
        expect (bar.style.height).toBe (height);
        expect (bar.style.width).toBe (width);
        expect (bar.style.backgroundColor).toBe (color);
    }

    function makeDocument () {
        var div = document.createElement ("DIV");
        div.innerHTML =
            '<div id="sample-div" style="width:500px; height:500px; background-color:#202020;">\n' +
            '</div>\n';
        var body = document.getElementsByTagName ("BODY")[0];
        body.appendChild (div);
    }

    function removeDocument () {
        var div = document.getElementById ("sample-div");
        div.parentNode.removeChild (div);
    }
});