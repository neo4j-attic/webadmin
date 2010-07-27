morpheus.provide("morpheus.components.server.monitor");

morpheus.components.server.gremlin = (function($, undefined) {
    
    var me = {};
    
    me.basePage = $("<div></div>");
    me.ui = {};
    
    me.uiLoaded  = false;
    me.server = null;
    
    me.visible = false;
    
    me.consoleElement = null;
    
    me.history = [];
    me.currentHistoryIndex = -1;
    
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
                        me.uiLoaded = true;
                        me.basePage.setTemplateURL("components/morpheus.server.gremlin/templates/index.tp");
                        me.render();
                    }
                    
                } else {
                    me.visible = false;
                }
            },
            
            serverChanged : function(ev) {
                
                me.server = ev.data.server;
                
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
                
                me.server.admin.post("gremlin/", {command:statement}, (function(statement, cb) {
                    return function(data) {
                        cb(statement, data);
                    };
                })(statement, cb));
                
            },
            
            init : function() {

            },
            
            pushHistory : function(cmd) {
                me.history.push(cmd);
                me.currentHistoryIndex = me.history.length - 1;
            },
            
            prevHistory : function() {
                if( me.currentHistoryIndex >= 0 && me.history.length > me.currentHistoryIndex ) {
                    me.currentHistoryIndex--;
                    return me.history[me.currentHistoryIndex + 1];
                } else if (me.history.length > 0) {
                    return me.history[0];
                } else {
                    return "";
                }
            },
            
            nextHistory : function() {
                if( me.history.length > (me.currentHistoryIndex + 1) ) {
                    me.currentHistoryIndex++;
                    return me.history[me.currentHistoryIndex];
                } else {
                    return "";
                }
            }
            
    };
    
    // 
    // PRIVATE
    //
    
    me.render = function() {
        
        me.basePage.processTemplate({
            server : me.server
        });
        
        me.consoleWrap      = $(".mor_gremlin_console_wrap");
        me.consoleElement   = $("#mor_gremlin_console");
        me.consoleInputWrap = $("#mor_gremlin_console_input_wrap");
        me.consoleInput     = $("#mor_gremlin_console_input");
        
    };
    
    /**
     * Default callback for evaluated gremlin statements. Prints the result to
     * the ui console.
     */
    me.evalCallback = function(originalStatement, data) {

        for( var key in data ) {
            me.writeConsoleLine(data[key], '==&gt; ');
        }
        
        
    };
    
    me.writeConsoleLine = function(line, prepend) {
        var prepend = prepend || "&gt; ";
        me.consoleInputWrap.before($("<p> " + prepend + line + "</p>"));
        me.consoleWrap[0].scrollTop = me.consoleWrap[0].scrollHeight;
    };
    
    //
    // CONSTRUCT
    //
    
    /**
     * Look for enter-key press on input field.
     */
    $("#mor_gremlin_console_input").live("keyup", function(ev) {
        if( ev.keyCode === 13 ) { // ENTER
            me.public.evaluate(me.consoleInput.val());
            
            me.writeConsoleLine(me.consoleInput.val());
            
            if( me.consoleInput.val().length > 0) {
                me.public.pushHistory(me.consoleInput.val());
            } 
             
            me.consoleInput.val(" ");
        } else if (ev.keyCode === 38) { // UP
            me.consoleInput.val(me.public.prevHistory());
        } else if (ev.keyCode === 40) { // DOWN
            me.consoleInput.val(me.public.nextHistory());
        }
    });
    
    return me.public;
    
})(jQuery);

//
// REGISTER STUFF
//

morpheus.ui.addPage("morpheus.server.gremlin",morpheus.components.server.gremlin);
morpheus.ui.mainmenu.add("Gremlin","morpheus.server.gremlin", null, "server");

morpheus.event.bind("morpheus.init", morpheus.components.server.gremlin.init);
morpheus.event.bind("morpheus.ui.page.changed", morpheus.components.server.gremlin.pageChanged);
morpheus.event.bind("morpheus.server.changed",  morpheus.components.server.gremlin.serverChanged);