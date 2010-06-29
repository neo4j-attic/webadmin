morpheus.provide("morpheus.components.Lifecycle");

/**
 * A widget for starting, stopping and restarting the neo4j backend.
 */
morpheus.components.Lifecycle = function() {
	
	var me = {};
	
	// 
	// PRIVATE
	//
	
	me.element = $("<div></div>");
	
	me.status = "UNKOWN";
	me.watingForResponse = false;
	
	me.getWidget = function() {
		return me.element;
	};
	
	me.start = function(ev) {
		if(me.status !== "RUNNING") {
			me.serverAction("/api/server/start", "Starting server..");
		}
		ev.preventDefault();
	};
	
	me.stop = function(ev) {
		if(me.status !== "STOPPED") {
			me.serverAction("/api/server/stop", "Stopping server..");
		}
		ev.preventDefault();
	};
	
	me.restart = function(ev) {
		me.serverAction("/api/server/restart", "Restarting server..");
		ev.preventDefault();
	};
	
	me.serverAction = function(url, message) {
		if( ! me.watingForResponse ) {
			me.statusElement.html(message);
			me.watingForResponse = true;
			
			// Allow UI update
			setTimeout(function() {
				$.post(url, function(data) {
					me.watingForResponse = false;
					me.setStatus(data.current_status);
				}, "json");
			},0);
		}
	};
	
	me.setStatus = function(status) {
		me.status = status;
		if(me.statusActions[status]) {
			me.statusActions[status]();
		}
	};
	
	me.statusActions = {
		RUNNING : function() {
			me.statusElement.html("Running");
		},
		
		STOPPED : function() {
			me.statusElement.html("Stopped");
		}
	};
	
	//
	// CONSTRUCT
	//
	
	me.element.setTemplateURL("components/morpheus.lifecycle/templates/widget.tp");
	me.element.processTemplate();
	
	// Cache element lookups
	me.buttons = {};
	me.buttons.start   = $(".mor_lifecycle_start",   me.element);
	me.buttons.restart = $(".mor_lifecycle_restart", me.element);
	me.buttons.stop    = $(".mor_lifecycle_stop",    me.element);
	
	me.statusElement = $(".mor_lifecycle_status", me.element);
	
	// Event listeners
	me.buttons.start   .click(me.start);
	me.buttons.restart .click(me.restart);
	me.buttons.stop    .click(me.stop);
	
	// Check server status
	me.statusElement.html("Checking server status..");
	setTimeout(function() {
		$.get("/api/server/status", function(data) {
			me.setStatus(data.current_status);
		}, "json");
	},0);
	
	//
	// PUBLIC INTERFACE
	//
	
	me.api = {
			init : me.init,
			getWidget : me.getWidget
	};
	
	return me.api;
	
};
