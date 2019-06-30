<template id="donut-charts">
    <div class="charts">
        <div class="chart-row">
            <div class="chart-container chart-container--third">
                <h2>Repos per Language</h2>
                <canvas id="langRepoCount"></canvas>
            </div>
            <div v-if="Math.max(...Object.values(data.repoStarCount)) > 0" class="chart-container chart-container--third">
                <h2>Stars per Language</h2>
                <canvas id="langStarCount"></canvas>
            </div>
            <div class="chart-container chart-container--third">
                <h2>Commits per Language</h2>
                <canvas id="langCommitCount"></canvas>
            </div>
        </div>
        <div class="chart-row">
            <div class="chart-container chart-container--half">
                <h2>Commits per Repo
                    <small v-if="Object.keys(data.repoCommitCount).length === 10">(top 10)</small>
                </h2>
                <canvas id="repoCommitCount"></canvas>
            </div>
            <div v-if="Object.keys(data.repoStarCount).length > 0" class="chart-container chart-container--half">
                <h2>Stars per Repo
                    <small v-if="Object.keys(data.repoStarCount).length == 10">(top 10)</small>
                </h2>
                <canvas id="repoStarCount"></canvas>
            </div>
        </div>
    </div>
</template>
<script>
    Vue.component("donut-charts", {
        template: "#donut-charts",
        props: ["data"],
        mounted() {
            donutChart("langRepoCount", this.data);
            donutChart("langStarCount", this.data);
            donutChart("langCommitCount", this.data);
            donutChart("repoCommitCount", this.data);
            donutChart("repoStarCount", this.data);
        }
    });
</script>
<style>
    canvas {
        user-select: none;
    }

    .charts,
    .chart-row {
        overflow: auto;
    }

    .chart-row {
        padding-bottom: 40px;
    }

    .chart-row {
        border-top: 1px solid rgba(0, 0, 0, 0.1);
        display: flex;
        justify-content: space-around;
    }

    .chart-container--third {
        width: 33%;
    }

    .chart-container--half {
        width: 50%;
    }

    @media (max-width: 900px) {
        .chart-container--third,
        .chart-container--half {
            width: 100%;
        }

        .chart-row {
            display: block;
        }

    }

    @media (max-width: 480px) {
        footer {
            display: none;
        }
    }
</style>
