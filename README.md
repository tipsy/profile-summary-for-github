# github-profile-summary

## live at [https://github-profile-summary.com/](https://github-profile-summary.com/)

## screenshot
![screenshot](https://user-images.githubusercontent.com/1521451/34072014-4451dbf6-e280-11e7-90a7-32ad1f313541.PNG)

## run locally
* `git clone https://github.com/tipsy/github-profile-summary.git`
* `cd github-profile-summary`
* `mvn install`
* `java -jar target/github-profile-summary-1.0-jar-with-dependencies.jar`

If no api-token is set, you only get ~50 requests/hour

To run the app with an api-token, first generate a token at
[https://github.com/settings/tokens](https://github.com/settings/tokens),
then launch the jar with the token:

* `java -Dapi-tokens=your-token -jar target/github-profile-summary-1.0-jar-with-dependencies.jar`

You can use a comma-separated list of tokens to increase your rate-limit

You can build a profile summary for any GitHub profile using `-Dunrestricted=true`:

* `java -Dunrestricted=true -jar target/github-profile-summary-1.0-jar-with-dependencies.jar`

## run locally with docker

* `git clone https://github.com/tipsy/github-profile-summary.git`
* `cd github-profile-summary`
* `docker build -t github-profile-summary .`
* `docker run -it --rm --name github-profile-summary -p 7070:7070 github-profile-summary`
* OR with a token `docker run -it --rm --name github-profile-summary -p 7070:7070 -e "TOKENS=mytoken1,mytoken2" github-profile-summary`
* browse to http://localhost:7070
