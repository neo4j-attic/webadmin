morpheus.provide("morpheus.components.server.data.base");

//$.require( "components/morpheus.server.data/js/manager.js" );

/**
 * Data browser.
 */
morpheus.components.server.data.base = (function($, undefined) {
    
    var me = {};
    
    me.basePage = $("<div></div>");
    me.ui = {};

	me.propertiesToListManager = morpheus.components.server.data.propertiesToListManager;
    me.uiLoaded  = false;
    me.server = null;
    
    me.visible = false;
    
    me.dataUrl = null;
    me.currentItem = null;
    
    //
    // PUBLIC
    //
    
    me.public = {
            
            getPage :  function() {
                return me.basePage;
            },
            
            pageChanged : function(ev) {
                
                if(ev.data === "morpheus.server.data") {
                    
            		me.visible = true;
                
                    if( me.uiLoaded === false ) {
                    	me.uiLoaded = true;
                        me.basePage.setTemplateURL("components/morpheus.server.data/templates/index.tp");
	                    
	                    me.reload();
                    }
                	
                } else {
                    me.visible = false;
                }
            },
            
            serverChanged : function(ev) {
                
                me.server = ev.data.server;
                
                // If the monitor page is currently visible
                if( me.visible === true ) {
                	me.reload();
                }
            },
            
            init : function() {
            	morpheus.event.bind('morpheus.data.listnames.changed',me.listNamesChanged);
            	
                $( window ).bind( "hashchange", me.hashchange );
                me.hashchange();
            },
            
            setDataUrl : function(url) {
            	$.bbq.pushState({ dataurl: me.stripUrlBase(url) });
            },
            
            /**
             * Get the current server being browsed.
             */
            getServer : function() {
            	return me.server;
            },
            
            /**
             * Return the current item being viewed.
             */
            getItem : function() {
            	return me.currentItem;
            }
            
    };
    
    // 
    // PRIVATE
    //
    
    me.reload = function() {
        
        if( me.server ) {
        	
        	if( typeof(me.dataUrl) !== "undefined" && me.dataUrl !== null ) {
                me.server.rest.get(me.dataUrl, function(data) { 
                    
                	me.currentItem = data;
                	me.currentItem.fields = me.extractFields([me.currentItem]);
            		
                	me.currentItem.isNode = me.dataUrl.indexOf("node") == 0 ? true : false;
                	me.currentItem.isRelationship = me.dataUrl.indexOf("relationship") == 0 ? true : false;
                    
                	me.notFound = false;
                	
                	if( me.currentItem.isNode ) {
                		me.currentItem.relationships = {
                			fields : me.propertiesToListManager.getListFields(),
                			data : [],
                		};
                	}
                	
                    me.render();
                    
                    if( me.currentItem.isNode ) {
                    	me.reloadRelations();
                    } else if (me.currentItem.isRelationship) {
                    	me.reloadRelationshipNodes();
                    }
                }, function(request) {
                	
                	me.currentItem = false;
                	me.notFound = true;
                	me.render();
                	
                });
            } else {
            	me.public.setDataUrl("node/0");
            }
        } else {
        	me.render();
        }
        
    };
    
    /**
	 * Triggered when showing a node. This will load all relations for the
	 * current node, and re-draw the UI.
	 */
    me.reloadRelations = function() {
    	var relationshipUrl = me.dataUrl + "/relationships/all";
    	
    	me.server.rest.get(relationshipUrl, function(data) {

    		// For each relation, find out which node is the "other" node, in
			// relation to the current node we're showing.
    		for( var i = 0, l = data.length; i < l; i ++) {
    			if( me.currentItem.self === data[i].start) {
    				data[i].otherNode = data[i].end;
    				data[i].direction = "FROM";
    			} else {
    				data[i].otherNode = data[i].start;
    				data[i].direction = "TO";
    			}
    		}
    		
    		me.currentItem.relationships.data = data;
    		me.currentItem.relationships.nodes = {};
    		
    		me.render();
    		
    		// Alright. Now we fetch all nodes on the other side of the
			// relationships.
    		me.reloadRelatedNodes();
    		
    	});
    	
    };
    
    /**
	 * This is triggered by me.reloadRelations, it will fetch all related nodes
	 * to the current node.
	 */
    me.reloadRelatedNodes = function() {
    	
    	var traversalUrl = me.dataUrl + "/traverse/node";
    	var traversal = {
    		"max depth": 1
    	};
    	
    	me.server.rest.post(traversalUrl, traversal, function(data) {
    		// Create a lookup table for related nodes, to make it easy for the
			// template to render it.
    		me.currentItem.relationships.nodes = nodes = {};
    		for( var i = 0, l = data.length; i < l; i ++) {
    			nodes[data[i].self] = data[i];
    		}
    		
    		me.render();
    	});
    	
    };
    
    /**
	 * Triggered when showing a relationship. This will load the start and end
	 * nodes for the current relationship.
	 */
    me.reloadRelationshipNodes = function() {
    	var startUrl = me.stripUrlBase(me.currentItem.start);
    	var endUrl = me.stripUrlBase(me.currentItem.end);
    	
    	me.server.rest.get(startUrl, function(data) {
    		me.currentItem.startNode = data;
    		me.currentItem.startNode.fields = me.extractFields([data]);
    		me.render();
    	});
    	
    	me.server.rest.get(endUrl, function(data) {
    		me.currentItem.endNode = data;
    		me.currentItem.endNode.fields = me.extractFields([data]);
    		me.render();
    	});
    };
    
    
    me.render = function() {
    	me.basePage.processTemplate({
            server : me.server,
            dataUrl : me.dataUrl,
            item : me.currentItem ? me.currentItem : {isNode:false,isRelationship:false},
            notFound : me.notFound === true ? true : false
        });
    };
    
    /**
	 * This is triggered when the list of names to show when listing nodes
	 * changes.
	 */
    me.listNamesChanged = function(ev) {
    	if( me.currentItem && me.currentItem.relationships) {
			me.currentItem.relationships.fields = me.propertiesToListManager.getListFields();
			me.render();
    	}
    };
    
    /**
	 * Triggered when the URL hash state changes.
	 */
    me.hashchange = function(ev) {
        var url = $.bbq.getState( "dataurl" );
        
        if( url !== me.dataUrl) {
        	me.dataUrl = url;
        	me.reload();
    	}
    };
    
    /**
	 * Get a list of field names from a set of data.
	 */
    me.extractFields = function(data) {
    	
    	var nameMap = {};
    	for(var i=0,l=data.length; i < l; i++) {
    		for (var key in data[i].data ) {
    			nameMap[key] = true;
    		}
    	}
    	
    	var names = [];
    	for( var name in nameMap ) {
    		names.push(name);
    	}
    	
    	return names;
    };
    
    me.stripUrlBase = function(url) {
    	if( typeof(url) !== undefined ) {
        	if( url.indexOf("://") !== -1) {
        		url = me.server.urls.stripBase(url);
        	}
    	}
    	
    	return url;
    };
    
    //
    // CONSTRUCT
    //
    
    $("a.mor_data_url_button").live("click", function(ev) {
    	ev.preventDefault();
    	
    	me.public.setDataUrl($(ev.target).attr('href'));
    });
    
    $("a.mor_data_refresh_button").live("click", function(ev){
    	ev.preventDefault();
    	
    	me.reload();
    });
    
    $("input.mor_data_get_node_button").live("click", function(ev) {
    	ev.preventDefault();
    	
    	me.public.setDataUrl("node/" + $("#mor_data_get_id_input").val() );
    });
    
    $("input.mor_data_get_relationship_button").live("click", function(ev) {
    	ev.preventDefault();
    	
    	me.public.setDataUrl("relationship/" + $("#mor_data_get_id_input").val() );
    });
    
    $("a.mor_data_reference_node_button").live("click", function(ev) {
    	ev.preventDefault();
    	
    	me.public.setDataUrl("node/0" );
    });
    
    return me.public;
    
})(jQuery);

//
// REGISTER STUFF
//

morpheus.ui.addPage("morpheus.server.data",morpheus.components.server.data.base);
morpheus.ui.mainmenu.add("Data","morpheus.server.data", null, "server",1);

morpheus.event.bind("morpheus.init", morpheus.components.server.data.base.init);
morpheus.event.bind("morpheus.ui.page.changed", morpheus.components.server.data.base.pageChanged);
morpheus.event.bind("morpheus.server.changed",  morpheus.components.server.data.base.serverChanged);