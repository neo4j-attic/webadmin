morpheus.provide("morpheus.components.serverdash");

morpheus.components.serverdash = (function($, undefined) {
   
    var me = {};
    
    // 
    // PRIVATE
    //

    me.page = $( "<div></div>" );
    me.template = "components/morpheus.server.dashboard/templates/dashboard.tp";
    me.currentServer = null;
    
    me.ui = {};
    
    me.serverChanged = function(ev) {
        
        me.currentServer = ev.data.server;
        if(me.currentServer !== null) {
            
            me.ui.name.html(me.currentServer.domain);
            me.ui.life.empty();
            me.ui.life.append( morpheus.components.Lifecycle(me.currentServer).getWidget() );
        }
        
    };
    
    //
    // CONSTRUCT
    //
    
    // Build ui
    me.page.setTemplateURL( me.template );
    me.page.processTemplate();
    
    // Cache UI lookups
    me.ui.name = $(".mor_serverdash_servername", me.page);
    me.ui.life = $(".mor_serverdash_serverlife", me.page);
    
    //
    // PUBLIC INTERFACE
    //

    me.api =
    {
        getPage : function() { return me.page; },
        serverChanged : me.serverChanged
    };

    return me.api;
    
})(jQuery);

// 
// HOOK UI
//

morpheus.ui.addPage("morpheus.server.dashboard",morpheus.components.serverdash);
morpheus.ui.mainmenu.add("Dashboard","morpheus.server.dashboard",{},"server");

morpheus.event.bind("morpheus.server.changed",morpheus.components.serverdash.serverChanged);