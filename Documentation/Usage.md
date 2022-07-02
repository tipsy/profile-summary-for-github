
# Usage

*How to configure the project.*

<br>

## API Token

*The access token to GitHubs API.*

<br>

### Without

If no api token is set, you only get `~50 requests / hour`.

<br>

### With

To run the app with an api-token, first **[Generate A Token]**.

Provide the token in the following way:

```shell
java                        \
    -Dapi-tokens=your-token \
    -jar target/profile-summary-for-github-jar-with-dependencies.jar
```

*You can use a comma-separated list* <br>
*of tokens to increase your rate-limit.*

<br>
<br>

## Parameters

*Command line options you can set.*

<br>

### Unrestricted

You can allow the building of any GitHub <br>
profile by passing `-Dunrestricted=true` :

```shell
java                    \
    -Dunrestricted=true \
    -jar target/profile-summary-for-github-jar-with-dependencies.jar
```

<br>

### Free Threshold

You can set when the app should require <br>
the user to star the GitHub repository.

*Pass the number of remaining requests at which the* <br>
*cutoff should kick in, as seen in the following example:*

```shell
java                            \
    -Dfree-requests-cutoff=1000 \
    -jar target/profile-summary-for-github-jar-with-dependencies.jar
```

<br>

### Google Tag Manager

You can enable this feature by passing the `gtm-id`.

```shell
java                    \
    -Dgtm-id=GTM-XXXXXX \
    -jar target/profile-summary-for-github-jar-with-dependencies.jar
```

<br>


<!----------------------------------------------------------------------------->

[Generate A Token]: https://github.com/settings/tokens
