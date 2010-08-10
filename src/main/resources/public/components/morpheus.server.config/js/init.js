morpheus.provide("morpheus.components.server.config");

/**
 * A component for modifying the configuration settings on the "current" server.
 * 
 * TODO: This has turned into major poop. The actual config getting/setting should be moved
 *       to morpheus.neo4j, and be loaded by default when a server is loaded. This component
 *       should simply provide a UI to that.
 */
morpheus.components.server.config = (function($, undefined) {
    
    var me = {};
    
    // 
    // PRIVATE
    //
    
    me.basePage = $("<div></div>");
    me.ui = {};
    
    me.uiLoaded = false;
    me.uiNeedsUpdate = true;
    me.server = null;
    
    /**
     * Configuration lookup map for the server currently beeing operated on. 
     */
    me.config = null;
    me.loadingConfig = false;
    
    /**
     * Pending requests for config data, these will be processed once data has loaded.
     */
    me.pendingConfigRequests = [];
    
    me.visible = false;
    
    me.getPage = function() {
        return me.basePage;
    };
    
    me.pageChanged = function(ev) {
        
        if(ev.data === "morpheus.server.config") {
            
            me.visible = true;
            
            if( me.uiLoaded === false ) {
            	me.uiLoaded = true;
                me.basePage.setTemplateURL("components/morpheus.server.config/templates/index.tp");
            }
            
            // If configuration has not been loaded for the current server
            if( me.server !== null && me.config === null && me.loadingConfig === false ) {
                me.loadConfig();   
            } else if ( me.config !== null ) {
            	me.render();
            }
            
        } else {
            me.visible = false;
        }
    };
    
    me.serverChanged = function(ev) {
        
        me.config = null;
        me.server = ev.data.server;
        
        // If the config page is currently visible, load config stuff
        me.loadConfig();
        
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
            
        	me.loadingConfig = true;
        	
            me.server.admin.get("config", function(data) {
                
                me.config = {};
                
                for( var index in data ) {
                    me.config[data[index].key] = data[index];
                }
                
                // Process pending config requests
                for( var index in me.pendingConfigRequests ) {
                	me.pendingConfigRequests[index].cb( me.config[me.pendingConfigRequests[index].key] );
                }
                
                me.render();
                
                me.loadingConfig = false;
                
            }, function() {
                
                me.config = {};

                // Server unreachable
                me.render();
                
                me.loadingConfig = false;
            });   
        }  
    };
    
    me.render = function() {
    	
    	if( me.uiLoaded ) {
	    	var config = [], advanced_config = [], jvm_config = [], general_config = [];
	        
	        for( var index in me.config ) {
	            if(me.config[index].type === "DB_CREATION_PROPERTY") {
	                advanced_config.push(me.config[index]);
	            } else if(me.config[index].type === "JVM_ARGUMENT") {
	                jvm_config.push(me.config[index]);
	            } else if(me.config[index].type === "GENERAL_PROPERTY") {
	                general_config.push(me.config[index]);
	            } else {
	                config.push(me.config[index]);
	            }
	        }
	    	
	    	me.basePage.processTemplate({
	            config : config,
	            jvm_config: jvm_config,
	            advanced_config : advanced_config,
	            server : me.server
	        });
    	}
    };
    
    me.configValueChanged = function(ev){
        var el = $(ev.target);
        
        var key = el.attr('name');

        if( me.config[key] !== undefined ) {
        	
        	// Handle checkboxes
	        if( el.attr("type") === "checkbox" ) {
	        	if(el.attr('checked')) {
	        		var value = me.config[key].definition.values[0].value;
	        	} else {
	        		if(me.config[key].definition.values.length > 1) {
		        		// Toggle between two values
	        			var value = me.config[key].definition.values[1].value;
	        		} else {
	        			// Toggle between some value and no value at all
	        			var value = "";
	        		}
	        	}
	        } else {
	        	var value = el.val().trim();
	        }
        
	        // Handle everything else
            if( value !== me.config[key].value ) {
                me.config[key].newValue = value;
                el.addClass("uncommitted");
            } else {   
                delete(me.config[key].newValue);
                el.removeClass("uncommitted");
            }
            
            me.updateSaveButtonState();
        }
    };
    
    //
    // CONSTRUCT
    //
    
    /**
     * Hook event listeners to the UI.
     */
    $('.mor_config_value').live('keyup',me.configValueChanged);
    $('.mor_config_value').live('change',me.configValueChanged);
    
    /**
     * Saving changes to normal config settings.
     */
    $("#mor_setting_save").live('click',function() {
        
        // Find all settings that are changed
        var changed = me.getUncommittedChanges({excludeType:"DB_CREATION_PROPERTY"});
        
        if(changed.length > 0) {
            
            // Disable controls while saving
            $("input",me.basePage).attr('disabled', 'disabled');
            
            // Check if username or password has changed
            var authChanged = false;
            for( var item in changed ) {
            	
            	if(item.key === "rest_username") {
            		me.server.setUsername(item.value);
            		authChanged = true;
            	}
            	
            	if(item.key === "rest_password") {
            		me.server.setPassword(item.value);
            		authChanged = true;
            	}
            }
            
            if ( authChanged ) {
            	me.server.save();
            }
            
            // Commit changes to server
            me.server.admin.post("config",changed,function(data){
            	
                me.allChangesCommitted({excludeType:"DB_CREATION_PROPERTY"});
                
                $("input",me.basePage).removeAttr('disabled');
                me.updateSaveButtonState();
                
            }, function(ev){
            	
                $("input",me.basePage).removeAttr('disabled');
                me.updateSaveButtonState();
                
            });
        }
    });
    
    //
    // PUBLIC INTERFACE
    //
    
    me.public = {
        getPage : me.getPage,
        pageChanged : me.pageChanged,
        serverChanged : me.serverChanged,
    
        /**
         * Set a single value and apply it directly to the server.
         * @param key is the key of the parameter to change
         * @param val is the value to change it to
         * @param cb will be called when the value has been changed, with a boolean 
         *        true argument if successful or boolean false and an exception response 
         *        if attempt failed. 
         */
        set : function(key, val, cb) {
    		var changed = [
    		    { 'key' : key, 'value' : val }
    		];
    		
	    	me.server.admin.post("config",changed,function(data){
	    		// Update the UI if applicable
	    		if ( me.config[key].type === "JVM_ARGUMENT" ||
                     me.config[key].type === "CONFIG_PROPERTY") {
	    			$("#mor_setting_" + key).val(val);	
	    		}
	    		
	    		cb( true );
	    		
	        }, function(ev){

	    		cb( false, ev );
	        	
	        });
		},
    
		/**
		 * Get some setting. 
		 * @param key is the key to fetch
		 * @param cb is a callback that will be called with the config data
		 */
    	get : function(key, cb) {
			if( me.config === null ) {
				me.pendingConfigRequests.push({ key:key,cb:cb });
				if( me.loadingConfig === false ) {
					me.loadConfig();
				}
			} else {
				cb( me.config[key] );
			}
		}
    };
    
    return me.public;
    
})(jQuery);

//
// REGISTER STUFF
//

morpheus.ui.addPage("morpheus.server.config",morpheus.components.server.config);
morpheus.ui.mainmenu.add("Configuration","morpheus.server.config", null, "server");

morpheus.event.bind("morpheus.ui.page.changed", morpheus.components.server.config.pageChanged);
morpheus.event.bind("morpheus.server.changed",  morpheus.components.server.config.serverChanged);