Morpheus - keeping track of Neo4j
=================================

This is a stand-alone, 
full feature Neo4j distribution that exposes a Neo4j database over 
REST together with a web-based administrative interface for said database.

It is currently in a very early stage, but new features are added on almost a daily basis.

<img src="http://github.com/downloads/neo4j/webadmin/config-small.png" />

Quickstart
------------
To try the admin interface out:

	git clone git@github.com:neo4j/webadmin.git
	cd webadmin
	./start
	

Build production version
------------------------
Morpheus is packaged as a java service. To trigger a production build, simply do:

	mvn package
	
Deploy the result where you see fit:

	tar xvf target/neo4j-webadmin-VERSION.tar.gz
	mv neo4j-webadmin-VERSION /wherever/you/like
	
Start the service

	/wherever/you/like/bin/webadmin start