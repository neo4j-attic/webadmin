morpheus.provide("morpheus.components.server.data.addDeleteNodeManager");

/**
 * Handles creating and removing nodes.
 */
morpheus.components.server.data.addDeleteNodeManager = (function($, undefined) { 
	
	var me = {};
	
	me.dataCore = morpheus.components.server.data.base;
	
	//
	// PUBLIC
	//
	
	me.public = {};
	
	//
	// INTERNALS
	//
	
	me.server = function() {
		return me.dataCore.getServer();
	};
	
	me.addNode = function(ev) {
		ev.preventDefault();
		me.server().rest.post("node", {}, function(data) {
			var url = data.self;
			
			// Strip the domain
			url = url.substring(url.indexOf("/", 8) + 1)
			
			// Show the node
			$.bbq.pushState({ dataurl: url });
		});
	};
	
	$(".mor_data_add_node_button").live("click", me.addNode);
	
	return me.public;
	
})(jQuery);