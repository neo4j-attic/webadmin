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
            domain : me.domain
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
    
    me.init = function() {
        
        // Fetch available neo4j servers
        morpheus.prop("neo4j-servers", me.serversLoaded );
        
    };
    
    me.serversLoaded = function(key, servers) {
        
        if( servers === undefined ) {
            
            // There are no servers defined.
            // Check if there is a local server running
            
            $.ajax({
                url : me.DEFAULT_ADMIN_URL + "status",
                success : function() {
                    // There is a local server running, start chatting
                    var serv = morpheus.neo4j( { urls: {admin : me.DEFAULT_ADMIN_URL }, domain:document.domain } )
                
                    me.servers = [serv];
                    morpheus.event.trigger("morpheus.servers.loaded", { servers : me.servers } );
                    
                    // Save this 'til next time..
                    morpheus.prop("neo4j-servers",me.servers);
                },
                failure : function() {
                    // No local server running :(
                    morpheus.prop("neo4j-servers",[]);
                    morpheus.event.trigger("morpheus.servers.loaded", { servers : me.servers } );
                }
            })
        } else {
         
            // Load available servers
            me.servers = [];
            for(var i = 0, l = servers.length; i < l ; i++) {
                me.servers.push( morpheus.neo4j(servers[i]) );
            }
            
            morpheus.event.trigger("morpheus.servers.loaded", { servers : me.servers } );
            
        }
        
    };
    
    //
    // PUBLIC API
    //

    me.api = {
            init: me.init,
            
            loaded  : function() { return (me.servers === false); },
            servers : function() { return me.servers; }
    };

    return me.api;

    
})();

//
// HOOKS
//

morpheus.event.bind( "morpheus.init", function(ev) {
    morpheus.neo4jHandler.init();
});