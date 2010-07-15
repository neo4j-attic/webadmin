morpheus.provide("morpheus.components.server.backup");

morpheus.components.server.backup = (function($, undefined) {
    
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
                
                if(ev.data === "morpheus.server.backup") {
                    
                    me.visible = true;
                    
                    if( me.uiLoaded === false ) {
                        me.uiLoaded = true;
                        me.basePage.setTemplateURL("components/morpheus.server.backup/templates/index.tp");
                        me.render();
                    }
                    
                } else {
                    me.visible = false;
                }
            },
            
            serverChanged : function(ev) {
                
                me.jmxData = null;
                me.server = ev.data.server;
                
                if( me.visible === true ) {
                     
                }
                
            },
            
            init : function() {
            }
            
    };
    
    // 
    // PRIVATE
    //
    
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

morpheus.ui.addPage("morpheus.server.backup",morpheus.components.server.backup);
morpheus.ui.mainmenu.add("Backups","morpheus.server.backup", null, "server");

morpheus.event.bind("morpheus.init", morpheus.components.server.backup.init);
morpheus.event.bind("morpheus.ui.page.changed", morpheus.components.server.backup.pageChanged);
morpheus.event.bind("morpheus.server.changed",  morpheus.components.server.backup.serverChanged);