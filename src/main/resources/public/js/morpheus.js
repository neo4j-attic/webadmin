/**
 * Morpheus boot script.
 */

var morpheus = morpheus || {};
morpheus.provide = function(path) {
	
	var parts = path.split(".");
	parts.pop(); // Last one is class / module name, and should not be defined here.
	
	var currentScope = window;
	for( var index in parts ) {
		currentScope[ parts[index] ] = currentScope[ parts[index] ] || {};
		currentScope = currentScope[ parts[index] ];
	}
};

$.require("js/vend/jquery-jtemplates.js");
$.require("js/base/views/RootView.js");

$(function() {
	$("#morpheus").setTemplate( morpheus.base.views.RootView );
	$("#morpheus").processTemplate({});
});