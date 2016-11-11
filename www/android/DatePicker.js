function DatePicker() {}

DatePicker.prototype.ANDROID_THEMES = {
	THEME_TRADITIONAL          : 1, // default
	THEME_HOLO_DARK            : 2,
	THEME_HOLO_LIGHT           : 3,
	THEME_DEVICE_DEFAULT_DARK  : 4,
	THEME_DEVICE_DEFAULT_LIGHT : 5
};

DatePicker.prototype.show = function(options, cb, errCb) {

	if (options.date && options.date instanceof Date) {
		options.date = (options.date.getMonth() + 1) + "/" +
					   (options.date.getDate()) + "/" +
					   (options.date.getFullYear()) + "/" +
					   (options.date.getHours()) + "/" +
					   (options.date.getMinutes());
	}

	var defaults = {
		mode : 'date',
		date : '',
		minDate: 0,
		maxDate: 0,
		titleText: '',
		cancelText: '',
		okText: '',
		todayText: '',
		nowText: '',
		is24Hour: false,
		androidTheme : window.datePicker.ANDROID_THEMES.THEME_TRADITIONAL, // Default theme
	};

	for (var key in defaults) {
		if (typeof options[key] !== "undefined") {
			defaults[key] = options[key];
		}
	}

	var callback = function(message) {
		if(message != 'error'){
			var parser = /(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2}).(\d{3})(\+[0-9]{2}:[0-9]{2})/i;
			var result = parser.exec(message);

			cb(result);
		} else {
			// TODO error popup?
		}
	}

	var errCallback = function(message) {
		if (typeof errCb === 'function') {
			errCb(message);
		}
	}

	cordova.exec(callback, errCallback, "DatePickerPlugin", defaults.mode, [defaults] );
};

var datePicker = new DatePicker();
module.exports = datePicker;

if (!window.plugins) {
	window.plugins = {};
}

if (!window.plugins.datePicker) {
	window.plugins.datePicker = datePicker;
}
