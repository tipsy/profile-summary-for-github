<template id="search-view">
    <app-frame v-slot="{requestsLeft}">
        <div class="search-screen">
            <h1>Enter GitHub username</h1>
            <div class="search-input-container">
                <input type="text" name="q" placeholder="ex. 'tipsy'" v-model="query" autofocus @keydown.enter="search">
            </div>
            <button @click="search" :disabled="!query.trim()">View Profile</button>
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

    .search-input-container {
        width: 100%;
        max-width: 300px;
    }

    .search-screen input {
        width: 100%;
        height: 40px;
        font-size: 18px;
        padding: 0 15px;
        border: 2px solid transparent;
        border-radius: 4px;
        transition: all 0.2s;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }

    .search-screen input:hover {
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
    }

    .search-screen input:focus {
        border-color: #0082c8;
        box-shadow: 0 2px 8px rgba(0, 130, 200, 0.2);
    }

    .search-screen button {
        margin-top: 20px;
        height: 40px;
        font-size: 18px;
        padding: 0 25px;
        border: 0;
        line-height: 1;
        background: #0082c8;
        color: white;
        cursor: pointer;
        border-radius: 4px;
        transition: background 0.2s;
    }

    .search-screen button:hover:not(:disabled) {
        background: #006ba1;
    }

    .search-screen button:disabled {
        background: #ccc;
        cursor: not-allowed;
        opacity: 0.6;
    }

    @media (max-width: 480px) {
        .search-screen h1 {
            font-size: 24px;
            margin: 10px 0 20px 0;
        }

        .search-input-container {
            max-width: 100%;
            padding: 0 10px;
        }

        .search-screen input {
            font-size: 16px;
            height: 44px;
            min-width: auto;
        }

        .search-screen button {
            width: 100%;
            max-width: 300px;
            font-size: 16px;
            height: 44px;
        }

        .search-screen .search-term {
            font-size: 90%;
        }

        .search-screen > div {
            padding: 0 20px;
        }
    }
</style>
