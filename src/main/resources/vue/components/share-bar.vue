<template id="share-bar">
    <div class="share-bar">
        <a class="social-btn" :href="twitterUrl" rel="nofollow" title="Share on Twitter"><i class="fa fa-fw fa-twitter"></i>Share on Twitter</a>
        <a class="social-btn" :href="facebookUrl" rel="nofollow" title="Share on Facebook"><i class="fa fa-fw fa-facebook"></i>Share on Facebook</a>
    </div>
</template>
<script>
    Vue.component("share-bar", {
        template: "#share-bar",
        props: ["user"],
        computed: {
            profileUrl: function () {
                return "https://profile-summary-for-github.com/user/" + this.user.login;
            },
            shareText: function () {
                return this.user.login + "'s GitHub profile - Visualized:";
            },
            twitterUrl: function () {
                return "https://twitter.com/intent/tweet?url=" + this.profileUrl + "&text=" + this.shareText + "&via=javalin_io&related=javalin_io";
            },
            facebookUrl: function () {
                return "https://facebook.com/sharer.php?u=" + this.profileUrl + "&quote=" + this.shareText
            }
        }
    });
</script>
<style>
    .share-bar {
        position: absolute;
        top: 0;
        left: 50%;
        transform: translateX(-50%);
        background: rgba(0, 0, 0, .04);
        font-size: 14px;
        text-align: center;
    }

    .share-bar a {
        white-space: nowrap;
        margin: 5px 8px;
        display: inline-block;
    }

    .share-bar a i {
        color: #0082c8;
    }

    @media (max-width: 480px) {
        .share-bar {
            width: 100%;
        }
    }
</style>
