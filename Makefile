run: clean
	mvn -f server/pom.xml exec:java -Dexec.mainClass="com.jeraff.patricia.PatriciaServer"

jar: clean compile
	mvn -f server/pom.xml compile assembly:single

clean:
	mvn clean

compile:
	mvn compile
