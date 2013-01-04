run:
	mvn -f server/pom.xml exec:java -Dexec.mainClass="com.jeraff.patricia.PatriciaServer"

jar:
	mvn -f server/pom.xml clean compile assembly:single
