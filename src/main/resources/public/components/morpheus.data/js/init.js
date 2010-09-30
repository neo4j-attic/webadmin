morpheus.provide("morpheus.components.data.base");

//$.require( "components/morpheus.data/js/manager.js" );

/**
 * Data browser.
 */
morpheus.components.data.base = (function($, undefined) {
    
    var me = {};
    
    me.basePage = $("<div></div>");
    me.ui = {};

	me.propertiesToListManager = morpheus.components.data.propertiesToListManager;
    me.uiLoaded  = false;
    me.server = null;
    
    me.visible = false;
    
    me.dataUrl = null;
    me.currentItem = null;
    
    /**
     * Pagination counter for the related nodes table.
     */
    me.currentRelatedNodePage = 0;
    
    /**
     * Number of related nodes to show per page.
     */
    me.relatedNodesPerPage = 10;
    
    //
    // PUBLIC
    //
    
    me.api = {
            
            getPage :  function() {
                return me.basePage;
            },
            
            pageChanged : function(ev) {
                
                if(ev.data === "morpheus.data") {
                    
            		me.visible = true;
                
                    if( me.uiLoaded === false ) {
                    	me.uiLoaded = true;
                        me.basePage.setTemplateURL("components/morpheus.data/templates/index.tp");
	                    
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
            	return morpheus.Servers.getCurrentServer();
            },
            
            /**
             * Return the current item being viewed.
             */
            getItem : function() {
            	return me.currentItem;
            },
            
            reload : function() {
                
                var server = morpheus.Servers.getCurrentServer();
                
                if( server ) {
                	
                	if( typeof(me.dataUrl) !== "undefined" && me.dataUrl !== null ) {
                		server.get(me.dataUrl, function(data) {
                    		me.currentRelatedNodePage = 0;
                            
                        	if( data !== null ) {
                        		
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
                        	} else {
                        		me.currentItem = false;
                            	me.notFound = true;
                            	me.render();
                        	}
                        }, function(request) {
                        	
                        	me.currentItem = false;
                        	me.notFound = true;
                        	me.render();
                        	
                        });
                    } else {
                    	me.api.setDataUrl("node/0");
                    }
                } else {
                	me.render();
                }
                
            }
            
    };
    
    // 
    // PRIVATE
    //
    
    me.reload = me.api.reload;
    
    /**
	 * Triggered when showing a node. This will load all relations for the
	 * current node, and re-draw the UI.
	 */
    me.reloadRelations = function() {
    	var relationshipUrl = me.dataUrl + "/relationships/all";
    	
    	var server = morpheus.Servers.getCurrentServer();
    	
    	server.get(relationshipUrl, function(data) {

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
    	
    	var server = morpheus.Servers.getCurrentServer();
        
        server.post(traversalUrl, traversal, function(data) {
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
    	
    	var server = morpheus.Servers.getCurrentServer();
        
        server.get(startUrl, function(data) {
    		me.currentItem.startNode = data;
    		me.currentItem.startNode.fields = me.extractFields([data]);
    		me.render();
    	});
    	
    	server.get(endUrl, function(data) {
    		me.currentItem.endNode = data;
    		me.currentItem.endNode.fields = me.extractFields([data]);
    		me.render();
    	});
    };
    
    
    me.render = function() {
    	
    	var item = me.currentItem ? me.currentItem : {isNode:false,isRelationship:false};
    	
    	me.addPaginationMetaData(item);
    	
    	me.basePage.processTemplate({
            server : me.server,
            dataUrl : me.dataUrl,
            item : item,
            notFound : me.notFound === true ? true : false
        });
    };
    
    me.addPaginationMetaData = function(item) {
    	
    	// Pagination pre-calculations
    	var relatedNodeCount = item.isNode ? item.relationships.data.length : 0
    	
    	var relatedNodeStartIndex = me.currentRelatedNodePage * me.relatedNodesPerPage;
    	var relatedNodeStopIndex = relatedNodeStartIndex + me.relatedNodesPerPage - 1;
    	var nodePageCount = Math.ceil(relatedNodeCount / me.relatedNodesPerPage);
    	if( relatedNodeStopIndex >= relatedNodeCount ) {
    		relatedNodeStopIndex = relatedNodeCount - 1;
    	}
    	
    	if( ! item.relationships ) {
    		item.relationships = {};
    	}
    	
    	item.relationships.pagination = {
			relatedNodeStartIndex : relatedNodeStartIndex,
            relatedNodeStopIndex : relatedNodeStopIndex,
            relatedNodePage : me.currentRelatedNodePage,
            relatedNodePageCount : nodePageCount > 0 ? nodePageCount : 1,
            relatedNodesPerPage : me.relatedNodesPerPage
    	};
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
        
        if( url !== me.dataUrl && me.notFound !== true) {
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
        var server = morpheus.Servers.getCurrentServer();
        return server.stripUrlBase(url);
    };
    
    //
    // CONSTRUCT
    //
    
    $("a.mor_data_url_button").live("click", function(ev) {
    	ev.preventDefault();
    	
    	me.api.setDataUrl($(ev.target).attr('href'));
    });
    
    $("a.mor_data_refresh_button").live("click", function(ev){
    	ev.preventDefault();
    	
    	me.reload();
    });
    
    $("input.mor_data_get_node_button").live("click", function(ev) {
    	ev.preventDefault();
    	
    	me.api.setDataUrl("node/" + $("#mor_data_get_id_input").val() );
    });
    
    $("input.mor_data_get_relationship_button").live("click", function(ev) {
    	ev.preventDefault();
    	
    	me.api.setDataUrl("relationship/" + $("#mor_data_get_id_input").val() );
    });
    
    $("a.mor_data_reference_node_button").live("click", function(ev) {
    	ev.preventDefault();
    	
    	me.api.setDataUrl("node/0" );
    });
    
    $("a.mor_data_paginate_next").live("click", function(ev){
    	ev.preventDefault();
    	if(me.currentItem && me.currentItem.isNode) {
    		var count = me.currentItem.relationships.data.length;
    		if(me.currentRelatedNodePage * me.relatedNodesPerPage + me.relatedNodesPerPage < count) {
    			me.currentRelatedNodePage++;
    			me.render();
    		}
    	}
    });
    
    $("a.mor_data_paginate_previous").live("click", function(ev){
    	ev.preventDefault();
    	if(me.currentItem && me.currentItem.isNode) {
    		if(me.currentRelatedNodePage > 0) {
    			me.currentRelatedNodePage--;
    			me.render();
    		}
    	}
    });
    
    return me.api;
    
})(jQuery);

//
// REGISTER STUFF
//

morpheus.ui.Pages.add("morpheus.data",morpheus.components.data.base);
morpheus.ui.MainMenu.add({ label : "Data", pageKey:"morpheus.data", index:1, perspectives:['server']});

morpheus.event.bind("morpheus.init", morpheus.components.data.base.init);
morpheus.event.bind("morpheus.ui.page.changed", morpheus.components.data.base.pageChanged);
morpheus.event.bind("morpheus.servers.current.changed",  morpheus.components.data.base.serverChanged);