morpheus.provide("morpheus.components.server.monitor.PrimitiveCountWidget");

/**
 * Used to keep track of the current status of a neo4j server.
 * 
 * @param server is the server instance to track
 * @param interval (optional) is the update interval in milliseconds. The default is 10000.
 */
morpheus.components.server.monitor.PrimitiveCountWidget = function(server, interval) {
	
	var me = {};
	
	me.server = server;
	
	//
	// PUBLIC
	//
	
	me.public = {
	};
	
	//
	// INTERNALS
	//
	
	//
	// CONSTRUCT
	// 
	
	return me.public;
};