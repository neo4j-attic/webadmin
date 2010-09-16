morpheus.provide("morpheus.components.io.init");

/**
 * Handles imports and exports.
 */
morpheus.components.io.init = (function($, undefined) {
	
	var me = {};
	
	me.basePage = $("<div></div>");
	me.uiLoaded  = false;
	
	//
	// PUBLIC
	//
	
	me.public = {
		getPage :  function() {
		    return me.basePage;
		}
	};
	
	//
	// INTERNALS
	//
	
	me.render = function() {
	    	
        me.basePage.processTemplate();
        
    };
    
    me.pageChanged = function(ev) {
        if(ev.data === "morpheus.io") {
            
            if( me.uiLoaded === false ) {
                me.uiLoaded = true;
                me.basePage.setTemplateURL("components/morpheus.io/templates/index.tp");
                me.render();
            }
        }
    };
    
	
	//
	// LISTEN TO THE WORLD
	//
	
	morpheus.ui.addPage("morpheus.io",me.public);
	morpheus.ui.mainmenu.add("Import / Export","morpheus.io", null, "server", 8);
	
	morpheus.event.bind("morpheus.ui.page.changed", me.pageChanged);
	
	return me.public;
	
})(jQuery);