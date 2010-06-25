morpheus.provide("morpheus.event");

/**
 * A simple event-handling system for morpheus.
 * TODO: Event handlers are not guaranteed to execute in the order they 
 * are added in, and there is no way to stop an event from triggering
 * all handlers.
 */
morpheus.event = (function () {
    
    var me = {};
    
    //
    // PRIVATE INTERFACE
    //
    
    me.uniqueNamespaceCount = 0; // Used to create unique namespaces
    me.handlers = {};
    
    // 
    // CONSTRUCT
    // 
    
    
    
    //
    // PUBLIC INTERFACE 
    //
    
    me.api = {
        
    	/**
    	 * Naive implementation to quickly get anonymous event namespaces.
    	 */
        createUniqueNamespace : function() {
            return "uniq#" + (me.uniqueNamespaceCount++);
        },
        
        addHandler : function( key, callback) {
            if ( typeof(me.handlers[key]) === "undefined" ) {
                me.handlers[key] = [];
            }
            
            me.handlers[key].push(callback);
        },
        
        trigger : function( key, data ) {
            if ( typeof(me.handlers[key]) !== "undefined" ) {
            
                data = data || {};
            
                var handlers = me.handlers[key];
                var event = { key : key, data : data };

                for(var i = 0, o = handlers.length; i < o; i++) {
                    setTimeout(function(){handlers[i](event);},0);
                }
            }
        }
        
    };
    
    return me.api;
    
})();