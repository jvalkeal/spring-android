# Spring for Android

[Spring for Android](http://www.springsource.org/spring-android) is an extension of the [Spring Framework](http://www.springsource.org/spring-framework) that aims to simplify the development of native [Android](http://developer.android.com/index.html) applications.

NOTE: This fork depends on android v4 support library for backward compatibility for older api versions. Because only revisions 6/7 exists on public maven repositories, there is a dependency for newest support libraries deployed(e.g. to local maven repo) using maven-android-sdk-deployer(https://github.com/mosabua/maven-android-sdk-deployer). 

## Check Out and Build from Source

1. Clone the repository from GitHub:

		$ git clone git://github.com/SpringSource/spring-android.git

2. Navigate into the cloned repository directory:

		$ cd spring-android

3. The project uses [Gradle](http://gradle.org/) to build:

		$ ./gradlew build

## Eclipse

To generate Eclipse metadata (.classpath and .project files), use the following Gradle task:

	$ ./gradlew eclipse

Once complete, you may then import the projects into Eclipse as usual:

	File -> Import -> Existing projects into workspace

Alternatively, [SpringSource Tool Suite](http://www.springsource.com/developer/sts) has built in support for [Gradle](http://gradle.org/), and you can simply import as Gradle projects.

## IDEA

To generate IDEA metadata (.iml and .ipr files), use the following Gradle task:

	$ ./gradlew idea

## JavaDoc

Use the following Gradle task to build the JavaDoc

	$ ./gradlew :docs:api

_Note: The result will be available in 'docs/build/api'._

## Tests

There are three Android Test Projects located in the repository that correspond to the three Spring for Android Modules (Core, Rest Template, and Auth). These projects are executed separately from the Gradle build process. To run the suite of tests, perform the following steps. A parent POM located in the root of the repository will execute each test project on all attached devices and emulators.

1. Build Spring for Android JARs and install them to the local Maven repository:

		$ ./gradlew build install

2. The tests are run using the Android Maven Plugin:

		$ mvn clean install

	_Note: Each test project can also be executed individually, by running the previous command from within the respective test project's directory._

## Maven

The [Android Maven Plugin](http://code.google.com/p/maven-android-plugin) makes it possible to build Android applications utilizing the power of Maven dependency management.

### Dependencies

Spring for Android consists of three modules: Core, Rest Template, and Auth. These are available via the following Maven dependencies:

	<dependency>
	    <groupId>org.springframework.android</groupId>
	    <artifactId>spring-android-core</artifactId>
	    <version>${org.springframework.android-version}</version>
	</dependency>

	<dependency>
	    <groupId>org.springframework.android</groupId>
	    <artifactId>spring-android-rest-template</artifactId>
	    <version>${org.springframework.android-version}</version>
	</dependency>

	<dependency>
	    <groupId>org.springframework.android</groupId>
	    <artifactId>spring-android-auth</artifactId>
	    <version>${org.springframework.android-version}</version>
	</dependency>

### Repositories

Three primary repositories are provided by SpringSource: release, milestone, and snapshot. More information can be found at the [SpringSource Repository FAQ](https://github.com/SpringSource/spring-framework/wiki/SpringSource-repository-FAQ).

	<repository>
		<id>spring-repo</id>
		<name>Spring Repository</name>
		<url>http://repo.springsource.org/release</url>
	</repository>	
		
	<repository>
		<id>spring-milestone</id>
		<name>Spring Milestone Repository</name>
		<url>http://repo.springsource.org/milestone</url>
	</repository>
	
	<repository>
		<id>spring-snapshot</id>
		<name>Spring Snapshot Repository</name>
		<url>http://repo.springsource.org/snapshot</url>
	</repository>

