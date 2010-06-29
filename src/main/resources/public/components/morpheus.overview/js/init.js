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
		
		// Temporary
		v = (morpheus.components.Lifecycle()).getWidget();
		me.basePage.processTemplate();
		me.basePage.append(v);
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

morpheus.ui.addPage("morpheus.overview",morpheus.components.overview);
morpheus.ui.mainmenu.addItem("Overview","morpheus.overview");

morpheus.event.bind( "morpheus.init", function(ev) {
	morpheus.components.overview.init();
});