<style>
    * {
        font-family: 'Barlow Semi Condensed', sans-serif;
        outline: 0;
        box-sizing: border-box;
    }

    [v-cloak] {
        display: none;
    }

    html {
        font-size: 18px;
        background: #eee9df;
        padding: 60px 30px;
        overflow-y: scroll;
    }

    body {
        margin: 0;
    }

    h1, h2, h3, h4 {
        font-weight: 400;
    }

    a {
        color: #0082c8;
        text-decoration: none;
    }

    .content {
        max-width: 1200px;
        margin: 0 auto;
    }

    .fade-in {
        opacity: 0;
        animation: fade-in .2s linear forwards;
    }

    @keyframes fade-in {
        from {
            opacity: 0;
        }
        to {
            opacity: 1;
        }
    }
</style>
