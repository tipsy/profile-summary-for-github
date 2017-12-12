# github-profile-summary

## run locally
* `git clone https://github.com/tipsy/github-profile-summary.git`
* `cd github-profile-summary`
* `mvn install`
* `java -jar target/github-profile-summary-1.0-SNAPSHOT-jar-with-dependencies.jar`

If no api-token is set, you only get ~50 requests/hour  

To run the app with an api-token, first generate a token at 
[https://github.com/settings/tokens](https://github.com/settings/tokens), 
then launch the jar with the token:

