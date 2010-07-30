morpheus.provide("morpheus.components.server.monitor.MonitorChart");

$.require("js/vend/jquery.jqplot.js");
$.require("js/vend/jqplot-plugins/jqplot.dateAxisRenderer.js");

morpheus.components.server.monitor.monitorCharts = 0;

/**
 * A chart that shows data from one or more monitor data sources over time. The
 * data is aquired via the morpheus.server.monitor.update event, which is
 * triggered for each loaded server at regular intervals.
 * 
 * Available settings
 * 
 * @param server
 *            is the server to work with
 * @param settings
 *            is an object that specifies what data to show and how. See above
 *            for available settings.
 */
morpheus.components.server.monitor.MonitorChart = function(server, settings) {
	
	var me = {};
	
	// Default settings
	me.settings = {
		'label' : "",
		'height' : 200,
	};
	
	// Override defaults with user settings
	var settings = settings || {};
	$.extend( true, me.settings, settings );
	
	me.server = server;
	me.containerId = "mor_monitor_chart_" + morpheus.components.server.monitor.monitorCharts++;
	me.container = $("<div class='mor_module mor_span-5'><h2>" + me.settings.label + "</h2><div class='mor_chart_container'><div style='height:"+me.settings.height+"px;' id='" + me.containerId + "'></div></div></div>")
	
	me.drawing = false;
	
	me.public = {
		
		render : function() {
			return me.container;
		},
		
		startDrawing : function() {
			if ( ! me.drawing ) {
				
				me.drawing = true;
				
			}
		},
		
		stopDrawing : function() {
			me.drawing = false;
		}
			
	};
	
	//
	// INTERNALS
	//
	
	me.draw = function(data) {
		$("#" + me.containerId).empty();
		$.jqplot(me.containerId, me.parseData(data),
				{ axes:{
					yaxis:{
						min : 0,
						autoscale : true
					},
					xaxis:{
						min:data.end_time - (1000 * 60),
						max:data.end_time,
						renderer:$.jqplot.DateAxisRenderer,
						tickOptions : {
							formatString : "%H:%M:%S"
						},
					}
				  },
				  legend : {
					  show : true,
					  location : 'nw'
				  },
				  series:[{
					  label : 'Nodes'
				  },{
					  label : 'Relationships'
				  },{
					  label : 'Properties'
				  }]
				});
	};
	
	me.parseData = function(data) {
		var nodeCount = [];
		var relCount = [];
		var propCount = [];
		
		for( var i = 0, l = data.timestamps.length; i < l; i++ ) {
			nodeCount.push([ data.timestamps[i], data.data["node_count"][i] ]);
			relCount.push([ data.timestamps[i],  data.data["relationship_count"][i] ]);
			propCount.push([ data.timestamps[i], data.data["property_count"][i] ]);
		}
		
		return [nodeCount,relCount,propCount];
	};
	
	// Listen for data updates
	morpheus.event.bind("morpheus.server.monitor.update", function(ev) {
		if( me.drawing && ev.data.server === me.server ) {
			me.draw( ev.data.allData );
		}
		
	});
	
	return me.public;
};