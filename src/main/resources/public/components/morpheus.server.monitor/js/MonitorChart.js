morpheus.provide("morpheus.components.server.monitor.MonitorChart");

$.require("js/vend/jquery.jqplot.js");

morpheus.components.server.monitor.monitorCharts = 0;

morpheus.components.server.monitor.MonitorChart = function(server) {
	
	var me = {};
	
	me.server = server;
	me.containerId = "mor_monitor_chart_" + morpheus.components.server.monitor.monitorCharts++;
	me.container = $("<div class='mor_module span-6'><h2>Monitor</h2><div id='" + me.containerId + "'></div></div>")
	
	me.drawing = false;
	
	me.public = {
		
		render : function() {
		
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
		
	};
	
	me.parseData = function(data) {
		var out = [];
		for( var i = 0, l = data.timestamps.length; i < l; i++ ) {
			
		}
		console.log(out);
		return out;
	};
	
	// Listen for data updates
	morpheus.event.bind("morpheus.server.monitor.update", function(ev) {
		
		if( ev.server === me.server ) {
			me.draw( me.parseData(ev.allData) );
		}
		
	});
	
	return me.public;
};