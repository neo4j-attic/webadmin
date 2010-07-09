morpheus.provide("morpheus.components.server.monitor");

morpheus.components.server.gremlin = (function($, undefined) {
    
    var me = {};
    
    me.basePage = $("<div></div>");
    me.ui = {};
    
    me.uiLoaded  = false;
    me.server = null;
    
    me.visible = false;
    
    //
    // PUBLIC
    //
    
    me.public = {
            
            getPage :  function() {
                return me.basePage;
            },
            
            pageChanged : function(ev) {
                
                if(ev.data === "morpheus.server.gremlin") {
                    
                    me.visible = true;
                    
                    if( me.uiLoaded === false ) {
                        me.basePage.setTemplateURL("components/morpheus.server.gremlin/templates/index.tp");
                        me.render();
                    }
                    
                } else {
                    me.visible = false;
                }
            },
            
            serverChanged : function(ev) {
                
                me.jmxData = null;
                me.server = ev.data.server;
                
                // If the monitor page is currently visible, load jmx stuff
                if( me.visible === true ) {
                    
                }
                
            },
            
            init : function() {
                $( window ).bind( "hashchange", me.hashchange );
                me.hashchange();
            }
            
    };
    
    // 
    // PRIVATE
    //
    
    /**
     * Triggered when the URL hash state changes.
     */
    me.hashchange = function(ev) {
        var beanName = $.bbq.getState( "jmxbean" );
        
        if( typeof(beanName) !== "undefined" ) {
            
            me.public.findBeans(beanName, function(beans) { 
                
                if(beans.length > 0) {
                    me.currentBean = beans[0];
                    me.render();
                }
                
            });
            
        }
    };
    
    me.render = function() {
        
        me.basePage.processTemplate({
            server : me.server
        });
        
    };
    
    //
    // CONSTRUCT
    //
    
    return me.public;
    
})(jQuery);

//
// REGISTER STUFF
//

morpheus.ui.addPage("morpheus.server.gremlin",morpheus.components.server.gremlin);
morpheus.ui.mainmenu.add("Gremlin","morpheus.server.gremlin", null, "server");

morpheus.event.bind("morpheus.init", morpheus.components.server.monitor.init);
morpheus.event.bind("morpheus.ui.page.changed", morpheus.components.server.gremlin.pageChanged);
morpheus.event.bind("morpheus.server.changed",  morpheus.components.server.gremlin.serverChanged);