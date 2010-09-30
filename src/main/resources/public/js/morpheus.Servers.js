/**
 * Keeps track of connected servers. Automatically loads saved servers from the
 * admin key/value store. If no servers are specified, this module will attempt
 * to connect to the current domain at default ports.
 */
morpheus.Servers = (function(undefined) {
    
    //
    // PRIVATE
    //

    var DEFAULT_DATA_URL = "http://" + document.domain + ":9999/";
    var DEFAULT_MANAGEMENT_URL = "/manage/";
    
    var servers = {};
    var currentServerKey = null;
    
    var isLoaded = false;
    
    var triedLocal = false;
    
    
    function onHashChange() {
        var newServerKey = $.bbq.getState( "s" );
        api.setCurrentServer(newServerKey);
    }
    
    function triggerLoadedEvent() {
        isLoaded = true;
        neo4j.events.trigger("morpheus.servers.loaded", servers );
        if(currentServerKey) {
            neo4j.events.trigger("morpheus.servers.current.changed", { current : servers[currentServerKey] } );
        }
    }
    
    //
    // CONSTRUCT
    //
    
    $( window ).bind( "hashchange", onHashChange );
    
    // Fetch available neo4j servers
    morpheus.prop("neo4j-servers", function(key, savedServers){
        
        if( triedLocal === false && (savedServers === null ||savedServers === undefined)) {

            // There are no servers defined.
            // Check if there is a local server running
            
            triedLocal = true;
            
            neo4j.Web.get( DEFAULT_MANAGEMENT_URL,
                function() {
                    // There is a local server running, start chatting
                    var localServer = new neo4j.GraphDatabase(DEFAULT_DATA_URL, DEFAULT_MANAGEMENT_URL);
                    
                    servers = {};
                    servers[document.domain] = localServer;
                    triggerLoadedEvent();
                    
                    // Save this 'til next time..
                    morpheus.prop("neo4j-servers",servers);
                },
                function() {
                    // No local server running :(
                    morpheus.prop("neo4j-servers",{});
                    
                    servers = {};
                }
            );
        } else {
            var isLegacyFormat = false;
            var item;
            for(var key in savedServers) {
                item = savedServers[key];
                if(item.name) {
                    // Legacy support
                    isLegacyFormat = true;
                    
                    if( item.adminUrl === "/admin/server/" ) {
                        item.adminUrl = "/manage/";
                    }
                    
                    servers[item.name] = new neo4j.GraphDatabase(item.restUrl, item.adminUrl);
                } else {
                    servers[key] = new neo4j.GraphDatabase(item.url, item.manageUrl);
                }
            }
            triggerLoadedEvent();
        }
    });
    
    //
    // PUBLIC API
    //

    var api = {
            
        isLoaded : function() {
            return isLoaded;
        },
        
        getServers : function() { 
            return servers; 
        },
        
        getServer : function(key) {
            if(servers[key]) {
                return servers[key];
            } else {
                return null;
            }
        },
        
        getServerKey : function(server) {
            for(var key in servers) {
                if(servers[key].url === server.url) {
                    return key;
                }
            }
            
            return null;
        },
        
        getCurrentServer : function() {
            return morpheus.Servers.getServer(currentServerKey);
        },
        
        setCurrentServer : function(keyOrServer) {
            
            if(typeof(keyOrServer) === "object" ) {
                var key = morpheus.Servers.getServerKey(keyOrServer);
            } else {
                var key = keyOrServer;
            }
            
            if( key !== currentServerKey ) {
                currentServerKey = key;
                if( servers[key] ) {
                    neo4j.events.trigger("morpheus.servers.current.changed", { current : servers[currentServerKey] } );
                }
            }
        },
        
        setServer : function(key, dataUrl, manageUrl) {
            servers[key] = new neo4j.GraphDatabase(dataUrl, manageUrl);
            neo4j.events.trigger("morpheus.servers.changed", { servers : servers } );
            
            morpheus.prop("neo4j-servers",servers);
        }
    };

    onHashChange();

    return api;
    
})();