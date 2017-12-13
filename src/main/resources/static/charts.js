function donutChart(objectName, data) {
    var userId = document.body.getAttribute("data-user-id");
    var labels = Object.keys(data[objectName]);
    var values = Object.values(data[objectName]);
    new Chart(document.getElementById(objectName).getContext("2d"), {
        type: "doughnut",
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: repeatColors(Object.keys(data[objectName]).length)
            }]
        },
        options: {
            animation: false,
            legend: {
                position: "left"
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

function repeatColors(length) {
    var array = [];
    while (array.length < length) {
        array = array.concat([
            "#f2637f",
            "#f5a441",
            "#f5c452",
            "#54ca76",
            "#55cbcb",
            "#31a4e6",
            "#9261f3"
        ]);
    }
    return array;
}
