morpheus.provide("morpheus.components.server.monitor");

morpheus.components.server.monitor = (function($, undefined) {
    
    var me = {};
    
    me.basePage = $("<div></div>");
    me.ui = {};
    
    me.uiLoaded  = false;
    me.server = null;
    
    me.jmxData = null;
    
    me.visible = false;
    
    //
    // PUBLIC
    //
    
    me.public = {
            getPage :  function() {
                return me.basePage;
            },
            pageChanged : function(ev) {
                
                if(ev.data === "morpheus.server.monitor") {
                    
                    me.visible = true;
                    
                    if( me.uiLoaded === false ) {
                        me.basePage.setTemplateURL("components/morpheus.server.monitor/templates/index.tp");
                    }
                    
                    // If jmx data has not been loaded for the current server
                    if( me.server !== null && me.jmxData === null ) {
                        
                        me.loadJMXDomains(me.server);
                        
                    }
                    
                } else {
                    me.visible = false;
                }
            },
            serverChanged : function(ev) {
                
                me.jmxData = null;
                me.server = ev.data.server;
                
                // If the monitor page is currently visible, load jmx stuff
                if( me.visible === true ) {
                    me.loadJMXDomains(me.server);
                }
                
            }
    };
    
    // 
    // PRIVATE
    //
    
    me.loadJMXDomains = function(server) {
        if(me.server !== null ) {
            me.server.admin.get("jmx", function(data) {
                
                // Make sure server hasn't changed
                if( me.server === server ) {
                    me.jmxData = [];
                    
                    for( var index in data ) {
                        // Push all jmx domains to the jmx list
                        me.jmxData.push( { name: data[index], loaded:false });
                        
                        // Fetch JMX data for this domain
                        setTimeout( (function(server, domain) {
                            return function() {
                                me.loadJMXData(server, domain);
                            };
                        })(server, data[index]), 0);
                    }
                }
                
            }, function() {
                
                // Make sure server hasn't changed
                if( me.server === server ) {
                    me.jmxData = {};
                    
                    // Server unreachable
                    me.basePage.processTemplate({
                        jmx : {},
                        server : false
                    });
                }
            });   
        }
    };
    
    me.loadJMXData = function(server, domain) {
        
        if(me.server !== null ) {
            me.server.admin.get("jmx/" + domain, function(data) {
                
                // Make sure server hasn't changed
                if( me.server === server ) {
                
                    // Check if everything is loaded
                    var domain = me.getDomain(data.domain);
                    domain.loaded = true;
                    domain.beans = data.beans;
                    
                    for(var index in me.jmxData) {
                        if( me.jmxData[index].loaded === false ) {
                            return;
                        }
                    }
                    
                    me.basePage.processTemplate({
                        jmx : me.jmxData,
                        server : me.server
                    });
                    
                }
                
            }, function() {
                
                // Make sure server hasn't changed
                if( me.server === server ) {
                    
                }
            });   
        }
        
    };
    
    me.getDomain = function(domain) {
        for( var index in me.jmxData ) {
            if(me.jmxData[index].name === domain) {
                return me.jmxData[index];
            }
        }
        
        return null;
    };
    
    return me.public;
    
})(jQuery);

//
// REGISTER STUFF
//

morpheus.ui.addPage("morpheus.server.monitor",morpheus.components.server.monitor);
morpheus.ui.mainmenu.add("Monitor","morpheus.server.monitor", null, "server");

morpheus.event.bind("morpheus.ui.page.changed", morpheus.components.server.monitor.pageChanged);
morpheus.event.bind("morpheus.server.changed",  morpheus.components.server.monitor.serverChanged);