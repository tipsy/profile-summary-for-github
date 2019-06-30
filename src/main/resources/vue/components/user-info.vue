<template id="user-info">
    <div class="user-info">
        <img :src="user.avatarUrl" :alt="user.login">
        <div class="details">
            <div><i class="fa fa-fw fa-user"></i>{{ user.login }}
                <small v-if="user.name">({{ user.name }})</small>
            </div>
            <div><i class="fa fa-fw fa-database"></i>{{ user.publicRepos }} public repos</div>
            <div><i class="fa fa-fw fa-clock-o"></i>Joined GitHub {{ timeAgo }}</div>
            <div v-if="user.email"><i class="fa fa-fw fa-envelope"></i> {{ user.email }}</div>
            <div v-if="user.company"><i class="fa fa-fw fa-building"></i>{{ user.company }}</div>
            <div><i class="fa fa-fw fa-external-link"></i><a :href="user.htmlUrl" target="_blank">View profile on GitHub</a></div>
        </div>
        <div class="chart-container commits-per-quarter">
            <canvas id="quarterCommitCount"></canvas>
        </div>
    </div>
</template>
<script>
    Vue.component("user-info", {
        template: "#user-info",
        props: ["user", "data"],
        computed: {
            timeAgo() {
                return moment(this.user.createdAt).fromNow()
            }
        },
        mounted() {
            lineChart("quarterCommitCount", this.data)
        }
    });
</script>
<style>
    .user-info {
        display: flex;
        padding-bottom: 40px;
    }

    .user-info img {
        align-self: center;
        border-radius: 3px;
        width: 175px;
        margin-right: 20px;
    }

    .user-info .details {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        margin-right: 20px;
        flex-shrink: 0;
    }

    .user-info i.fa {
        color: rgba(0, 0, 0, 0.67);
        margin-right: 5px;
    }

    .user-info .commits-per-quarter {
        flex-grow: 1;
        flex-shrink: 1;
        position: relative;
    }

    .user-info .commits-per-quarter::after {
        content: "Commits per quarter";
        position: absolute;
        right: 40px;
        bottom: -15px;
        font-size: 13px;
    }

    @media (max-width: 480px) {
        .user-info img,
        .user-info .commits-per-quarter{
            display: none;
        }
    }
</style>
