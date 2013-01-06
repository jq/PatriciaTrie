clean:
	mvn clean

compile:
	mvn compile

run: clean compile
	mvn exec:java -Dexec.mainClass="com.jeraff.patricia.server.PatriciaServer"

jar: clean
	mvn compile assembly:single
