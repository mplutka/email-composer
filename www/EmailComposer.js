function EmailComposer() {
	this.resultCallback = null; // Function
}

EmailComposer.ComposeResultType = {
	Cancelled : 0,
	Saved : 1,
	Sent : 2,
	Failed : 3,
	NotSent : 4
}

/**
 * @private
 *
 * Creates a callback, which will be executed within a specific scope.
 *
 * @param {Function} callbackFn
 *      The callback function
 * @param {Object} scope
 *      The scope for the function
 *
 * @return {Function}
 *      The new callback function
 */
EmailComposer.prototype.createCallbackFn = function (callbackFn, scope) {
    return function () {
        if (typeof callbackFn == 'function') {
            callbackFn.apply(scope || this, arguments);
        }
    }
}

/**
 * Verifies if sending emails is supported on the device.
 *
 * @param {Function} callback
 *      A callback function to be called with the result
 * @param {Object} scope
 *      The scope of the callback
 */
EmailComposer.prototype.isServiceAvailable = function (callback, scope) {
    var callbackFn = this.createCallbackFn(callback, scope);

    cordova.exec(callbackFn, null, 'EmailComposer', 'isServiceAvailable', []);
}

// showEmailComposer : all args optional

EmailComposer.prototype.showEmailComposer = function(subject, body, toRecipients, ccRecipients, bccRecipients, bIsHTML, attachments, attachmentsData) {
	console.log("****************************AVVIATO");
	var args = {};
	if (toRecipients) {
            args.toRecipients = toRecipients;            
        }
	if (ccRecipients) {
            args.ccRecipients = ccRecipients;
        }
	if (bccRecipients) {
            args.bccRecipients = bccRecipients;            
        }
	if (subject) {
            args.subject = subject;            
        }
	if (body) {
            args.body = body;            
        }
	if (bIsHTML) {
            args.bIsHTML = bIsHTML;            
        }
	if (attachments) {
            args.attachments = attachments;            
        }
        if (attachmentsData) {
            args.attachmentsData = attachmentsData;            
        }
        
	cordova.exec(this.resultCallback, null, "EmailComposer", "showEmailComposer", [args]);
}

EmailComposer.prototype.showEmailComposerWithCallback = function(callback, subject, body, toRecipients, ccRecipients, bccRecipients, isHTML, attachments, attachmentsData) {
	this.resultCallback = callback;
	this.showEmailComposer.apply(this, [subject, body, toRecipients, ccRecipients, bccRecipients, isHTML, attachments, attachmentsData]);
}

EmailComposer.prototype._didFinishWithResult = function(res) {
	this.resultCallback(res);
}

cordova.addConstructor(function() {
	console.log("****************************");
	if (!window.plugins) {
            window.plugins = {};
	}

	// shim to work in 1.5 and 1.6
	if (!window.Cordova) {
            window.Cordova = cordova;
	}

	window.plugins.emailComposer = new EmailComposer();
});