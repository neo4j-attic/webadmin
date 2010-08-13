/**
 * Component imports. These are the components morpheus is shipped with.
 */

var morpheus = morpheus || {};
if(jQuery.browser.msie ) {
	// Temporary hack, IE screws up the interaction between server.data and server.config somehow.
	// Rather than fixing it, we wait for a planned refactoring to remove the interaction in the first place.
	morpheus.componentList = [
	        'morpheus.server.lifecycle',
	        'morpheus.server.monitor',
	        //'morpheus.server.data',
	        'morpheus.server.gremlin',
	        'morpheus.server.config',
	        'morpheus.server.backup'
	];
} else {
	morpheus.componentList = [
	        'morpheus.server.lifecycle',
	        'morpheus.server.monitor',
	        'morpheus.server.data',
	        'morpheus.server.gremlin',
	        'morpheus.server.config',
	        'morpheus.server.backup'
	];
}