morpheus.provide("morpheus.components.io.init");

/**
 * Handles imports and exports.
 */
morpheus.components.io.init = (function($, undefined) {
	
	var me = {};
	
	me.basePage = $("<div></div>");
	me.uiLoaded  = false;
	me.uploadUrl = "";
	
	
	//
	// INTERNALS
	//
	
	me.render = function() {
	    if( me.uiLoaded ) {
	        me.basePage.processTemplate({uploadUrl:me.uploadUrl});
	    }
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
	
	morpheus.ui.MainMenu.add({ label : "Import / Export", pageKey:"morpheus.io", index:8, requiredServices:['importing','exporting'], perspectives:['server']});
	
	morpheus.event.bind("morpheus.ui.page.changed", me.pageChanged);
	morpheus.event.bind("morpheus.servers.current.changed", function(ev) {
	    var server = morpheus.Servers.getCurrentServer();
	    server.manage.importing.getUploadUrl(function(url){
	        me.uploadUrl = url;
	        me.render();
	    })
	});
	
	return {
        getPage :  function() {
            return me.basePage;
        }
    };;
	
})(jQuery);


morpheus.ui.Pages.add("morpheus.io",morpheus.components.io.init);