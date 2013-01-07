clean:
	mvn clean

compile:
	mvn compile

run: clean compile
	mvn exec:java -Dexec.mainClass="com.jeraff.patricia.server.PatriciaServer"

run-jar: jar
	java -jar target/patricia*.jar

jar: clean
	mvn compile assembly:single
