<template id="app-frame">
    <div>
        <main class="main-content">
            <slot :requests-left="requestsLeft"></slot>
        </main>
        <footer>
            GitHub profile summary is built with <a href="https://javalin.io">javalin</a> <small>(kotlin web framework)</small> and
            <a href="http://www.chartjs.org/docs/latest/" target="_blank">chart.js</a> <small>(visualization)</small>.
            Source is on <a href="https://github.com/tipsy/profile-summary-for-github" target="_blank">GitHub</a>.
        </footer>
        <span class="rate-limit">
            <span v-if="requestsLeft === 0">The app is currently rate-limited<br>Please check back later</span>
            <span v-if="requestsLeft !== 0"><strong>{{requestsLeft}}</strong> requests left <br> before rate-limit</span>
        </span>
    </div>
</template>
<script>
    Vue.component("app-frame", {
        template: "#app-frame",
        data: () => ({
            requestsLeft: 9876,
        }),
        created() {
            let wsProtocol = window.location.protocol.indexOf("https") > -1 ? "wss" : "ws";
            let ws = new WebSocket(wsProtocol + "://" + location.hostname + ":" + location.port + "/rate-limit-status");
            ws.onmessage = msg => this.requestsLeft = msg.data;
        },
    });
</script>
<style>
    footer {
        font-size: 17px;
        position: fixed;
        left: 0;
        bottom: 0;
        width: 100%;
        text-align: center;
        padding: 10px 30px;
        border-top: 1px solid rgba(0, 0, 0, 0.1);
        background: #eee9df;
    }

    .rate-limit {
        position: fixed;
        right: 20px;
        bottom: 60px;
        background: #fff;
        padding: 10px;
        box-shadow: 0 1px 1px rgba(0, 0, 0, 0.3);
        font-size: 16px;
    }

    @media (max-width: 480px) {
        .rate-limit {
            padding: 5px 8px;
            font-size: 13px;
            bottom: 0;
            right: 0;
        }
    }
</style>
