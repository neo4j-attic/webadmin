morpheus.provide("morpheus.components.server.monitor.MonitorChart");

$.require("js/vend/jquery.flot.js");

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
		xaxis : {
			mode: "time",
		    timeformat: "%H:%M:%S"
		},
		yaxis : {},
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
	me.controlsClass = me.containerId + "_controls";
	me.zoom = {
		week : { 
			id : me.containerId + "_zoom_1",
			xSpan : 1000 * 60 * 60 * 24 * 7
		},
		day : { 
			id : me.containerId + "_zoom_2",
			xSpan : 1000 * 60 * 60 * 24
		},
		six_hours : { 
			id : me.containerId + "_zoom_3",
			xSpan : 1000 * 60 * 60 * 6
		},
		thirty_minutes : { 
			id : me.containerId + "_zoom_4",
			xSpan : 1000 * 60 * 30
		}
	};
	
	var controls = "<ul class='mor_module_actions'><li><a class='"+me.controlsClass+"' href='#' id='"+me.zoom.week.id +"'>Week</a></li><li><a class='"+me.controlsClass+" current' href='#' id='"+me.zoom.day.id +"'>Day</a></li><li><a class='"+me.controlsClass+"' href='#' id='"+me.zoom.six_hours.id +"'>6 hours</a><li><a class='"+me.controlsClass+"' href='#' id='"+me.zoom.thirty_minutes.id +"'>30 minutes</a></li></li></ul><div class='break'></div>"
	me.container = $("<div class='mor_module mor_span-5'><h2>" + me.settings.label + "</h2>" + controls + "<div class='mor_chart_container'><div style='height:"+me.settings.height+"px;' id='" + me.containerId + "'></div></div></div>")
	
	me.drawing = false;
	me.currentZoom = "day";
	me.currentData = [];
	
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
		
		// Set zoom and tick label formatting
		if( data.timestamps.length > 0 ) {
			me.settings.xaxis.min = data.timestamps[ data.timestamps.length - 1 ] - me.zoom[me.currentZoom].xSpan;
		}
		
		$.plot($("#" + me.containerId), me.parseData(data), {
			xaxis : me.settings.xaxis,
			yaxis : me.settings.yaxis
		});
	};
	
	me.parseData = function(data) {
		var output = [];
		var numberOfDataSeries = me.series.length;
		
		// Initialize data arrays for all data series
		for( var dataIndex = 0; dataIndex < numberOfDataSeries; dataIndex ++ ) {
			output[dataIndex] = {
				data : []
			};
			
			$.extend( true, output[dataIndex], me.settings.data[me.series[dataIndex].key] );
		}
		
		// Format data for jqChart
		for( var i = 0, l = data.timestamps.length; i < l; i++ ) {
			for( var dataIndex = 0; dataIndex < numberOfDataSeries; dataIndex ++ ) {
				output[dataIndex].data.push( [ data.timestamps[i], data.data[ me.series[dataIndex].key ][i] ] );
			}
		}
		
		return output;
	};
	
	// 
	// CONSTRUCT
	//
	
	for( var key in me.zoom ) {
		$("#" + me.zoom[key].id).live('click', (function(zoomKey) {
			return function(ev) {
				ev.preventDefault();
				me.currentZoom = zoomKey;
				
				$("." +me.controlsClass).removeClass("current");
				$("#" + me.zoom[zoomKey].id).addClass("current");
				
				me.draw( me.server.getMonitorData() );
			};
		})(key));
	}
	
	// Listen for data updates
	morpheus.event.bind("morpheus.server.monitor.update", function(ev) {
		if( me.drawing && ev.data.server === me.server ) {
			me.draw( ev.data.allData );
		}
		
	});
	
	return me.public;
};