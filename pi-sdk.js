if (!window.Pi) {
    var Pi = {
        _onIncompletePaymentFound: null,
        init: function(e) {
            if (!e.version) throw new Error("version is required");
            if ("2.0" !== e.version) throw new Error("Unsupported version");
            this.version = e.version;
            this.sandbox = e.sandbox || false;
        },
        authenticate: function(e, t) {
            if (!this.version) throw new Error("Pi.init must be called before Pi.authenticate");
            if (!e || 0 === e.length) throw new Error("scopes are required");
            if (!t) throw new Error("onIncompletePaymentFound is required");
            this._onIncompletePaymentFound = t;
            var n = window.parent;
            return new Promise(function(t, o) {
                var r = function(e) {
                    if (e.origin.window === n && e.data && "pi_auth_result" === e.data.type) {
                        window.removeEventListener("message", r);
                        var i = e.data.payload;
                        i.error ? o(i.error) : t(i);
                    }
                };
                window.addEventListener("message", r);
                n.postMessage({ type: "pi_auth_request", payload: { scopes: e, sandbox: Pi.sandbox } }, "*");
            });
        },
        createPayment: function(e, t) {
            if (!this._onIncompletePaymentFound) throw new Error("Pi.authenticate must be called before Pi.createPayment");
            var n = window.parent;
            return new Promise(function(o, r) {
                var s = function(e) {
                    if (e.origin.window === n && e.data && "pi_payment_result" === e.data.type) {
                        window.removeEventListener("message", s);
                        var i = e.data.payload;
                        i.error ? r(i.error) : o(i);
                    }
                };
                window.addEventListener("message", s);
                n.postMessage({ type: "pi_payment_request", payload: { payment: e, callbacks: t, sandbox: Pi.sandbox } }, "*");
            });
        }
    };
    window.Pi = Pi;
}
