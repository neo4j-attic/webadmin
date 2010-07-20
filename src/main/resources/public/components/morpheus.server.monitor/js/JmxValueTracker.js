morpheus.provide("morpheus.components.server.monitor.JmxValueTracker);

/**
 * Tracks a given jmx value of a given neo4j server.
 * 
 * @param server is the server instance to track
 * @param beanName is the bean to track
 * @param extractor is a function that will extract the value we want from the bean object
 * @param cb is a callback that is triggered with the new value any time the value beeing tracked changes.
 * @param interval (optional) is the update interval in milliseconds. The default is 10000.
 */
morpheus.components.server.monitor.JmxValueTracker = function(server, beanName, extractor, cb, interval) {
	
	var me = {};
	
	me.polling_interval = interval || 10000;
	me.max_polling_interval = me.polling_interval * 10;

	/**
	 * This is the actual current polling interval beeing used.
	 * If the server data has not changed, the polling time will be
	 * extended until it reaches max_polling interval.
	 */
	me.actual_polling_interval = me.polling_interval;
	
	me.callback = cb;
	me.extractor = extractor;
	me.beanName = beanName;
	me.server = server;
	
	me.prevValue = null;
	
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
		
//		me.server.admin.get("backup/status", function(data) {
//			
//			// If any data has changed
//			if( data.current_action !== me.prevAction ||
//				data.started !== me.prevStarted ||
//				data.eta !== me.prevEta ) {
//				
//				me.prevAction = data.current_action;
//				me.prevStarted = data.started;
//				me.prevEta = data.eta;
//				
//				// Call callback, if it returns false, stop polling.
//				if( ! me.callback(data) ) {
//					return;
//				}
//				
//				// Reset actual polling to the polling interval the user wanted.
//				me.actual_polling_interval = me.polling_interval;
//				
//			} else {
//				if( me.actual_polling_interval >= me.max_polling_interval ) {
//					me.actual_polling_interval = me.max_polling_interval;
//				} else {
//					me.actual_polling_interval *= 2;
//				}
//			}
//			
//			setTimeout(me.poll, me.actual_polling_interval);
//			
//		});
		
	};
	
	//
	// CONSTRUCT
	// 
	
	return me.public;
};