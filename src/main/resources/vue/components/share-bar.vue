<template id="share-bar">
    <div class="share-bar">
        <a class="social-btn" :href="twitterUrl" rel="nofollow" title="Share on Twitter"><i class="fa fa-fw fa-twitter"></i>Share on Twitter</a>
        <a class="social-btn" :href="facebookUrl" rel="nofollow" title="Share on Facebook"><i class="fa fa-fw fa-facebook"></i>Share on Facebook</a>
        <a class="social-btn copy-link-btn" @click.prevent="copyLink" :title="copyButtonText">
            <i class="fa fa-fw" :class="copied ? 'fa-check' : 'fa-link'"></i>{{ copyButtonText }}
        </a>
    </div>
</template>
<script>
    Vue.component("share-bar", {
        template: "#share-bar",
        props: ["user"],
        data: () => ({
            copied: false
        }),
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
            },
            copyButtonText: function () {
                return this.copied ? "Copied!" : "Copy Link";
            }
        },
        methods: {
            copyLink: function () {
                const url = this.profileUrl;
                if (navigator.clipboard && navigator.clipboard.writeText) {
                    navigator.clipboard.writeText(url).then(() => {
                        this.copied = true;
                        setTimeout(() => { this.copied = false; }, 2000);
                    });
                } else {
                    // Fallback for older browsers
                    const textArea = document.createElement("textarea");
                    textArea.value = url;
                    textArea.style.position = "fixed";
                    textArea.style.left = "-999999px";
                    document.body.appendChild(textArea);
                    textArea.select();
                    try {
                        document.execCommand('copy');
                        this.copied = true;
                        setTimeout(() => { this.copied = false; }, 2000);
                    } catch (err) {
                        console.error('Failed to copy:', err);
                    }
                    document.body.removeChild(textArea);
                }
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
        border-radius: 4px;
        padding: 5px 10px;
    }

    .share-bar a {
        white-space: nowrap;
        margin: 5px 8px;
        display: inline-block;
        transition: opacity 0.2s;
    }

    .share-bar a:hover {
        opacity: 0.7;
    }

    .share-bar a i {
        color: #0082c8;
    }

    .copy-link-btn {
        cursor: pointer;
    }

    .copy-link-btn.copied {
        color: #27ae60;
    }

    .copy-link-btn .fa-check {
        color: #27ae60;
    }

    @media (max-width: 480px) {
        .share-bar {
            width: 100%;
            position: relative;
            left: 0;
            transform: none;
            margin-bottom: 20px;
            font-size: 12px;
            padding: 8px 5px;
        }

        .share-bar a {
            margin: 3px 4px;
            font-size: 12px;
        }

        .share-bar a i {
            margin-right: 3px;
        }
    }
</style>
