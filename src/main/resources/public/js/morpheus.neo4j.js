morpheus.provide("morpheus.neo4j");

/**
 * Contains resources required to connect to a running neo4j instance.
 */
morpheus.neo4j = function(url, restPort, jmxPort) {
	
	var me = {};
	
	//
	// PRIVATE
	//
	
	me.jmxPort = jmxPort || 9998;
	me.restPort = restPort || 9999;
	
	me.url = url;
	
	//
	// CONSTRUCT
	//
	
	
	
	//
	// PUBLIC API
	//
	
	me.api = {};
	
	return me.api;
	
};