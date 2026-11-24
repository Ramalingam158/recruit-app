# recruit-app
## Running the Application:
There are 2 ways to run it:
* Using Docker (If installed in your system)
* Running it directly from an IDE

Please do clone the application in your system

### Running with Docker
Once cloned, please run the below 2 commands in order to create an image & run (in detached mode)

* `docker build -t recruit-api:local .`

* `docker run -d --rm -p 8080:8080 -e SPRING_H2_CONSOLE_SETTINGS_WEB_ALLOW_OTHERS=true --name recruit-api recruit-api:local`

After 15s - 30s, application should have started successfully & can be tested now

### Running with IDE
Once cloned, open an IDE of your choice & load this repo as a maven project. Then please open ApiServiceApplication class & you should be able to see a play button near class name & main method name (next to line number). Press that & application should start up normally

## Running Integration Tests
Integration tests are configured to run with `verify` phase with `integration-tests` profile. Please run the below command to run it

`mvn verify -Pintegration-tests`