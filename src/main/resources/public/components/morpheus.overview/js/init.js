morpheus.provide("morpheus.components.overview");

morpheus.components.overview = (function() {
	
	var me = {};
	
	// 
	// PRIVATE
	//
	
	me.basePage = $("<div></div>");
	
	/**
	 * Set up the user interface.
	 */
	me.init = function() {
		me.basePage.setTemplateURL("components/morpheus.overview/templates/overview.tp");
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
// REGISTER STUFF
//

morpheus.registerComponent(morpheus.components.overview);
morpheus.ui.addPage("morpheus.overview",morpheus.components.overview);
morpheus.ui.mainmenu.addItem("Overview","morpheus.overview");