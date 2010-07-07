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
    
    /**
     * Configuration lookup map for the server currently beeing operated on. 
     */
    me.config = null;
    
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
            if( me.server !== null && me.config === null ) {
                
                me.loadConfig();
                
            }
            
        } else {
            me.visible = false;
        }
    };
    
    me.serverChanged = function(ev) {
        
        me.config = null;
        me.server = ev.data.server;
        
        // If the config page is currently visible, load config stuff
        if( me.visible === true ) {
            me.loadConfig();
        }
        
    };
    
    me.getUncommittedChanges = function(args) {
        var args = args || {};
        var changed = [];
        for( var key in me.config ) {
            if( me.config[key].newValue !== undefined && ( !args.excludeType || me.config[key].type !== args.excludeType)) {
                changed.push({ 'key' : key, 'value' : me.config[key].newValue });
            }
        }
        
        return changed;
    };
    
    /**
     * Set all changes committed.
     */
    me.allChangesCommitted = function(args) {
        var args = args || {};
        for( var key in me.config) {
            if( me.config[key].newValue !== undefined && ( !args.excludeType || me.config[key].type !== args.excludeType)) {
                me.config[key].value = me.config[key].newValue;
                delete(me.config[key].newValue);
            }
        }
        
        $(".mor_config_value",me.basePage).removeClass('uncommitted');
    };
    
    /**
     * Enable save button if there are uncommitted changes. Disable it otherwise.
     */
    me.updateSaveButtonState = function() {
        if( me.getUncommittedChanges({excludeType:"DB_CREATION_PROPERTY"}).length > 0 ) {
            $("input#mor_setting_save",me.basePage).removeAttr('disabled');
        } else {
            $("input#mor_setting_save",me.basePage).attr('disabled', 'disabled');
        }
    };
    
    me.loadConfig = function() {
        
        if(me.server !== null ) {
            
            me.server.admin.get("config", function(data) {
                
                me.config = {};
                
                var config = [], advanced_config = [], jvm_config = [];
                
                for( var index in data ) {
                    if(data[index].type === "DB_CREATION_PROPERTY") {
                        advanced_config.push(data[index]);
                    } else if(data[index].type === "JVM_ARGUMENT") {
                        jvm_config.push(data[index]);
                    } else {
                        config.push(data[index]);
                    }
                    
                    me.config[data[index].key] = data[index];
                }
                
                me.basePage.processTemplate({
                    config : config,
                    jvm_config: jvm_config,
                    advanced_config : advanced_config,
                    server : me.server
                });
                
            }, function() {
                
                me.config = {};
                
                
                // Server unreachable
                me.basePage.processTemplate({
                    config : [],
                    server : false
                });
            });   
        }  
    };
    
    /**
     * Hook event listeners to the UI.
     */
    $('.mor_config_value').live('change',function(ev){
        var el = $(ev.originalTarget);
        var value = el.val().trim();
        var key   = el.attr('name');
        
        if( me.config[key] !== undefined ) {
            if( value !== me.config[key].value ) {
                me.config[key].newValue = value;
                el.addClass("uncommitted");
            } else {   
                delete(me.config[key].newValue);
                el.removeClass("uncommitted");
            }
            
            me.updateSaveButtonState();
        }
    });
    
    /**
     * Saving changes to normal config settings.
     */
    $("#mor_setting_save").live('click',function() {
        
        // Find all settings that are changed
        var changed = me.getUncommittedChanges({excludeType:"DB_CREATION_PROPERTY"});
        
        if(changed.length > 0) {
            
            // Disable controls while saving
            $("input",me.basePage).attr('disabled', 'disabled');
            
            me.server.admin.post("config",{value:JSON.stringify(changed)},function(data){
                me.allChangesCommitted({excludeType:"DB_CREATION_PROPERTY"});
                
                $("input",me.basePage).removeAttr('disabled');
                me.updateSaveButtonState();
            }, function(ev){
                $("input",me.basePage).removeAttr('disabled');
                me.updateSaveButtonState();
            });
        }
    });
    
    /**
     * Savings changes to advanced (re-do-database) settings.
     */
    $("#mor_setting_advanced_save").live('click',function() {
        
    });
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