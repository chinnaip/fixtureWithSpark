## Analysis fitnesse-fixtures

The analysis fitnesse fixtures is a dockerized module which is deployed to every  environment as part of the deploy job. You can look at the `analysis-fitnesse` server group in the inventories repo to find out the fitnesse server. For example FUT1 fitnesse server is `ghelion-fut1-s9.ie.tslabs.hpecorp.net` and the wiki URL is https://ghelion-fut1-s9.ie.tslabs.hpecorp.net:8082/AnalysisRoot . For all the environments(except sandbox), the wiki runs at port 8082 over https.

Like any other environment, the fitnesse container is deployed to a sandbox as well(done by default when you run the sandbox deploy shell script - `deploy\SandboxScripts\deploy_analysis.sh`). Once deployed, you can access the wiki at @ http://hdp-sandbox.hpe.com:2181/AnalysisRoot (for sandboxes, ssl isn't enabled and the wiki is configured to use port 2181 as 8082 is used by curiosity REST)

For triggering tests against an environment or a sandbox, you can use the environment specific wiki URL(the jenkins fitnesse test job does the same, but via ansible)

While refactoring the tests or building new ones, you might want to start up a local non-dockerized  wiki. To do that

Build the `analysis-fitnesse-fixtures` module 

    mvn clean install
	
Start analysis fitnesse wiki locally from `test\acceptance\analysis-fitnesse-fixtures`

    java -DMAV_OUTPUT_DIRECTORY=$(pwd)/target -DRESOURCE_DIRECTORY=$(pwd)/src/test/resources  -jar target/fitnesse-standalone.jar -p 9090 -f config.properties

This command should startup the fitnesse wiki @ http://localhost:9090/AnalysisRoot . The config file(config.properties) is setup with details for  a sandbox. If you want your local wiki to point  to a different environment, tweak the config file appropriately.
