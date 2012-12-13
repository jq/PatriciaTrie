run:
	mvn exec:java -Dexec.mainClass="com.jeraff.patricia.PatriciaServer"

jar:
	mvn clean compile assembly:single
