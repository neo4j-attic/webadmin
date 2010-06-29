/**
 * This is the entry point file for Morpheus.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 */

//
// MORPHEUS CORE
//

$.require("js/vend/jquery-jtemplates.js");
$.require("js/vend/jquery.bbq.js");

$.require("js/morpheus.ui.js");
$.require("js/morpheus.event.js");

/**
 * Morpheus core
 */
var morpheus = (function($) {
	
	var me = morpheus || {};
	
	// 
	// PRIVATE
	//
	
	me.components = [];
	me.initiated = false;
	
	me.init = function() {
		
		morpheus.ui.init();
		
		// Initiate components
		for( var i = 0, l = me.components.length; i < l; i++) {
			if(typeof(me.components[i].init) === "function") {
				me.components[i].init();
			}
		}
		
		me.initiated = true;
	};
	
	me.registerComponent = function(component) {
		me.components.push(component);
		
		if(me.initiated && typeof(component.init) === "function") {
			component.init();
		}
	};
	
	//
	// PUBLIC INTERFACE
	//
	
	me.api = {
			init : me.init,
			registerComponent : me.registerComponent
	};
	
	return me.api;
	
})(jQuery);

//
// GLOBAL UTILS
//

/**
 * Quick-n-dirty provide implementation, shortens down boilerplate like:
 * 
 * morpheus.something = morpheus.something || {};
 * morpheus.something.somethingelse = morpheus.something.somethingelse || {};
 * 
 * to:
 * 
 * morpheus.provide("morpheus.something.somethingelse")
 */
morpheus.provide = function(path) {
	
	var parts = path.split(".");
	parts.pop(); // Last one is class / module name, and should not be defined here.
	
	var currentScope = window;
	for( var index in parts ) {
		currentScope[ parts[index] ] = currentScope[ parts[index] ] || {};
		currentScope = currentScope[ parts[index] ];
	}
};

//
// BOOT
//

$.require("components.js");

$(function() {
	morpheus.init();
});