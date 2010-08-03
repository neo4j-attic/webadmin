morpheus.provide("morpheus.components.server.backup.StatusTracker");

/**
 * Used to keep track of the current status of a neo4j server.
 * 
 * @param server is the server instance to track
 * @param cb is a callback that is called every time state changes. 
 *        If this callback returns true, StatusTracker will keep polling.
 * @param interval (optional) is the polling interval in milliseconds. The default is 1000.
 */
morpheus.components.server.backup.StatusTracker = function(server, cb, interval) {
	
	var me = {};
	
	me.max_polling_interval = 15000;
	
	me.polling_interval = interval || 500;

	/**
	 * This is the actual current polling interval beeing used.
	 * If the server data has not changed, the polling time will be
	 * extended until it reaches max_polling interval.
	 */
	me.actual_polling_interval = me.polling_interval;
	
	me.callback = cb;
	me.server = server;
	
	me.prevAction  = null;
	me.prevStarted = null;
	me.prevEta     = null;
	
	//
	// PUBLIC
	//
	
	me.public = {
		run : function() {
			me.poll();
		}
	};
	
	//
	// INTERNALS
	//
	
	me.poll = function() {
		
		me.server.admin.get("backup/status", function(data) {
			
			// If any data has changed
			if( data.current_action !== me.prevAction ||
				data.started !== me.prevStarted ||
				data.eta !== me.prevEta ) {
				
				me.prevAction = data.current_action;
				me.prevStarted = data.started;
				me.prevEta = data.eta;
				
				// Call callback, if it returns false, stop polling.
				if( ! me.callback(data) ) {
					return;
				}
				
				// Reset actual polling to the polling interval the user wanted.
				me.actual_polling_interval = me.polling_interval;
				
			} else {
				if( me.actual_polling_interval >= me.max_polling_interval ) {
					me.actual_polling_interval = me.max_polling_interval;
				} else {
					me.actual_polling_interval *= 2;
				}
			}
			
			setTimeout(me.poll, me.actual_polling_interval);
			
		});
		
	};
	
	//
	// CONSTRUCT
	// 
	
	return me.public;
};