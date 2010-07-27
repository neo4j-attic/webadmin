morpheus.provide("morpheus.components.server.data.manage");

/**
 * Data browser.
 */
morpheus.components.server.data.manager = (function($, undefined) { 
	
	var me = {};
	
	me.listFields = ['name'];
	
	me.public = {
		getListFields : function() {
			return me.listFields;
		},
		
		serverChanged : function(ev) {
			morpheus.components.server.config.get("general.data.listfields", function(data) {
	    		me.setFieldString(data.value);
	    	}); 
		}
	};
	
	//
	// INTERNALS
	//
	
	/**
	 * Set the display fields with a comma separated string.
	 */
	me.setFieldString = function(fieldString) {
		me.listFields = [];
		var fields = fieldString.split(",");
		for(var i=0,l=fields.length; i<l; i++) {
			me.listFields.push(fields[i].trim());
		}
		
		morpheus.event.trigger("morpheus.data.listnames.changed", key);
	}
	
	// 
	// CONSTRUCT
	//
	
	$("#mor_data_listfields_button").live("click", function(ev) {
		ev.preventDefault();
		
		var fieldString = $("#mor_data_listfields").val();
		
		me.setFieldString(fieldString);
		
		// Persist the new setting
		morpheus.components.server.config.set("general.data.listfields", fieldString);
	});
	
	return me.public;
	
})(jQuery);


morpheus.event.bind("morpheus.server.changed", morpheus.components.server.data.manager.serverChanged);