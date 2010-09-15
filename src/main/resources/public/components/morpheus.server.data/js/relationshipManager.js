morpheus.provide("morpheus.components.server.data.relationshipManager");

/**
 * Handles creating and removing relationships, changing relationship type and setting start and end nodes.
 */
morpheus.components.server.data.relationshipManager = (function($, undefined) {
	
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
	
	me.deleteItem = function(ev) {
		ev.preventDefault();
		if( confirm("Are you sure?")) {
			morpheus.del(me.dataCore.getItem().self, function(data) {
				// Go to root node
				$.bbq.pushState({ dataurl: "node/0" });
			});
		}
	};
	
	$(".mor_data_delete_relationship_button").live("click", me.deleteItem);
	
	return me.public;
	
})(jQuery);