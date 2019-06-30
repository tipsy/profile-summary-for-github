<template id="user-view">
    <app-frame v-slot="{requestsLeft}">
        <loading-bouncer v-if="!data && !error"></loading-bouncer>
        <div v-if="data" class="fade-in">
            <share-bar :user="user"></share-bar>
            <user-info :user="user" :data="data"></user-info>
            <donut-charts :data="data"></donut-charts>
        </div>
        <div v-if="error">
            <div v-if="error.response.status === 404">User not found.</div>
            <div v-else-if="requestsLeft >= 5000">Something went wrong. Please try again.</div>
            <div v-else-if="requestsLeft < 5000">Less than 5000 requests left. Please star the repo and try again.</div>
            <div v-else-if="requestsLeft === 0">The app is rate-limited. Please come back later.</div>
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
