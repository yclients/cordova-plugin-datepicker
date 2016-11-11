var exec = require('cordova/exec');

function DatePicker() { this._callback; }

DatePicker.prototype.show = function(options, cb) {
	var padDate = function(date) {
	  if (date.length == 1) {
		return ("0" + date);
	  }
	  return date;
	};

	var formatDate = function(date){
		if (!(date instanceof Date)) {
			date = new Date(date);
		}

		date = date.getFullYear()
			+ "-"
			+ padDate(date.getMonth()+1)
			+ "-"
			+ padDate(date.getDate())
			+ "T"
			+ padDate(date.getHours())
			+ ":"
			+ padDate(date.getMinutes())
			+ ":00Z";

	  return date
	}

	if (options.date) {
		options.date = formatDate(options.date);
	}

	if (options.minDate) {
		options.minDate = formatDate(options.minDate);
	}

	if (options.maxDate) {
		options.maxDate = formatDate(options.maxDate);
	}

	if (options.popoverArrowDirection) {
		options.popoverArrowDirection = this._popoverArrowDirectionIntegerFromString(options.popoverArrowDirection);
		console.log('ha options', this, options.popoverArrowDirection);
	}

	var defaults = {
		mode: 'date',
		date: new Date(),
		allowOldDates: true,
		allowFutureDates: true,
		minDate: '',
		maxDate: '',
		doneButtonLabel: 'Done',
		doneButtonColor: '#007AFF',
		cancelButtonLabel: 'Cancel',
		cancelButtonColor: '#007AFF',
		locale: "NL",
		x: '0',
		y: '0',
		minuteInterval: 1,
		popoverArrowDirection: this._popoverArrowDirectionIntegerFromString("any"),
		locale: "en_US"
	};

	for (var key in defaults) {
		if (typeof options[key] !== "undefined") {
			defaults[key] = options[key];
		}
	}

	this._callback = cb;

	exec(null, null, "DatePicker", "show", [defaults] );
};

DatePicker.prototype._dateSelected = function(date) {
  var parser = /(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2}).(\d{3})(\+[0-9]{2}:[0-9]{2})/i;
  var result = parser.exec(date);

  if (this._callback) {
	this._callback(result);
  }
};

DatePicker.prototype._dateSelectionCanceled = function() {
	if (this._callback) {
		this._callback();
	}
};

DatePicker.prototype._UIPopoverArrowDirection = {
	"up": 1,
	"down": 2,
	"left": 4,
	"right": 8,
	"any": 15
};

DatePicker.prototype._popoverArrowDirectionIntegerFromString = function (string) {
	if (typeof this._UIPopoverArrowDirection[string] !== "undefined") {
		return this._UIPopoverArrowDirection[string];
	}

	return this._UIPopoverArrowDirection.any;
};

var datePicker = new DatePicker();
module.exports = datePicker;

if (!window.plugins) {
	window.plugins = {};
}
if (!window.plugins.datePicker) {
	window.plugins.datePicker = datePicker;
}
