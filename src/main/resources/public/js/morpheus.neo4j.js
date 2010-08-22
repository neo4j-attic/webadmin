morpheus.provide( "morpheus.neo4j" );
morpheus.provide( "morpheus.neo4jHandler" );

/**
 * Contains resources required to connect to a running neo4j instance.
 */
morpheus.neo4j = function( data )
{

    var me = {};
    
    me.monitoring = false;
    me.monitorInterval = 3000;
    me.latestDataPointTime = (new Date()).getTime() - 1000 * 60 * 60;
    

    me.domain = me.name = data.domain || data.name || "unknown";
    
    me.password = data.username || "";
    me.username = data.password || "";
    
    me.kernelQueryQueue = [];
    me.kernelQueryRunning = false;
    
    me.monitorData = {
    	timestamps : [],
    	data : {},
    	end_time : me.latestDataPointTime + 1,
		start_time : me.latestDataPointTime
    };
    
    //
    // PUBLIC
    //

    me.public = {
    		
            toJSON : function() {
                return {adminUrl:me.public.urls.admin, restUrl:me.public.urls.rest, domain:me.domain, name:me.name, password:me.password,username:me.username};
            },
            
            urls : {
                admin : data.adminUrl || "",
                rest  : data.restUrl  || "",
                
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
            
            admin : {
                get  : function(resource, data, success, failure) { return me.public.admin.ajax(resource,data,success,failure,'get'); },
                post : function(resource, data, success, failure) { return me.public.admin.ajax(resource,data,success,failure,'post');},
                put  : function(resource, data, success, failure) { return me.public.admin.ajax(resource,data,success,failure,'put'); },
                del  : function(resource, data, success, failure) { return me.public.admin.ajax(resource,data,success,failure,'del'); },
                
                ajax : function( resource, data, success, failure, method ) {
                	
                	if(typeof(data) === "function") {
                        failure = success;
                        success = data;
                        data = null;
                    }
                	
                	return morpheus[method]( me.public.urls.admin + resource, data, success, me.wrapFailureCallback(failure), {
                		username : me.username,
                		password : me.password
                	});
                }
            },
            
            rest : {
            	get  : function(resource, data, success, failure) { return me.public.rest.ajax(resource,data,success,failure,'get'); },
                post : function(resource, data, success, failure) { return me.public.rest.ajax(resource,data,success,failure,'post');},
                put  : function(resource, data, success, failure) { return me.public.rest.ajax(resource,data,success,failure,'put'); },
                del  : function(resource, data, success, failure) { return me.public.rest.ajax(resource,data,success,failure,'del'); },
                
                ajax : function( resource, data, success, failure, method ) {
                	
                	if(typeof(data) === "function") {
                        failure = success;
                        success = data;
                        data = null;
                    }
                	
                	return morpheus[method]( me.public.urls.rest + resource, data, success, me.wrapFailureCallback(failure), {
                		username : me.username,
                		password : me.password
                	});
                }
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
            	
            	me.kernelQueryQueue.push(cb);
            	
            	if( ! me.kernelQueryRunning ) {
            		me.kernelQueryRunning = true;
	            	me.public.admin.get("jmx/kernelquery",  
	            		function(data) {
	            			// Data looks like : org.neo4j:instance=kernel#0,name=*
	            			// Split it to be: instance=kernel#0
	            			var result = data.split(":")[1].split(",")[0];
	            			var callbacks = me.kernelQueryQueue;
	            			
	            			me.kernelQueryQueue = [];
	            			me.kernelQueryRunning = false;
	            			
	            			for( var i = 0, l = callbacks.length; i < l; i++ ) {
	            				callbacks[i](result);
	            			}
	            			
	            		}
	            	);
            	}
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
                
            },
            
            /**
			 * Start monitoring this server, polling it at regular intervals for
			 * data about its health. Call this method, and then listen for
			 * "morpheus.server.monitor.update" events.
			 */
            startMonitoring : function() {
            	if( me.monitoring === false ) {
            		me.monitoring = true;
            		me.pollMonitor();
            	}
            },
            
            stopMonitoring : function() {
            	me.monitoring = false;
            },
    		
    		domain : me.domain,
    		
    		getDomain : function() {
    			return me.domain;
    		},
    		
    		getName : function() {
    			return me.name;
    		},
            
            getMonitorData : function() {
            	return me.monitorData;
            },
            
            setName : function(name) {
            	me.name = name;
            },
            
            setUsername : function(username) {
            	me.username = username;
            },
            
            setPassword : function(password) {
            	me.password = password;
            },
            
            /**
             * Persist any changes made to this server to the backend.
             */
            save : function() {
            	morpheus.neo4jHandler.save();
            }
    };
    
    //
    // PRIVATE
    //
    
    /**
	 * This will poll the monitoring service for this neo4j server, fetching any
	 * new data that is available on the current health of the server. It will
	 * also trigger itself to be called again at a regular interval.
	 * 
	 * To stop this, call me.public.stopMonitoring()
	 */
    me.pollMonitor = function() {
    	
    	if( me.monitoring ) {
    		
    		me.public.admin.get("monitor/" + me.latestDataPointTime, function(data) {
				
				// Find a data point list to check
				var key;
				for (key in data.data ) { break; }
				
				// If there is any data sources
    			if( key ) {
    				// Find the last timestamp that has any data associated with
					// it
    				var lastIndexWithData, firstIndexWithData;
	    			for( lastIndexWithData = data.timestamps.length - 1; lastIndexWithData >= 0; lastIndexWithData-- ) {
	    				if( typeof(data.data[key][lastIndexWithData]) === "number" ) {
	    					me.latestDataPointTime = data.timestamps[lastIndexWithData];
	    					break;
	    				}
	    			}
	    			
	    			// Find the first timestamp that has any data associated with
					// it
	    			for( firstIndexWithData = 0; firstIndexWithData <= lastIndexWithData; firstIndexWithData++ ) {
	    				if( typeof(data.data[key][firstIndexWithData]) === "number" ) {
	    					break;
	    				}
	    			}
	    			
	    			// If there is new data
	    			if( lastIndexWithData >= 0 ) {
	    				
	    				// Add timestamps
	    				var newTimestamps = data.timestamps.splice(firstIndexWithData, lastIndexWithData - firstIndexWithData);
	    				me.monitorData.timestamps = me.monitorData.timestamps.concat( newTimestamps );
	    				
	    				// Add data
	    				var newData = {};
	    				for( var key in data.data ) {
	    					newData[key] = data.data[key].splice(firstIndexWithData, lastIndexWithData - firstIndexWithData);
	    					
	    					if ( typeof(me.monitorData.data[key]) === "undefined" ) {
	    						me.monitorData.data[key] = [];
	    					}
	    					
	    					me.monitorData.data[key] = me.monitorData.data[key].concat( newData[key] );
	    				}

	    				me.monitorData.end_time = me.latestDataPointTime;
	    				
	    				// Let the world know
	    				morpheus.event.trigger("morpheus.server.monitor.update", {
	    					server : me.public,
	    					newData : {
	    						data: newData,
	    						timestamps : newTimestamps,
	    						end_time : me.latestDataPointTime,
	    						start_time : data.start_time
	    					},
	    					
	    					allData : me.monitorData
	    				});
	    			}
	    			
    			} else {
    				// There is no data provided by the server
    				me.latestDataPointTime = (new Date()).getTime();
    			}
    			
    			// Trigger a new poll
    			setTimeout( me.pollMonitor, me.monitorInterval);
    			
    		});
    	}
    };
    
    /**
     * Wrap an AJAX failure callback in some common boilerplate that handles
     * unresponsive servers etc.
     */
    me.wrapFailureCallback = function(cb) {
    	
    	return function(req) {
    		
    		if( typeof(cb) === "function" ) {
    			cb(req);
    		} else {
    			morpheus.showError("Unable to reach server, please ensure your internet connection is working.")
    		}
    	};
    	
    };
    
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
    	
        if( servers === null || servers === undefined || (servers.length === 0 && me.triedLocal === false)) {

            // There are no servers defined.
            // Check if there is a local server running
            
            me.triedLocal = true;
            
            // TODO: Switch to morpheus.ajax
            
            $.ajax({
                url : me.DEFAULT_ADMIN_URL + "status",
                success : function() {
                    // There is a local server running, start chatting
                    var serv = morpheus.neo4j( { adminUrl : me.DEFAULT_ADMIN_URL, restUrl : me.DEFAULT_REST_URL, name:document.domain } )
                
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
                if( servers[i].restUrl == undefined || servers[i].restUrl.length === 0 ) {
                	servers[i].restUrl = me.DEFAULT_REST_URL;
                }
                
                if( servers[i].adminUrl == undefined || servers[i].adminUrl.length === 0 ) {
                	servers[i].adminUrl = me.DEFAULT_ADMIN_URL;
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
            if( me.servers[key].getName() === serverName ) {
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
            servers : function() { return me.servers; },
            
            currentServer : function(server) {
            	if( server !== undefined) {
            		$.bbq.pushState( {s:server} );
            	} else {
            		return me.currentServer;
            	}
            },
            
            addServer : function(name, url) {
            	me.servers.push(morpheus.neo4j( { adminUrl : url, restUrl : url, name:name }));
            	morpheus.event.trigger("morpheus.servers.changed", { servers : me.servers } );
            },
            
            /**
             * Save the current server state to the backend.
             */
            save : function() {
                morpheus.prop("neo4j-servers",me.servers);
            }
    };

    return me.api;

    
})();

//
// HOOKS
//

morpheus.event.bind("morpheus.init", morpheus.neo4jHandler.init );
