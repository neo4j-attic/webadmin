morpheus.provide( "morpheus.neo4j" );
morpheus.provide( "morpheus.neo4jHandler" );

/**
 * Contains resources required to connect to a running neo4j instance.
 */
morpheus.neo4j = function( data )
{

    var me = {};
    
    //
    // PUBLIC
    //

    me.public = {
            toJSON : function() {
                return {urls:me.urls, domain:me.domain};
            },
            
            urls : {
                admin : data.urls.admin || "",
                rest  : data.urls.rest  || "",
                
                /**
				 * Takes a url. If the host in the url matches the REST base
				 * url, the rest base url will be stripped off. If it matches
				 * the admin base url, that will be stripped off.
				 * 
				 * If none of them match, the host will be stripped off.
				 */
                stripBase : function( url ) {
            		if (typeof(url) === "undefined") {
            			return url;
            		}
            		
            		if ( url.indexOf(me.public.urls.rest) === 0 ) {
            			return url.substring(me.public.urls.rest.length);
            		} else if ( url.indexOf(me.public.urls.admin) === 0 ) {
            			return url.substring(me.public.urls.admin.length);
            		} else {
            			return url.substring("/", 8);
            		}
            	}
            },
            
            domain : data.domain || "unknown",
            
            admin : {
                get  : function(resource, data, success, failure) { return morpheus.get(  me.public.urls.admin + resource, data, success, failure ); },
                post : function(resource, data, success, failure) { return morpheus.post( me.public.urls.admin + resource, data, success, failure ); },
                put  : function(resource, data, success, failure) { return morpheus.put(  me.public.urls.admin + resource, data, success, failure ); },
                del  : function(resource, data, success, failure) { return morpheus.del(  me.public.urls.admin + resource, data, success, failure ); }
            },
            
            rest : {
                get  : function(resource, data, success, failure) { return morpheus.get(  me.public.urls.rest + resource, data, success, failure ); },
                post : function(resource, data, success, failure) { return morpheus.post( me.public.urls.rest + resource, data, success, failure ); },
                put  : function(resource, data, success, failure) { return morpheus.put(  me.public.urls.rest + resource, data, success, failure ); },
                del  : function(resource, data, success, failure) { return morpheus.del(  me.public.urls.rest + resource, data, success, failure ); }
            },
            
            /**
			 * Get a list of jmx domains, or specific beans by their names.
			 * 
			 * The name parameter is a string with a colon, ":", separating bean
			 * domain and bean name. You can use the same wildcard patterns that
			 * you can when working with jmx in java. For instance, somedomain:*
			 * will fetch all beans in the somedomain domain.
			 * 
			 * There is a special domain for fetching kernel beans for the
			 * current local kernel. This is put in place because the name of
			 * the neo4j beans changes each time the kernel is restarted, and
			 * since there can be several kernels running in the backend.
			 * 
			 * jmx("localkernel:*") will fetch all beans for the local neo4j
			 * kernel. To fetch specific beans for the local kernel, you only
			 * specify the name attribute.
			 * 
			 * Example:
			 * 
			 * jmx("localkernel:Configuration", myCallback)
			 * jmx("localkernel:Cache", myCallback)
			 * 
			 * @param name
			 *            is the jmx bean to load. If this is omitted, a list of
			 *            available domains will be passed to the callback.
			 * 
			 * @param cb
			 *            is a callback that will be called with the result.
			 * 
			 */
            jmx : function(name, cb) {
            	
            	var noNameSpecified = (typeof(cb) === "undefined");
            	
            	var beanName = me.public.parseBeanName(name);
            	var cb = cb || name || function() {};
            	
            	
            	if( noNameSpecified ) { 
            		// List all available domains
            		
            		if( me.domains ) {
            			cb(me.domains);
            		} else {
		            	me.public.admin.get("jmx", (function(cb) {
		                    return function(data) {
		                    	me.domains = data;
		                        cb(data);
		                    };
		                })(cb));
            		}
            		
            	} else {
            		// Load specific bean(s)
            		if( beanName.domain === "localkernel") {
            			
            			// Find out what instance name the local kernel has
            			me.public.kernelInstanceName((function(cb, name) {
            				return function(instanceName) {
            					var query = escape(instanceName + ",name=" + name);
            					me.public.admin.get("jmx/org.neo4j/" + query, (function(cb) {
        		                    return function(data) {
        		                        cb(data);
        		                    };
        		                })(cb));
            				};
            			})(cb,beanName.name));
            			
            		} else {
		            	me.public.admin.get("jmx/" + beanName.domain + "/" + beanName.name, (function(cb) {
		                    return function(data) {
		                        cb(data);
		                    };
		                })(cb));
            		}
            	}
            	
            },
            
            /**
			 * Get the current jmx instance name for the local neo4j instance.
			 * 
			 * @cb is a callback that will be called with the name
			 */
            kernelInstanceName : function(cb) {
            	me.public.admin.get("jmx/kernelquery", (function(cb) { 
            		return function(data) {
            			// Data looks like : org.neo4j:instance=kernel#0,name=*
            			// Split it to be: instance=kernel#0
            			cb(data.split(":")[1].split(",")[0]);
            		};
            	})(cb));
            },
            
            /**
			 * Extract data from a bean name
			 */
            parseBeanName : function(beanName) {
                
            	if( typeof(beanName) === "string") {
	                var parts = beanName.split(":");
	                
	                return {domain:parts[0], name: parts.length > 1 ? parts[1] : "*"};
            	} else {
            		return { domain: "*", name: "*"};
            	}
                
            }
    };
    
    //
    // PRIVATE
    //
    
    
    
    //
    // CONSTRUCT
    //

    return me.public;

};

morpheus.neo4jHandler = (function(undefined) {
    
    var me = {};
    
    //
    // PRIVATE
    //
    
    me.DEFAULT_ADMIN_URL = "/admin/server/";
    me.DEFAULT_REST_URL = "http://" + document.domain + ":9999/";
    
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
                    var serv = morpheus.neo4j( { urls: {admin : me.DEFAULT_ADMIN_URL, rest : me.DEFAULT_REST_URL }, domain:document.domain } )
                
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
                
                // If no REST-url is specified, use default
                if( ! servers[i].urls.rest ) {
                	servers[i].urls.rest = me.DEFAULT_REST_URL;
                }

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
