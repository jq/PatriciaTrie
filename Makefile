run:
	cd server
	mvn exec:java -Dexec.mainClass="com.jeraff.patricia.PatriciaServer"

jar:
	cd server
	mvn clean compile assembly:single
