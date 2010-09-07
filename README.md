Neo4j Monitoring and Management Tool
=================================

This is a stand-alone, 
full feature Neo4j distribution that exposes a Neo4j database over 
REST together with a web-based administrative interface for said database.

It is currently in a very early stage, but new features are added on almost a daily basis.

Current features include:

<ul>
    <li>Lifecycle management</li>
    <li>Server configuration</li>
    <li>Monitoring of memory usage, disk usage, cache status and database primitives</li>
    <li>Historic data archiving and charting via built - in Round Robin Database</li>
    <li>JMX overview</li>
    <li>Data browsing</li>
    <li>Advanced data manipulation and exploration via Gremlin console</li>
    <li>Server configuration</li>
    <li>Online backups</li>
    <li>Charting of relevant data</li>
</ul>

Quickstart
------------
To try it out:

	git clone http://github.com/neo4j/webadmin.git
	cd webadmin
	./start
	
The administration pages will be available at <a href="http://localhost:9988/">http://localhost:9988/</a> and a neo4j REST server connected to it will be available at <a href="http://localhost:9999/">http://localhost:9999/</a>.

Run production version
--------------------------

Download the latest distribution as a <a href="http://m2.neo4j.org/org/neo4j/neo4j-webadmin/0.0.1-SNAPSHOT/neo4j-webadmin-0.0.1-SNAPSHOT.zip">zip</a> or as a <a href="http://m2.neo4j.org/org/neo4j/neo4j-webadmin/0.0.1-SNAPSHOT/neo4j-webadmin-0.0.1-SNAPSHOT.tar.gz">tarball</a>.

Unpack in your favourite folder and start by running:

    *nix:
    ./bin/neo4j-webadmin start
    
    Windows:
    bin\neo4j-webadmin.bat start
    
The administration pages will be available at <a href="http://localhost:9988/">http://localhost:9988/</a> and a neo4j REST server connected to it will be available at <a href="http://localhost:9999/">http://localhost:9999/</a>.

Build production version
------------------------
Neo4j Monitoring and Management Tool is packaged as a java service. To trigger a production build, simply do:

	mvn package
	
Deploy the result where you see fit:

	tar xvf target/neo4j-webadmin-VERSION.tar.gz
	mv neo4j-webadmin-VERSION /wherever/you/like
	
Start the service

	/wherever/you/like/bin/webadmin start
	
	
Screenshots 
-----------

<a href="http://github.com/neo4j/webadmin/raw/master/src/main/screenshots/dashboard.jpg"><img src="http://github.com/neo4j/webadmin/raw/master/src/main/screenshots/dashboard-small.jpg" /></a>
<a href="http://github.com/neo4j/webadmin/raw/master/src/main/screenshots/data.jpg"><img src="http://github.com/neo4j/webadmin/raw/master/src/main/screenshots/data-small.jpg" /></a>
<a href="http://github.com/neo4j/webadmin/raw/master/src/main/screenshots/gremlin.jpg"><img src="http://github.com/neo4j/webadmin/raw/master/src/main/screenshots/gremlin-small.jpg" /></a>
<a href="http://github.com/neo4j/webadmin/raw/master/src/main/screenshots/settings.jpg"><img src="http://github.com/neo4j/webadmin/raw/master/src/main/screenshots/settings-small.jpg" /></a>
<a href="http://github.com/neo4j/webadmin/raw/master/src/main/screenshots/backup.jpg"><img src="http://github.com/neo4j/webadmin/raw/master/src/main/screenshots/backup-small.jpg" /></a>
