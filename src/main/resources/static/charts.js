function donutChart(objectName, data) {
    let canvas = document.getElementById(objectName);
    if (canvas === null) {
        return;
    }
    let userId = document.body.getAttribute("data-user-id");
    let labels = Object.keys(data[objectName]);
    let values = Object.values(data[objectName]);
    let colors = createColorArray(labels.length);
    let tooltipInfo = null;
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
        tooltipInfo = data[objectName + "Descriptions"]; // high quality programming
        arrayRotate(colors, 4); // change starting color
    }
    if (objectName === "repoStarCount") {
        tooltipInfo = data[objectName + "Descriptions"]; // high quality programming
        arrayRotate(colors, 2); // change starting color
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
            tooltips: {
                callbacks: {
                    afterLabel: function (tooltipItem, data) {
                        if (tooltipInfo !== null) {
                            return wordWrap(tooltipInfo[data["labels"][tooltipItem["index"]]], 45);
                        }
                    }
                },
            },
            onClick: function (e, data) {
                try {
                    let label = labels[data[0]._index];
                    let canvas = data[0]._chart.canvas.id;
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

    function createColorArray(length) {
        let array = [];
        while (array.length < length) {
            array = array.concat([
                "#54ca76",
                "#f5c452",
                "#f2637f",
                "#9261f3",
                "#31a4e6",
                "#55cbcb",
            ]);
        }
        return array;
    }

    function arrayRotate(arr, n) {
        for (let i = 0; i < n; i++) {
            arr.push(arr.shift());
        }
        return arr
    }

    function wordWrap(str, n) {
        if (str === null) {
            return null;
        }
        let currentLine = [];
        let resultLines = [];
        str.split(" ").forEach(word => {
            currentLine.push(word);
            if (currentLine.join(" ").length > n) {
                resultLines.push(currentLine.join(" "));
                currentLine = [];
            }
        });
        if (currentLine.length > 0) {
            resultLines.push(currentLine.join(" "));
        }
        return resultLines
    }
}

function lineChart(objectName, data) {
    new Chart(document.getElementById(objectName).getContext("2d"), {
        type: "line",
        data: {
            labels: Object.keys(data[objectName]),
            datasets: [{
                label: "Commits",
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

