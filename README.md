# Kotlin Boilerplate
> Got some error on Node and dotNet? Kotlin got you...

This is an awesome start up boilerplate for API and App Services using the following libraries :
> Let's just say that this can replace your old apps and services. :)

Database
- [impossibl](https://github.com/impossibl/pgjdbc-ng)
- [JetBrians / Exposed](https://github.com/JetBrains/Exposed)
- [JetBrians / exposed-jodatime](https://github.com/JetBrains/Exposed/tree/master/exposed-jodatime)
- [HikariCP](https://github.com/brettwooldridge/HikariCP) - we've heard that you always encounter pool error? Hikari is the key!
- [KMongo](https://litote.org/kmongo/)

HTTP Request
- [Unirest-Java](http://kong.github.io/unirest-java/) - so if you're from nodeJS world and you use HTTP request, you're probably thinking fetch or axios? Yeah that's the counterpart on java/kotlin.


Web Framework for URL and Endpoints
- [Javalin](https://javalin.io/) - expressJS? We got Javalin here on Java/Kotlin :P

Logs
- [slf4j-simple](http://www.slf4j.org/api/org/slf4j/impl/SimpleLogger.html)
- [kotlin-logging](https://github.com/MicroUtils/kotlin-logging)

JSON Object Format
- [kotlin json](https://developer.android.com/reference/kotlin/org/json/JSONObject)

DotEnv
- [java-dotenv](https://github.com/cdimascio/java-dotenv)

Datetime Format
- [joda-time](https://github.com/JodaOrg/joda-time)

> You can click the name for more details of the libraries.
> 
> You can also add libraries inside the build.gradle.kts file on dependency object.

## Want to know how this cool thing created? Check the steps below...

### Step 1 : Just make sure that you have java and gradle installed on your machine.

```bash
java --version
```

```bash
gradle --version
```

### Step 2 : Initial Gradle
```bash
gradle init
```

 Choose the ff:
 - application
 - Kotlin
 - no
 - Kotlin
 - Input project name
 - Input source package name - app (recommended)

> It will create a basic file and directories

### Step 3 : Add Dependency Libraries on build.gradle.kts file inside the package source directory.

### Step 4 : Open starting file located on src/main/kotlin/app/App and start coding.

### Or you can clone this repo and start coding...

## Getting Started

### Step 1 : Install IntelliJ IDEA IDE on your machine.

> https://www.jetbrains.com/idea/

### Step 2 : Clone this repo or generate directories using gradle init

> git@gitlab.app.net:kraken/kotlin-boilerplate.git

### Step 3 : Rename .env.sample file to .env

```bash
cp .env.example .env
```

### Step 4 : Fill up all important variable on .env file.

### Step 5 : Go starting src/main/kotlin/app/App start the app.

### Step 6 : Test the app by visiting the following link on your browser.

http://localhost:{{APP_PORT}}/{{APP_ROUTE_PREFIX}}/text
http://localhost:{{APP_PORT}}/{{APP_ROUTE_PREFIX}}/json

> Saw the programmer's magic word? Then congrats! You are now a Java / Kotlin Developer.
> You may include that on your resume. ;)






