morpheus.provide( "morpheus.neo4j" );
morpheus.provide( "morpheus.neo4jHandler" );

/**
 * Contains resources required to connect to a running neo4j instance.
 */
morpheus.neo4j = function( adminUrl, restUrl, jmxUrl )
{

    var me = {};

    //
    // PRIVATE
    //
 
    // Allow instantiation from object
    if( typeof(adminUrl) === "object" ) {
        me.urls = {
                admin : adminUrl['admin'] || "",
                rest  : adminUrl['rest']  || "",
                jmx   : adminUrl['jmx']   || ""
          };
    } else {
        me.urls = {
                admin : adminUrl || "",
                rest  : restUrl  || "",
                jmx   : jmxUrl   || ""
          };
    }
    
    //
    // CONSTRUCT
    //
    
    
    
    //
    // PUBLIC API
    //

    me.api = {
            toJSON : function() {
                return me.urls;
            },
            
            urls : me.urls
    };

    return me.api;

};

morpheus.neo4jHandler = (function(undefined) {
    
    var me = {};
    
    //
    // PRIVATE
    //
    
    me.DEFAULT_ADMIN_URL = "/admin/server/";
    
    me.servers = [];
    
    me.init = function() {
        
        // Fetch available neo4j servers
        morpheus.prop("neo4j-servers",me.serversLoaded);
        
    };
    
    me.serversLoaded = function(key, servers) {
        
        if( servers === undefined ) {
            
            // There are no servers defined.
            // Check if there is a local server running
            
            $.ajax({
                url : me.DEFAULT_ADMIN_URL + "status",
                success : function() {
                    // There is a local server running, start chatting
                    var serv = morpheus.neo4j( me.DEFAULT_ADMIN_URL )
                
                    me.servers = [serv];
                    
                    morpheus.prop("neo4j-servers",[serv]);
                },
                failure : function() {
                    morpheus.prop("neo4j-servers",[]);
                }
            })
        } else {
         
            // Load available servers
            
            for(var i = 0, l = servers.length; i < l ; i++) {
                me.servers.push( morpheus.neo4j(servers[i]) );
            }
            
        }
        
    };
    
    //
    // PUBLIC API
    //

    me.api = {
            init: me.init
    };

    return me.api;

    
})();

//
// HOOKS
//

morpheus.event.bind( "morpheus.init", function(ev) {
    morpheus.neo4jHandler.init();
});