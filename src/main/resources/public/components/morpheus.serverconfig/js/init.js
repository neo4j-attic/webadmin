morpheus.provide("morpheus.components.serverconfig");

morpheus.components.serverconfig = (function($, undefined) {
    
    var me = {};
    
    // 
    // PRIVATE
    //
    
    me.basePage = $("<div></div>");
    me.ui = {};
    
    me.uiLoaded  = false;
    me.server = null;
    me.serverConfig = null;
    
    me.visible = false;
    
    me.getPage = function() {
        return me.basePage;
    };
    
    me.pageChanged = function(ev) {
        
        if(ev.data === "morpheus.server.config") {
            
            me.visible = true;
            
            if( me.uiLoaded === false ) {
                me.basePage.setTemplateURL("components/morpheus.serverconfig/templates/index.tp");
            }
            
            // If configuration has not been loaded for the current server
            if( me.server !== null && me.serverConfig === null ) {
                
                me.loadConfig();
                
            }
            
        } else {
            me.visible = false;
        }
    };
    
    me.serverChanged = function(ev) {
        
        me.serverConfig = null;
        me.server = ev.data.server;
        
        // If the config page is currently visible, load config stuff
        if( me.visible === true ) {
            me.loadConfig();
        }
        
    };
    
    me.loadConfig = function() {
        
        if(me.server !== null ) {
            
            me.server.admin.get("config", function(data) {
                
                me.serverConfig = data;
                
                var config = [], advanced_config = [];
                
                for( var index in data ) {
                    if(data[index].type === "DB_CREATION_PROPERTY") {
                        advanced_config.push(data[index]);
                    } else {
                        config.push(data[index]);
                    }
                }
                
                me.basePage.processTemplate({
                    config : config,
                    advanced_config : advanced_config,
                    server : me.server
                });
                
            }, function() {
                // Server unreachable
                me.basePage.processTemplate({
                    config : [],
                    server : false
                });
            });
            
        }
        
    };
    
    //
    // CONSTRUCT
    //
    
    //
    // PUBLIC INTERFACE
    //
    
    me.api = {
            getPage : me.getPage,
            pageChanged : me.pageChanged,
            serverChanged : me.serverChanged
    };
    
    return me.api;
    
})(jQuery);

//
// REGISTER STUFF
//

morpheus.ui.addPage("morpheus.server.config",morpheus.components.serverconfig);
morpheus.ui.mainmenu.add("Configuration","morpheus.server.config", null, "server");

morpheus.event.bind("morpheus.ui.page.changed", morpheus.components.serverconfig.pageChanged);
morpheus.event.bind("morpheus.server.changed",  morpheus.components.serverconfig.serverChanged);