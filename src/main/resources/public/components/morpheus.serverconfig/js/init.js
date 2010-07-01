morpheus.provide("morpheus.components.serverconfig");

morpheus.components.serverconfig = (function($, undefined) {
    
    var me = {};
    
    // 
    // PRIVATE
    //
    
    me.basePage = $("<div></div>");
    me.ui = {};
    me.initiated = false;
    
    me.uiLoaded = false;
    
    me.init = function() {
        
        me.initiated = true;
        
    };
    
    me.getPage = function() {
        return me.basePage;
    };
    
    me.pageShown = function() {
        if( me.uiLoaded === false ) {
            me.basePage.setTemplateURL("components/morpheus.serverconfig/templates/index.tp");
            me.basePage.processTemplate();
        }
    };
    
    //
    // CONSTRUCT
    //
    
    //
    // PUBLIC INTERFACE
    //
    
    me.api = {
            init : me.init,
            getPage : me.getPage,
            pageShown : me.pageShown
    };
    
    return me.api;
    
})(jQuery);

//
// REGISTER STUFF
//

morpheus.ui.addPage("morpheus.server.config",morpheus.components.serverconfig);
morpheus.ui.mainmenu.add("Configuration","morpheus.server.config", null, "server");

morpheus.event.bind("morpheus.init", morpheus.components.serverconfig.init );

morpheus.event.bind("morpheus.ui.page.changed", function(ev) {
    if(ev.data === "morpheus.server.config") {
        morpheus.components.serverconfig.pageShown();
    }
} );