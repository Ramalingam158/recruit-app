# recruit-app
## Running the Application:
There are 2 ways to run it:
* Using Docker (If installed in your system)
* Running it directly from an IDE

Please do clone the application in your system

### Running with Docker
Once cloned, please run the below 2 commands in order to create an image & run (in detached mode)

* ``docker build -t recruit-api:local .``

* ``docker run -d --rm -p 8080:8080 -e SPRING_H2_CONSOLE_SETTINGS_WEB_ALLOW_OTHERS=true --name recruit-api recruit-api:local``

After 15s - 30s, application should have started successfully & can be tested now

