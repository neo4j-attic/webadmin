morpheus.provide("morpheus.components.server.monitor.base");

$.require( "components/morpheus.server.monitor/js/jmx.js" );
$.require( "components/morpheus.server.monitor/js/PrimitiveCountWidget.js" );
$.require( "components/morpheus.server.monitor/js/DiskUsageWidget.js" );
$.require( "components/morpheus.server.monitor/js/CacheWidget.js" );
$.require( "components/morpheus.server.monitor/js/MonitorChart.js" );

/**
 * Base module for the monitor component.
 */
morpheus.components.server.monitor.base = (function($, undefined) {
    
    var me = {};
    
    me.basePage = $("<div></div>");
    me.ui = {};
    
    me.uiLoaded  = false;
    me.server = null;
    
    me.visible = false;
    
    me.valueTrackers = [];
    me.charts = [];
    
    //
    // PUBLIC
    //
    
    me.public = {
            
            getPage :  function() {
                return me.basePage;
            },
            
            pageChanged : function(ev) {
                
                if(ev.data === "morpheus.server.monitor") {
                    
            		me.visible = true;
                
                    if( me.uiLoaded === false ) {
                    	me.uiLoaded = true;
                        me.basePage.setTemplateURL("components/morpheus.server.monitor/templates/index.tp");
	                    
	                    me.reload();
                    } else {
                    	me.runMonitors();
                    }
                	
                } else {
                    me.visible = false;
                    me.haltMonitors();
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
        
        me.destroyMonitors();
        
        if( me.server ) {
        	
        	me.server.startMonitoring();
        	
        	me.loadMonitors(me.server);
        	$("#mor_monitor_lifecycle").empty();
        	$("#mor_monitor_lifecycle").append( morpheus.components.Lifecycle(me.server).render() );
        }
        
    };
    
    me.destroyMonitors= function() {
    	me.haltMonitors();
    	
    	me.valueTrackers = [];
    	me.charts = [];
    };
    
    me.haltMonitors = function() {
    	for( var i = 0, l = me.charts.length; i < l ; i++ ) {
			me.charts[i].stopDrawing();
    	}
    	
    	for( var i = 0, l = me.valueTrackers.length; i < l ; i++ ) {
			me.valueTrackers[i].stopPolling();
    	}
    };
    
    me.runMonitors = function() {
    	for( var i = 0, l = me.charts.length; i < l ; i++ ) {
			me.charts[i].startDrawing();
    	}
    	
    	for( var i = 0, l = me.valueTrackers.length; i < l ; i++ ) {
			me.valueTrackers[i].startPolling();
    	}
    };
    
    me.loadMonitors = function(server) {
    	var box = $("#mor_monitor_valuetrackers");
    	
    	var primitiveTracker = morpheus.components.server.monitor.PrimitiveCountWidget(server);
    	var diskTracker      = morpheus.components.server.monitor.DiskUsageWidget(server);
    	var cacheTracker      = morpheus.components.server.monitor.CacheWidget(server);
    	
    	var primitivesChart = morpheus.components.server.monitor.MonitorChart(server, {
    		label : 'Primitive entitites',
    		data : {
    		    node_count : {
				    label : 'Nodes'
				},
				relationship_count : {
				    label : 'Relationships'
			    },
			    property_count : {
				    label : 'Properties'
			    }
    		}
    	});
    	
    	var memoryChart = morpheus.components.server.monitor.MonitorChart(server, {
    		label : 'Heap memory usage',
    		data : {
    			memory_usage_percent : {
				    label : 'Heap memory usage',
				}
    		},
    		yaxis : {
    			min : 0,
    			max : 100
    		},
    		series : {
    			lines: { show: true, fill: true, fillColor: "#4f848f" }
    		}
    	});
    	
    	
    	
    	me.valueTrackers.push(primitiveTracker);
    	me.valueTrackers.push(diskTracker);
    	me.valueTrackers.push(cacheTracker);
    	
    	me.charts.push(primitivesChart);
    	me.charts.push(memoryChart);

    	box.append(primitivesChart.render());
    	box.append(memoryChart.render());
    	box.append(primitiveTracker.render());
    	box.append(diskTracker.render());
    	box.append(cacheTracker.render());
    	
    	primitivesChart.startDrawing();
    	memoryChart.startDrawing();
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
morpheus.ui.mainmenu.add("Dashboard","morpheus.server.monitor", null, "server");

morpheus.event.bind("morpheus.init", morpheus.components.server.monitor.base.init);
morpheus.event.bind("morpheus.ui.page.changed", morpheus.components.server.monitor.base.pageChanged);
morpheus.event.bind("morpheus.server.changed",  morpheus.components.server.monitor.base.serverChanged);