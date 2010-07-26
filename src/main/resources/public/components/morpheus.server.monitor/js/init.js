morpheus.provide("morpheus.components.server.monitor.base");

$.require( "components/morpheus.server.monitor/js/jmx.js" );
$.require( "components/morpheus.server.monitor/js/PrimitiveCountWidget.js" );
$.require( "components/morpheus.server.monitor/js/DiskUsageWidget.js" );
$.require( "components/morpheus.server.monitor/js/CacheWidget.js" );

/**
 * Base module for the monitor component.
 * 
 * TODO: This needs to start using the jmx interface provided by neo4j instances instead of implementing it's own.
 * TODO: Break out jmx into a sub-module
 */
morpheus.components.server.monitor.base = (function($, undefined) {
    
    var me = {};
    
    me.basePage = $("<div></div>");
    me.ui = {};
    
    me.uiLoaded  = false;
    me.server = null;
    
    me.visible = false;
    
    me.valueTrackers = [];
    
    //
    // PUBLIC
    //
    
    me.public = {
            
            getPage :  function() {
                return me.basePage;
            },
            
            pageChanged : function(ev) {
                
                if(ev.data === "morpheus.server.monitor") {
                    
                	if( me.visible === false ) {
                		me.visible = true;
                    
	                    if( me.uiLoaded === false ) {
	                    	me.uiLoaded = true;
	                        me.basePage.setTemplateURL("components/morpheus.server.monitor/templates/index.tp");
	                    }
	                    
	                    me.reload();
                	}
                	
                } else {
                    me.visible = false;
                }
            },
            
            serverChanged : function(ev) {
                
                me.server = ev.data.server;
                
                // If the monitor page is currently visible
                if( me.visible === true ) {
                	me.reload();
                }
            },
            
            init : function() { }
            
    };
    
    // 
    // PRIVATE
    //
    
    me.reload = function() {
        
        me.basePage.processTemplate({
            server : me.server
        });
        
        me.destroyValueTrackers();
        
        if( me.server ) {
        	me.loadValueTrackers(me.server);
        }
        
    };
    
    me.destroyValueTrackers = function() {
    	for( var i = 0, l = me.valueTrackers.length; i < l ; i++ ) {
    		me.valueTrackers[i].stopPolling();
    	}
    	me.valueTrackers = [];
    };
    
    me.loadValueTrackers = function(server) {
    	var box = $("#mor_monitor_valuetrackers");
    	
    	var primitiveTracker = morpheus.components.server.monitor.PrimitiveCountWidget(server);
    	var diskTracker      = morpheus.components.server.monitor.DiskUsageWidget(server);
    	var cacheTracker      = morpheus.components.server.monitor.CacheWidget(server);
    	
    	me.valueTrackers.push(primitiveTracker);
    	me.valueTrackers.push(diskTracker);
    	me.valueTrackers.push(cacheTracker);
    	
    	box.append(primitiveTracker.render());
    	box.append(diskTracker.render());
    	box.append(cacheTracker.render());
    };
    
    //
    // CONSTRUCT
    //
    
    $('.mor_monitor_showjmx').live('click', function(ev) {
        
        $.bbq.pushState({
            p :"morpheus.server.monitor.jmx"
        });
    
        ev.preventDefault();
    });
    
    return me.public;
    
})(jQuery);

//
// REGISTER STUFF
//

morpheus.ui.addPage("morpheus.server.monitor",morpheus.components.server.monitor.base);
morpheus.ui.mainmenu.add("Monitor","morpheus.server.monitor", null, "server");

morpheus.event.bind("morpheus.init", morpheus.components.server.monitor.base.init);
morpheus.event.bind("morpheus.ui.page.changed", morpheus.components.server.monitor.base.pageChanged);
morpheus.event.bind("morpheus.server.changed",  morpheus.components.server.monitor.base.serverChanged);