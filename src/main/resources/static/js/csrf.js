(function () {
    function getCookie(name) {
        var match = document.cookie.match(new RegExp('(^|;\\s*)' + name + '=([^;]*)'));
        return match ? decodeURIComponent(match[2]) : null;
    }

    var SAFE_METHODS = ['GET', 'HEAD', 'OPTIONS', 'TRACE'];

    $(document).ajaxSend(function (event, jqXHR, settings) {
        var method = (settings.type || 'GET').toUpperCase();
        if (SAFE_METHODS.indexOf(method) !== -1) {
            return;
        }
        var token = getCookie('XSRF-TOKEN');
        if (token) {
            jqXHR.setRequestHeader('X-XSRF-TOKEN', token);
        }
    });
})();
