(function (win) {
    var hasOwnProperty = Object.prototype.hasOwnProperty;
    var JSBridge = win.JSBridge || (win.JSBridge = {});
    var JS_BRIDGE_PROTOCOL = 'JsBridge';
    var Inner = {
        callbacks: {},
        call: function (obj, method, params, callback) {
            console.log(obj+" "+method+" "+params+" "+callback);
            var port = Util.getPort();
            console.log(port);
            this.callbacks[port] = callback;
            var uri=Util.getUri(obj,method,params,port);
            console.log(uri);
            window.prompt(uri, "");
        },
        onFinish: function (port, jsonObj){
            var callback = this.callbacks[port];
            callback && callback(jsonObj);
            delete this.callbacks[port];
        },
    };
    var Util = {
        getPort: function () {
            return Math.floor(Math.random() * (1 << 30));
        },
        getUri:function(obj, method, params, port){
            params = this.getParam(params);
            var uri = JS_BRIDGE_PROTOCOL + '://' + obj + ':' + port + '/' + method + '?' + params;
            return uri;
        },
        getParam:function(obj){
            if (obj && typeof obj === 'object') {
                return JSON.stringify(obj);
            } else {
                return obj || '';
            }
        }
    };
    for (var key in Inner) {
        if (!hasOwnProperty.call(JSBridge, key)) {
            JSBridge[key] = Inner[key];
        }
    }
})(window);