if exist "neo4j-rest-db/jvmargs" (
	MAVEN_OPTS=<neo4j-rest-db/jvmargs
) else (
	MAVEN_OPTS=-Xmx512M
)
mvn --settings neo4j-repo.xml compile exec:java
