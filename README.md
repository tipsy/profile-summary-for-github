# profile-summary-for-github

## live at [https://profile-summary-for-github.com/](https://profile-summary-for-github.com/)

## screenshot
![screenshot](https://user-images.githubusercontent.com/1521451/34072014-4451dbf6-e280-11e7-90a7-32ad1f313541.PNG)

## run locally
* `git clone https://github.com/tipsy/profile-summary-for-github.git`
* `cd profile-summary-for-github`
* `mvn install`
* `java -jar target/profile-summary-for-github-jar-with-dependencies.jar`

If no api-token is set, you only get ~50 requests/hour

To run the app with an api-token, first generate a token at
[https://github.com/settings/tokens](https://github.com/settings/tokens),
then launch the jar with the token:

* `java -Dapi-tokens=your-token -jar target/profile-summary-for-github-jar-with-dependencies.jar`

You can use a comma-separated list of tokens to increase your rate-limit

You can build a profile summary for any GitHub profile using `-Dunrestricted=true`:

* `java -Dunrestricted=true -jar target/profile-summary-for-github-jar-with-dependencies.jar`

You can bypass user star checks for a given number of remaining requests by setting `star-bypass`:

* `java -Dstar-bypass=5000 -jar target/profile-summary-for-github-jar-with-dependencies.jar`

You can enable Google Tag Manager on your instance by setting `gtm-id`:

* `java -Dgtm-id=GTM-XXXXXX -jar target/profile-summary-for-github-jar-with-dependencies.jar`

## run locally with docker

* `git clone https://github.com/tipsy/profile-summary-for-github.git`
* `cd profile-summary-for-github`
* `docker build -t profile-summary-for-github .`
* `docker run -it --rm --name profile-summary-for-github -p 7070:7070 profile-summary-for-github`
* OR with a token `docker run -it --rm --name profile-summary-for-github -p 7070:7070 -e "API_TOKENS=mytoken1,mytoken2" profile-summary-for-github`
* browse to http://localhost:7070
