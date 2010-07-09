morpheus.provide("morpheus.components.server.monitor");

morpheus.components.server.gremlin = (function($, undefined) {
    
    var me = {};
    
    me.basePage = $("<div></div>");
    me.ui = {};
    
    me.uiLoaded  = false;
    me.server = null;
    
    me.visible = false;
    
    //
    // PUBLIC
    //
    
    me.public = {
            
            getPage :  function() {
                return me.basePage;
            },
            
            pageChanged : function(ev) {
                
                if(ev.data === "morpheus.server.gremlin") {
                    
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
                
            },
            
            init : function() {
                $( window ).bind( "hashchange", me.hashchange );
                me.hashchange();
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

                    me.render();
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
                    
                    // Trigger a re-load of the currently visible bean, if there is one.
                    me.hashchange();
                    me.render();
                    
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
    
    /**
     * Triggered when the URL hash state changes.
     */
    me.hashchange = function(ev) {
        var beanName = $.bbq.getState( "jmxbean" );
        
        if( typeof(beanName) !== "undefined" ) {
            
            me.public.findBeans(beanName, function(beans) { 
                
                if(beans.length > 0) {
                    me.currentBean = beans[0];
                    me.render();
                }
                
            });
            
        }
    };
    
    me.render = function() {
        
        me.basePage.processTemplate({
            jmx : (me.jmxData === null) ? [] : me.jmxData,
            server : me.server,
            bean : me.currentBean
        });
        
    };
    
    //
    // CONSTRUCT
    //
    
    $('.mor_monitor_jmxbean_button').live('click', function(ev) {
        
        setTimeout((function(ev){
            return function() {
                $.bbq.pushState({
                    jmxbean : $(ev.originalTarget).attr('data-bean')
                });
            };
        })(ev));
    
        ev.preventDefault();
    });
    
    return me.public;
    
})(jQuery);

//
// REGISTER STUFF
//

morpheus.ui.addPage("morpheus.server.monitor",morpheus.components.server.monitor);
morpheus.ui.mainmenu.add("Monitor","morpheus.server.monitor", null, "server");

morpheus.event.bind("morpheus.init", morpheus.components.server.monitor.init);
morpheus.event.bind("morpheus.ui.page.changed", morpheus.components.server.monitor.pageChanged);
morpheus.event.bind("morpheus.server.changed",  morpheus.components.server.monitor.serverChanged);