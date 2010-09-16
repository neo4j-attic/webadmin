morpheus.provide("morpheus.components.data.relationshipManager");

/**
 * Handles creating and removing relationships, changing relationship type and setting start and end nodes.
 */
morpheus.components.data.relationshipManager = (function($, undefined) {
	
	var me = {};
	
	me.dataCore = morpheus.components.data.base;
	
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
	
	me.addRelatiohship = function(ev) {
		ev.preventDefault();
		morpheus.ui.dialog.showUsingTemplate("New relationship","components/morpheus.data/templates/new_relationship.tp", me.dialogLoaded);
	};
	
	me.saveNewRelationship = function(ev) {
		ev.preventDefault();
		var from = $("#mor_data_relationship_dialog_from").val();
		var type = $("#mor_data_relationship_dialog_type").val();
		var to = $("#mor_data_relationship_dialog_to").val();
		
		if( from.indexOf("://") ) {
			from = from.substring(from.lastIndexOf("/")+1);
		}
		
		if( ! to.indexOf("://")) {
			to = me.server().urls.rest + to;
		}
		
		me.server().rest.post("node/" + from + "/relationships", {
				"to" : to,
				"data" : {},
				"type": type
			}, function(data) {
				morpheus.components.data.base.reload();
			}
		);
		
		morpheus.ui.dialog.close();
	};
	
	/**
	 * This is called each time the create relationship dialog is shown.
	 */
	me.dialogLoaded = function() {
		// Populate from field
		var id = me.dataCore.getItem().self;
		id = id.substring(id.lastIndexOf("/") + 1);
		
		$("#mor_data_relationship_dialog_from").val(id);
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
	$(".mor_data_add_relationship").live("click", me.addRelatiohship);
	$(".mor_data_relationship_dialog_save").live("click", me.saveNewRelationship);
	
	return me.public;
	
})(jQuery);