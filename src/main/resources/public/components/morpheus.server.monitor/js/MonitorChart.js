morpheus.provide("morpheus.components.server.monitor.MonitorChart");

$.require("js/vend/jquery.jqplot.js");

morpheus.components.server.monitor.monitorCharts = 0;

morpheus.components.server.monitor.MonitorChart = function(server) {
	
	var me = {};
	
	me.server = server;
	me.containerId = "mor_monitor_chart_" + morpheus.components.server.monitor.monitorCharts++;
	me.container = $("<div class='mor_module mor_span-5'><h2>Number of nodes</h2><div id='" + me.containerId + "'></div></div>")
	
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
		$.jqplot(me.containerId,   me.parseData(data),
				{ title:'Monitor',
				  axes:{
					yaxis:{min:0, max:40},
					xaxis:{min:data.end_time - (1000 * 60),max:data.end_time}
				  },
				  series:[{color:'#5FAB78'},{color:'#AB5F78'},{color:'#AB785F'}]
				});
	};
	
	me.parseData = function(data) {
		var nodeCount = [];
		var relCount = [];
		var propCount = [];
		
		for( var i = 0, l = data.timestamps.length; i < l; i++ ) {
			nodeCount.push([ data.timestamps[i], data.data["node_count"][i] ]);
			relCount.push([ data.timestamps[i], data.data["relationship_count"][i] ]);
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