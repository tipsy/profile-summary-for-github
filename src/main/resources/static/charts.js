function donutChart(objectName, data) {
    var canvas = document.getElementById(objectName);
    if (canvas === null) {
        return;
    }
    var userId = document.body.getAttribute("data-user-id");
    var labels = Object.keys(data[objectName]);
    var values = Object.values(data[objectName]);
    var colors = repeatColors(labels.length);
    window.languageColors = window.languageColors || {};
    if ("langRepoCount" === objectName) {
        // when the first language-set is loaded, set a color-profile for all languages
        labels.forEach((language, i) => languageColors[language] = colors[i]);
    }
    if (["langRepoCount", "langStarCount", "langCommitCount"].indexOf(objectName) > -1) {
        // if the dataset is language-related, load color-profile
        labels.forEach((language, i) => colors[i] = languageColors[language]);
    }
    if (objectName === "repoCommitCount") {
        arrayRotate(colors, 2); // change starting color
    }
    if (objectName === "repoStarCount") {
        arrayRotate(colors, 4); // change starting color
    }
    new Chart(canvas.getContext("2d"), {
        type: "doughnut",
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: colors
            }]
        },
        options: {
            animation: false,
            rotation: (-0.40 * Math.PI),
            legend: {
                position: "left",
                labels: {
                    boxWidth: 12
                }
            },
            onClick: function (e, data) {
                try {
                    var label = labels[data[0]._index];
                    var canvas = data[0]._chart.canvas.id;
                    if (canvas === "repoStarCount" || canvas === "repoCommitCount") {
                        window.open("https://github.com/" + userId + "/" + label, "_blank");
                        window.focus();
                    } else {
                        window.open("https://github.com/" + userId + "?utf8=%E2%9C%93&tab=repositories&q=&type=source&language=" + encodeURIComponent(label), "_blank");
                        window.focus();
                    }
                } catch (ignored) {
                }
            }
        }
    });
}

function lineChart(objectName, data) {
    new Chart(document.getElementById(objectName).getContext("2d"), {
        type: "line",
        data: {
            labels: Object.keys(data[objectName]),
            datasets: [{
                label: 'Commits',
                data: Object.values(data[objectName]),
                backgroundColor: "rgba(67, 142, 233, 0.2)",
                borderColor: "rgba(67, 142, 233, 1)",
                lineTension: 0
            }]
        },
        options: {
            maintainAspectRatio: false,
            animation: false,
            scales: {
                xAxes: [{
                    display: false
                }],
                yAxes: [{
                    position: "right"
                }]
            },
            legend: {
                display: false
            },
            tooltips: {
                intersect: false
            }
        }
    });
}

var rainbowColors = [
    "#54ca76",
    "#f5c452",
    "#f2637f",
    "#9261f3",
    "#31a4e6",
    "#55cbcb",
];

function repeatColors(length) {
    var array = [];
    while (array.length < length) {
        array = array.concat(rainbowColors);
    }
    return array;
}

function arrayRotate(arr, n) {
    for (var i = 0; i < n; i++) {
        arr.push(arr.shift());
    }
    return arr
}
