/**
 * Component imports. These are the components morpheus is shipped with.
 */

var morpheus = morpheus || {};
if(jQuery.browser.msie ) {
	// Temporary hack, IE screws up the interaction between server.data and server.config somehow.
	// Rather than fixing it, we wait for a planned refactoring to remove the interaction in the first place.
	morpheus.componentList = [
	        'morpheus.lifecycle',
	        'morpheus.monitor',
	        //'morpheus.data',
	        'morpheus.gremlin',
	        'morpheus.config',
	        'morpheus.backup'
	];
} else {
	morpheus.componentList = [
	        'morpheus.lifecycle',
	        'morpheus.monitor',
	        'morpheus.data',
	        'morpheus.gremlin',
	        'morpheus.config',
	        'morpheus.backup'
	];
}