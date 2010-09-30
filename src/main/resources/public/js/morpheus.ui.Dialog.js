morpheus.ui.Dialog = (function($){
    var me = {};
    
    me.container = null;
    
    me.showImpl = function() {
        $("#mor_dialog_content").modal({
            overlayId: 'mor_dialog_overlay',
            containerId: 'mor_dialog_container',
            closeHTML: null,
            minHeight: 80,
            opacity: 65, 
            position: ['0',],
            overlayClose: true,
            onOpen: me.open,
            onClose: me.close
        });
    };
    
    me.open = function (d) {
        me.container = d.container[0];
        d.overlay.fadeIn('slow', function () {
            $("#mor_dialog_content", me.container).show();
            var title = $("#mor_dialog_title", me.container);
            title.show();
            d.container.slideDown('slow', function () {
                setTimeout(function () {
                    var h = $("#mor_dialog_data", me.container).height()
                        + title.height()
                        + 20; // padding
                    d.container.animate(
                        {height: h}, 
                        100,
                        function () {
                            $("div.close", me.container).show();
                            $("#mor_dialog_data", me.container).show();
                            
                            if( typeof(me.cb) === "function" ) {
                                me.cb(true);
                            }
                            
                        }
                    );
                }, 100);
            });
        })
    };
    
    me.close = function (d) {
        d.container.animate(
            {top:"-" + (d.container.height() + 20)},
            200,
            function () {
                $.modal.close();
            }
        );
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
        
        close : function() {
            $.modal.close();
        }
    };
})(jQuery);
