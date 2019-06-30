<template id="search-view">
    <app-frame v-slot="{requestsLeft}">
        <div class="search-screen">
            <h1>Enter GitHub username</h1>
            <input type="text" name="q" placeholder="ex. 'tipsy'" v-model="query" autofocus @keydown.enter="search">
            <div v-if="error && error.response.status === 404">
                <h4>Can't find user <span class="search-term">{{failedQuery}}</span>. Check spelling.</h4>
            </div>
            <div v-else-if="failedQuery">
                <h4>Can't build profile for <span class="search-term">{{failedQuery}}</span></h4>
                <p>
                    If you are <span class="search-term">{{failedQuery}}</span>, please
                    <a href="https://github.com/tipsy/profile-summary-for-github">star the repo</a> and try again.
                </p>
                <p>
                    The app is running with two GitHub tokens, giving 10 000 requests per hour.
                    The first 5000 requests can be used to build any profile, while the last 5000 requests are
                    reserved for users building their own profile. To confirm that you're building your own
                    profile, we check if you've starred the repository.
                </p>
            </div>
            <div v-if="requestsLeft === 0">
                The app is rate limited. Please come back later or build the app locally and use your own tokens.
            </div>
        </div>
    </app-frame>
</template>
<script>
    Vue.component("search-view", {
        template: "#search-view",
        data: () => ({
            error: null,
            failedQuery: "",
            query: ""
        }),
        methods: {
            search() {
                this.error = null;
                this.failedQuery = null;
                axios.get("/api/can-load?user=" + this.query)
                    .then(() => window.location = "/user/" + this.query)
                    .catch(error => {
                        this.error = error;
                        this.failedQuery = this.query
                    });
            }
        },
    });
</script>
<style>
    .search-screen {
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    .search-term {
        border: 1px solid rgba(0, 0, 0, 0.2);
        background: rgba(0, 0, 0, 0.025);
        padding: 1px 2px;
        font-family: monospace;
        font-size: 80%;
    }

    .search-screen input {
        height: 40px;
        font-size: 18px;
        padding: 0 15px;
        border: 0;
    }
</style>
