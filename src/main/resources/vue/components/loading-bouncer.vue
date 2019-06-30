<template id="loading-bouncer">
    <div class="loading-bouncer" style="opacity: 0; animation: fade-in 0.2s linear 0.5s forwards">
        <div class="la-square-jelly-box la-3x">
            <div></div>
            <div></div>
        </div>
        <h2>Analyzing GitHub profile</h2>
        <h3 style="opacity: 0; animation: fade-in 0.2s linear 4s forwards">This could take some time ...</h3>
        <h3 style="opacity: 0; animation: fade-in 0.2s linear 8s forwards">This user has a lot of repos!</h3>
    </div>
</template>
<script>
    Vue.component("loading-bouncer", {template: "#loading-bouncer"});
</script>
<style>
    .loading-bouncer {
        margin-top: 50px;
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    .la-square-jelly-box {
        color: #38abe2;
    }
</style>
