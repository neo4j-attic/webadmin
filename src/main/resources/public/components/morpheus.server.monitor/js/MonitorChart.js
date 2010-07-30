morpheus.provide("morpheus.components.server.monitor.MonitorChart");

$.require("js/vend/jquery.jqplot.js");
$.require("js/vend/jqplot-plugins/jqplot.dateAxisRenderer.js");

morpheus.components.server.monitor.monitorCharts = 0;

/**
 * A chart that shows data from one or more monitor data sources over time. The
 * data is acquired via the morpheus.server.monitor.update event, which is
 * triggered for each loaded server at regular intervals.
 * 
 * Available settings include:
 * 
 * <ul>
 * <li>label : the label for the module that the graph is shown in</li>
 * <li>height : integer value, height in pixels of the chart, default is 200</li>
 * <li>data : an object with data source definitions. Each key in this object
 * should correspond to a data set key used by the rrdb monitor system. See
 * {@link org.neo4j.webadmin.rrd.RrdManager}. For available settings in each
 * data set, see below</li>
 * </ul>
 * 
 * Data sources have the following settings:
 * 
 * <ul>
 * <li>label : is the label to show in the legend</li>
 * </ul>
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
		label : "",
		yMin : 0,
		yMax : null,
		xMin : null,
		xMax : 0,
		height : 200,
		data : []
	};
	
	// Override defaults with user settings
	var settings = settings || {};
	$.extend( true, me.settings, settings );
	
	// Define data series
	me.series = [];
	for( var key in me.settings.data ) {
		me.settings.data[key].key = key;
		me.series.push(me.settings.data[key]);
	}
	
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
						min : me.settings.yMin,
						max : me.settings.yMax,
						autoscale : true
					},
					xaxis:{
						min : me.settings.xMin,
						max : me.settings.xMax,
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
				  series:me.series
				});
	};
	
	me.parseData = function(data) {
		var output = [];
		var numberOfDataSeries = me.series.length;
		
		// Initialize data arrays for all data series
		for( var dataIndex = 0; dataIndex < numberOfDataSeries; dataIndex ++ ) {
			output[dataIndex] = [];
		}
		
		// Format data for jqChart
		for( var i = 0, l = data.timestamps.length; i < l; i++ ) {
			for( var dataIndex = 0; dataIndex < numberOfDataSeries; dataIndex ++ ) {
				output[dataIndex].push( [ data.timestamps[i], data.data[ me.series[dataIndex].key ][i] ] );
			}
		}
		
		return output;
	};
	
	// Listen for data updates
	morpheus.event.bind("morpheus.server.monitor.update", function(ev) {
		if( me.drawing && ev.data.server === me.server ) {
			me.draw( ev.data.allData );
		}
		
	});
	
	return me.public;
};