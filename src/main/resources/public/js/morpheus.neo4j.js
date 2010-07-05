morpheus.provide( "morpheus.neo4j" );
morpheus.provide( "morpheus.neo4jHandler" );

/**
 * Contains resources required to connect to a running neo4j instance.
 */
morpheus.neo4j = function( data )
{

    var me = {};

    //
    // PRIVATE
    //
 
    me.domain = data.domain || "unknown";
    
    me.urls = {
        admin : data.urls.admin || "",
        rest  : data.urls.rest  || "",
        jmx   : data.urls.jmx   || ""
    };
    
    // Remote API
    
    me.rest = {};
    me.admin = {
            get  : function(resource, data, success, failure) { return morpheus.get(  me.urls.admin + resource, data, success, failure ); },
            post : function(resource, data, success, failure) { return morpheus.post( me.urls.admin + resource, data, success, failure ); },
            put  : function(resource, data, success, failure) { return morpheus.put(  me.urls.admin + resource, data, success, failure ); },
            del  : function(resource, data, success, failure) { return morpheus.del(  me.urls.admin + resource, data, success, failure ); }
    };
    me.jmx = {};
    
    //
    // CONSTRUCT
    //
    
    
    
    //
    // PUBLIC API
    //

    me.api = {
            toJSON : function() {
                return {urls:me.urls, domain:me.domain};
            },
            
            urls : me.urls,
            domain : me.domain,
            admin : me.admin,
            rest : me.rest,
            jmx : me.jmx
    };

    return me.api;

};

morpheus.neo4jHandler = (function(undefined) {
    
    var me = {};
    
    //
    // PRIVATE
    //
    
    me.DEFAULT_ADMIN_URL = "/admin/server/";
    
    me.servers = false;
    me.currentServer = null;
    me.morpheusInitiated = false;
    
    me.triedLocal = false;
    
    /**
     * Called when servers are loaded and morpheus is initiated.
     */
    me.init = function() {

        $( window ).bind( "hashchange", me.hashchange );
        me.hashchange();
        morpheus.event.trigger("morpheus.servers.loaded", { servers : me.servers } );
        
    };
    
    /**
     * Called when morpheus is initiated
     */
    me.morpheusInit = function () {
        me.morpheusInitiated = true;
        if(me.servers !== false ) {
            me.init();
        }
    };
    
    me.serversLoaded = function(key, servers) {
        
        if( servers === undefined || (servers.length === 0 && me.triedLocal === false)) {

            // There are no servers defined.
            // Check if there is a local server running
            
            me.triedLocal = true;
            
            $.ajax({
                url : me.DEFAULT_ADMIN_URL + "status",
                success : function() {
                    // There is a local server running, start chatting
                    var serv = morpheus.neo4j( { urls: {admin : me.DEFAULT_ADMIN_URL }, domain:document.domain } )
                
                    var servers = [serv];
                    
                    me.serversLoaded(null, servers);
                    
                    // Save this 'til next time..
                    morpheus.prop("neo4j-servers",servers);
                },
                failure : function() {
                    // No local server running :(
                    morpheus.prop("neo4j-servers",[]);
                    
                    me.serversLoaded(null, []);
                }
            })
        } else {
         
            // Load available servers
            me.servers = [];
            for(var i = 0, l = servers.length; i < l ; i++) {
                me.servers.push( morpheus.neo4j(servers[i]) );
            }
            
            if(me.morpheusInitiated) {
                me.init();
            }
            
        }
        
    };
    
    me.setServer = function(serverName) {
      
        if( me.currentServer !== me.getServer(serverName)) {
        
            me.currentServer = me.getServer(serverName);
            if( me.currentServer === null ) {
                
                // Hide server-related menu items
                morpheus.ui.mainmenu.hideSet("server");
                morpheus.event.trigger("morpheus.server.changed", { server:null });
                
            } else {
                
                // Show server-related menu items
                morpheus.ui.mainmenu.showSet("server");
                morpheus.event.trigger("morpheus.server.changed", { server:me.currentServer });
                
            }
        }
       
    };
    
    /**
     * Get a server given its name, return the server object or null.
     * 
     * TODO: This currently uses the server domain as a unique identifier, we
     * need to switch to something else to allow for several servers per domain.
     */
    me.getServer = function(serverName) {
        for( var key in me.servers ) {
            if( me.servers[key].domain === serverName ) {
                return me.servers[key];
            }
        }
        
        return null;
    };
    
    me.hashchange = function(ev) {
      
        me.setServer($.bbq.getState( "s" ));
        
    };
    
    //
    // CONSTRUCT
    //
    
    // Fetch available neo4j servers
    morpheus.prop("neo4j-servers", me.serversLoaded );
    
    //
    // PUBLIC API
    //

    me.api = {
            init: me.morpheusInit,
            
            loaded  : function() { return (me.servers === false); },
            servers : function() { return me.servers; }
    };

    return me.api;

    
})();

//
// HOOKS
//

morpheus.event.bind("morpheus.init", morpheus.neo4jHandler.init );
