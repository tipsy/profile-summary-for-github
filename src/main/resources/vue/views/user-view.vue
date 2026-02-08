<template id="user-view">
    <app-frame v-slot="{requestsLeft}">
        <div class="back-button-container">
            <a href="/search" class="back-button">
                <i class="fa fa-arrow-left"></i> Search Another User
            </a>
        </div>
        <loading-bouncer v-if="!data && !error"></loading-bouncer>
        <div v-if="data" class="fade-in">
            <share-bar :user="user"></share-bar>
            <user-info :user="user" :data="data"></user-info>
            <donut-charts :data="data"></donut-charts>
        </div>
        <div v-if="error" class="error-container">
            <div class="error-box">
                <i class="fa fa-exclamation-circle error-icon"></i>
                <div v-if="error.response.status === 404">
                    <h3>User Not Found</h3>
                    <p>We couldn't find a GitHub user with that username. Please check the spelling and try again.</p>
                </div>
                <div v-else-if="requestsLeft === 0">
                    <h3>Rate Limited</h3>
                    <p>The app has reached its GitHub API rate limit. Please come back later or build the app locally with your own tokens.</p>
                </div>
                <div v-else>
                    <h3>Something Went Wrong</h3>
                    <p>An unexpected error occurred. Please try again in a moment.</p>
                </div>
                <a href="/search" class="error-back-button">← Back to Search</a>
            </div>
        </div>
    </app-frame>
</template>
<script>
    Vue.component("user-view", {
        template: "#user-view",
        data: () => ({
            data: null,
            user: null,
            error: null,
        }),
        created() {
            let userId = this.$javalin.pathParams["user"];
            axios.get("/api/user/" + userId).then(response => {
                this.data = response.data;
                this.user = response.data.user;
            }).catch(error => this.error = error);
        },
    });
</script>
<style>
    .back-button-container {
        margin-bottom: 20px;
    }

    .back-button {
        display: inline-block;
        padding: 8px 16px;
        background: rgba(255, 255, 255, 0.8);
        border-radius: 4px;
        font-size: 14px;
        transition: background 0.2s;
    }

    .back-button:hover {
        background: rgba(255, 255, 255, 1);
    }

    .back-button i {
        margin-right: 5px;
    }

    .error-container {
        display: flex;
        justify-content: center;
        align-items: center;
        min-height: 300px;
    }

    .error-box {
        background: white;
        padding: 40px;
        border-radius: 4px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        text-align: center;
        max-width: 500px;
    }

    .error-icon {
        font-size: 48px;
        color: #e74c3c;
        margin-bottom: 20px;
    }

    .error-box h3 {
        margin: 0 0 10px 0;
        color: #333;
    }

    .error-box p {
        margin: 0 0 20px 0;
        color: #666;
        line-height: 1.6;
    }

    .error-back-button {
        display: inline-block;
        padding: 10px 20px;
        background: #0082c8;
        color: white;
        border-radius: 4px;
        margin-top: 10px;
        transition: background 0.2s;
    }

    .error-back-button:hover {
        background: #006ba1;
    }

    @media (max-width: 480px) {
        .back-button-container {
            margin-bottom: 15px;
        }

        .back-button {
            font-size: 13px;
            padding: 6px 12px;
        }

        .error-box {
            padding: 30px 20px;
            margin: 0 10px;
        }

        .error-icon {
            font-size: 36px;
            margin-bottom: 15px;
        }

        .error-box h3 {
            font-size: 20px;
        }

        .error-box p {
            font-size: 15px;
        }

        .error-back-button {
            width: 100%;
            max-width: 250px;
        }
    }
</style>
