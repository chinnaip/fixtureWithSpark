## fitnesse-fixtures
Analysis
The analysis fitnesse fixtures is a dockerized module which is deployed to every  environment as part of the deploy job. You can look at the `analysis-fitnesse` server group in the inventories repo to find out the fitnesse server. 

Like any other environment, the fitnesse container is deployed to a sandbox as well Once deployed, you can access the wiki

For triggering tests against an environment or a sandbox, you can use the environment specific wiki URL (the jenkins fitnesse test job can be made to do the same, but via ansible)

While refactoring the tests or building new ones, you might want to start up a local non-dockerized  wiki. To do that

Build the `analysis-fitnesse-fixtures` module 

    mvn clean install
	
Start analysis fitnesse wiki locally from `test\acceptance\analysis-fitnesse-fixtures`

    java -DMAV_OUTPUT_DIRECTORY=$(pwd)/target -DRESOURCE_DIRECTORY=$(pwd)/src/test/resources  -jar target/fitnesse-standalone.jar -p 9090 -f config.properties

This command should startup the fitnesse wiki @ http://localhost:9090/AnalysisRoot . The config file(config.properties) is setup with details for  a sandbox. If you want your local wiki to point  to a different environment, tweak the config file appropriately.
