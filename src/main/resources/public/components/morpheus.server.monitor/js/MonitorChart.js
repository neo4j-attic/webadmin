morpheus.provide("morpheus.components.server.monitor.MonitorChart");

$.require("js/vend/jquery.jqplot.js");

morpheus.components.server.monitor.monitorCharts = 0;

morpheus.components.server.monitor.MonitorChart = function(server) {
	
	var me = {};
	
	me.containerId = "mor_monitor_chart_" + morpheus.components.server.monitor.monitorCharts++;
	me.container = $("<div class='mor_module span-6'><h2>Monitor</h2><div id='" + me.containerId + "'></div></div>")
	
	me.public = {
		
		render : function() {
		
		}
			
	};
	
	
	return me.public;
};