function donutChart(objectName, data) {
    new Chart(document.getElementById(objectName).getContext("2d"), {
        type: "doughnut",
        data: {
            labels: Object.keys(data[objectName]),
            datasets: [{
                data: Object.values(data[objectName]),
                backgroundColor: repeatColors(Object.keys(data[objectName]).length)
            }]
        },
        options: {
            animation: false,
            legend: {
                position: "left"
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
