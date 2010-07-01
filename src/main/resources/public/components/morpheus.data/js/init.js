morpheus.provide("morpheus.components.data");

$.require("components/morpheus.lifecycle/js/init.js");

morpheus.components.data = (function() {
	
	var me = {};
	
	// 
	// PRIVATE
	//
	
	me.basePage = $("<div>Hello</div>");
	
	/**
	 * Set up the user interface.
	 */
	me.init = function() {
		me.basePage.setTemplateURL("components/morpheus.data/templates/index.tp");
		me.basePage.processTemplate();
	};
	
	me.getPage = function() {
		return me.basePage;
	};
	
	//
	// PUBLIC INTERFACE
	//
	
	me.api = {
			init : me.init,
			getPage : me.getPage
	};
	
	return me.api;
	
})();

//
// REGISTER UI HOOKS
//

morpheus.ui.addPage("morpheus.data",morpheus.components.data);
//morpheus.ui.mainmenu.add("Data","morpheus.data", null, "server");