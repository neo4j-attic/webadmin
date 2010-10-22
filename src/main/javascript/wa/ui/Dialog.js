wa.ui.Dialog = (function($){
    var me = {};
    
    me.container = null;
    
    me.showImpl = function() {
        $("#mor_dialog_content").modal({
            overlayId: 'mor_dialog_overlay',
            containerId: 'mor_dialog_container',
            closeHTML: null,
            minHeight: 80,
            opacity: 65, 
            position: ['300',],
            overlayClose: true,
            onOpen: me.adjustHeight
        });
    };
    
    me.adjustHeight = function (d) {
        me.container = d.container[0];
        var h = $("#mor_dialog_data", me.container).height()
            +   $("#mor_dialog_title", me.container).height()
            + 20; // padding
        d.container.css( { height: h } );
    };
    
    return {
        show : function(title, body, cb) {
            me.cb = cb;
            $("#mor_dialog_title").html(title);
            $("#mor_dialog_data").html(body);
            me.showImpl();
        },
        
        showUsingTemplate : function( title, templateUrl, templateContext, cb ) {
            cb = typeof(templateContext) === "function" ? templateContext : cb;
            me.cb = cb;
            $("#mor_dialog_title").html(title);
            $("#mor_dialog_data").setTemplateURL(templateUrl);
            $("#mor_dialog_data").processTemplate(templateContext || {});
            me.showImpl();
        },
        
        updateTemplateContext : function(templateContext) {
        	$("#mor_dialog_data").processTemplate(templateContext || {});
        	me.adjustHeight();
        },
        
        close : function() {
            $.modal.close();
        }
    };
})(jQuery);
