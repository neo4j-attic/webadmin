morpheus.provide("morpheus.components.server.monitor");

morpheus.components.server.gremlin = (function($, undefined) {
    
    var me = {};
    
    me.basePage = $("<div></div>");
    me.ui = {};
    
    me.uiLoaded  = false;
    me.server = null;
    
    me.visible = false;
    
    me.consoleElement = null;
    
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
                        me.basePage.setTemplateURL("components/morpheus.server.gremlin/templates/index.tp");
                        me.render();
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
                     
                }
                
            },
            
            /**
             * Send a gremlin command up to the server to be evaluated.
             * 
             * @param statement
             *            is the statement string
             * @param cb
             *            (optional) callback that is called with the result
             *            object. If this is not specified, the result will be
             *            printed to the console.
             */
            evaluate : function(statement, cb) {
                var cb = cb || me.evalCallback;
                
                console.log(me.server);
                me.server.admin.post("gremlin/", {value:JSON.stringify({command:statement})}, (function(statement, cb) {
                    return function(data) {
                        cb(statement, data);
                    };
                })(statement, cb));
                
            },
            
            init : function() {
                $( window ).bind( "hashchange", me.hashchange );
                me.hashchange();
            }
            
    };
    
    // 
    // PRIVATE
    //
    
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
            server : me.server
        });
        
        me.consoleElement   = $("#mor_gremlin_console");
        me.consoleInputWrap = $("#mor_gremlin_console_input_wrap");
        me.consoleInput     = $("#mor_gremlin_console_input");
        me.consoleLineCount     = $(".mor_gremlin_linecount ");
        
    };
    
    /**
     * Default callback for evaluated gremlin statements. Prints the result to
     * the ui console.
     */
    me.evalCallback = function(originalStatement, data) {

        me.consoleInputWrap.before($("<p>" + originalStatement + "</p>"));
        for( var key in data ) {
            me.consoleInputWrap.before($("<p> ==&gt;" + data[key] + "</p>"));
            me.consoleLineCount.append($("<li></li>"));
        }

        me.consoleLineCount.append($("<li><p>&gt;</p></li>"));
        
    };
    
    //
    // CONSTRUCT
    //
    
    /**
     * Look for enter-key press on input field.
     */
    $("#mor_gremlin_console_input").live("keyup", function(ev) {
        if( ev.keyCode === 13 ) {
            me.public.evaluate(me.consoleInput.val());
            me.consoleInput.val("");
        }
    });
    
    return me.public;
    
})(jQuery);

//
// REGISTER STUFF
//

morpheus.ui.addPage("morpheus.server.gremlin",morpheus.components.server.gremlin);
morpheus.ui.mainmenu.add("Gremlin","morpheus.server.gremlin", null, "server");

morpheus.event.bind("morpheus.init", morpheus.components.server.monitor.init);
morpheus.event.bind("morpheus.ui.page.changed", morpheus.components.server.gremlin.pageChanged);
morpheus.event.bind("morpheus.server.changed",  morpheus.components.server.gremlin.serverChanged);