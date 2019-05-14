function sendLocationRequest(message) {
    try {
        webkit.messageHandlers.locationHandler.postMessage(message);
    } catch(err) {
        console.warn('The native context does not exist yet');
    }
}

var handlers = {};
var count = 0;
var nextId = 1;

var lastPosition = null;
var lastError = null;

window._nativePositionDenied = function() {
    // console.log("navigator.geolocation._nativePositionDenied", location);
    lastPosition = null;
    lastError = { code: 1, message: "denied" };
    for (var key in handlers) {
        var handler = handlers[key].error;
        handler && handler(lastError);
    }
};

window._nativePositionChanged = function(location) {
    // console.log("navigator.geolocation._nativePositionChanged", location);
    lastPosition = location;
    lastError = null;
    for (var key in handlers) {
        var handler = handlers[key].success;
        handler && handler(location);
    }
};

navigator.geolocation.getCurrentPosition = function(success, error, options) {   
    var id = 0;
    var successHandler = function () {
        success && success();
        clearWatch(id);
    };
    var errorHandler = function () {
        error && error();
        clearWatch(id);
    };
    id = watchPosition(successHandler, errorHandler, options);
};

navigator.geolocation.watchPosition = function(success, error, options) {
    if (count == 0) {
        sendLocationRequest("watchPosition");
    } else if (success && lastPosition) {
        setTimeout(function() { success(lastPosition); }, 1);
    } else if (error && lastError) {
        setTimeout(function() { error(lastError); }, 1);
    }
    
    var id = nextId++;
    count++;
        
    handlers[id] = {
        success: success,
        error: error,
    };
    return id;
};

navigator.geolocation.clearWatch = function(id) {
    if (handlers[id]) {
        delete handlers[id];
        count--;
    }
    
    if (count <= 0) {
        sendLocationRequest("stopPosition");
    }
};
