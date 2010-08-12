/**
 * Component imports. These are the components morpheus is shipped with.
 */

var morpheus = morpheus || {};
if(jQuery.browser.msie ) {
	morpheus.componentList = [ 
	        //'morpheus.overview',
	        'morpheus.server.lifecycle',
	        'morpheus.server.monitor',
	        //'morpheus.server.data',
	        'morpheus.server.gremlin',
	        'morpheus.server.config',
	        'morpheus.server.backup'
	];
} else {
	morpheus.componentList = [ 
	        //'morpheus.overview',
	        'morpheus.server.lifecycle',
	        'morpheus.server.monitor',
	        'morpheus.server.data',
	        'morpheus.server.gremlin',
	        'morpheus.server.config',
	        'morpheus.server.backup'
	];
}