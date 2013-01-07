clean:
	mvn clean

compile:
	mvn compile

run: clean compile
	mvn exec:java -Dexec.mainClass="com.jeraff.patricia.server.PatriciaServer"

run-jar: jar
	java -jar target/PatriciaTrie-0.5.0-jar-with-dependencies.jar

jar: clean
	mvn compile assembly:single
