morpheus.provide("morpheus.components.monitor.base");

/**
 * JMX exploration page module for the monitor component.
 * 
 * TODO: This needs to start using the jmx interface provided by neo4j instances instead of implementing it's own.
 */
morpheus.components.monitor.jmx = (function($, undefined) {
    
    var me = {};
    
    me.basePage = $("<div></div>");
    me.ui = {};
    
    me.uiLoaded  = false;
    me.server = null;
    
    me.jmxData = null;
    me.currentBean = null;
    
    me.visible = false;
    
    //
    // PUBLIC
    //
    
    me.public = {
            
            getPage :  function() {
                return me.basePage;
            },
            
            /**
             * Get bean data, given a bean name. This takes a callback since it
             * is not for sure that data has been loaded yet.
             * @param name is the bean name to find
             * @param cb is a callback that will be called with an array of matching beans 
             *        (usually just one, but could be several if you use wildcard markers in the name)
             */
            findBeans : function(name, cb) {

                var beanInfo = me.public.parseBeanName(name);
                var domain = me.getDomain(beanInfo.domain);
                
                if( me.jmxData !== null && domain !== null ) {
                    
                    // It seems we have the data..
                    for( var index in domain.beans ) {
                        if( domain.beans[index].name === name ) {
                            cb([domain.beans[index]]);
                            return;
                        }
                        
                    }
                }
                
                // The requested bean is not available locally, check if there is a server connection
                
                if( me.server === null ) { // Nope
                    cb([]);
                    return;
                }
                    
                
                // Server available
                me.server.admin.get("jmx/" + beanInfo.domain + "/" + beanInfo.name, (function(cb) {
                    return function(data) {
                        cb(data);
                    };
                })(cb));
                
            },
            
            /**
             * Extract data from a bean name
             */
            parseBeanName : function(beanName) {
                
                var parts = beanName.split(":");
                
                return {domain:parts[0], name:parts[1]};
                
            },
            
            pageChanged : function(ev) {
                
                if(ev.data === "morpheus.monitor.jmx") {
                    
                    me.visible = true;
                    
                    if( me.uiLoaded === false ) {
                        me.basePage.setTemplateURL("components/morpheus.monitor/templates/jmx.tp");
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
        
        if( typeof(beanName) !== "undefined" && (me.currentBean === null || beanName !== me.currentBean.name)) {
            me.public.findBeans(beanName, function(beans) { 
                if(beans.length > 0) {
                    me.currentBean = beans[0];
                    
                    if( me.visible ) {
                    	me.render();
                    }
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
                    jmxbean : $('.bean-name',ev.target.parentNode).val()
                });
            };
        })(ev),0);
    
        ev.preventDefault();
    });
    
    return me.public;
    
})(jQuery);

//
// REGISTER STUFF
//

morpheus.ui.addPage("morpheus.monitor.jmx",morpheus.components.monitor.jmx);
morpheus.ui.mainmenu.add("JMX","morpheus.monitor.jmx", null, "server",7);

morpheus.event.bind("morpheus.init", morpheus.components.monitor.jmx.init);
morpheus.event.bind("morpheus.ui.page.changed", morpheus.components.monitor.jmx.pageChanged);
morpheus.event.bind("morpheus.changed",  morpheus.components.monitor.jmx.serverChanged);