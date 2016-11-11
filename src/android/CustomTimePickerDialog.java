package com.plugin.datepicker;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Workaround for this bug: https://code.google.com/p/android/issues/detail?id=222208
 * In Android 7.0 Nougat, spinner mode for the TimePicker in TimePickerDialog is
 * incorrectly displayed as clock, even when the theme specifies otherwise, such as:
 * <p>
 * <resources>
 * <style name="Theme.MyApp" parent="Theme.AppCompat.Light.NoActionBar">
 * <item name="android:timePickerStyle">@style/Widget.MyApp.TimePicker</item>
 * </style>
 * <p>
 * <style name="Widget.MyApp.TimePicker" parent="android:Widget.Material.TimePicker">
 * <item name="android:timePickerMode">spinner</item>
 * </style>
 * </resources>
 * <p>
 * May also pass TimePickerDialog.THEME_HOLO_LIGHT as an argument to the constructor,
 * as this theme has the TimePickerMode set to spinner.
 */
public class CustomTimePickerDialog extends TimePickerDialog {
	private final static int TIME_PICKER_INTERVAL = 15;
	private TimePicker timePicker;
	private final OnTimeSetListener callback;
	private  int _minute;

	/**
	 * Creates a new time picker dialog.
	 *
	 * @param context      the parent context
	 * @param callback     the callback to call when the time is set
	 * @param hourOfDay    the initial hour
	 * @param minute       the initial minute
	 * @param is24HourView whether this is a 24 hour view or AM/PM
	 */
	public CustomTimePickerDialog(Context context, OnTimeSetListener callback, int hourOfDay, int minute, boolean is24HourView) {

		super(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert, callback, hourOfDay, minute / TIME_PICKER_INTERVAL, is24HourView);

		this._minute = minute;
		this.callback = callback;

		fixSpinner(context, hourOfDay, minute, is24HourView);
	}

	private void fixSpinner(Context context, int hourOfDay, int minute, boolean is24HourView) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // android:timePickerMode spinner and clock began in Lollipop
			try {
				TimePicker timePicker = (TimePicker) findField(TimePickerDialog.class, TimePicker.class, "mTimePicker").get(this);
				Class<?> delegateClass = Class.forName("android.widget.TimePicker$TimePickerDelegate");
				Field delegateField = findField(TimePicker.class, delegateClass, "mDelegate");
				Object delegate = delegateField.get(timePicker);
				Class<?> spinnerDelegateClass = Class.forName("android.widget.TimePickerSpinnerDelegate");
				// In 7.0 Nougat for some reason the timePickerMode is ignored and the delegate is TimePickerClockDelegate
				if (delegate.getClass() != spinnerDelegateClass) {
					delegateField.set(timePicker, null); // throw out the TimePickerClockDelegate!
					timePicker.removeAllViews(); // remove the TimePickerClockDelegate views
					Constructor spinnerDelegateConstructor = spinnerDelegateClass.getConstructor(TimePicker.class, Context.class, AttributeSet.class, int.class, int.class);
					spinnerDelegateConstructor.setAccessible(true);
					// Instantiate a TimePickerSpinnerDelegate
					delegate = spinnerDelegateConstructor.newInstance(timePicker, context, null, android.R.attr.timePickerStyle, 0);
					delegateField.set(timePicker, delegate); // set the TimePicker.mDelegate to the spinner delegate
					// Set up the TimePicker again, with the TimePickerSpinnerDelegate
					timePicker.setIs24HourView(is24HourView);
					timePicker.setCurrentHour(hourOfDay);
					timePicker.setCurrentMinute(minute);
					timePicker.setOnTimeChangedListener(this);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static Field findField(Class objectClass, Class fieldClass, String expectedName) {
		try {
			Field field = objectClass.getDeclaredField(expectedName);
			field.setAccessible(true);
			return field;
		} catch (NoSuchFieldException e) {
		}
		// search for it if it wasn't found under the expected ivar name
		for (Field searchField : objectClass.getDeclaredFields()) {
			if (searchField.getType() == fieldClass) {
				searchField.setAccessible(true);
				return searchField;
			}
		}
		return null;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (this.callback != null && timePicker != null) {
			timePicker.clearFocus();
			this.callback.onTimeSet(timePicker, timePicker.getCurrentHour(), timePicker.getCurrentMinute() * TIME_PICKER_INTERVAL);
		}
	}

	@Override
	protected void onStop() {
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		try {
			Class<?> classForid = Class.forName("com.android.internal.R$id");
			Field timePickerField = classForid.getField("timePicker");
			this.timePicker = (TimePicker) findViewById(timePickerField.getInt(null));

			Field field = classForid.getField("minute");

			NumberPicker mMinuteSpinner = (NumberPicker) timePicker.findViewById(field.getInt(null));
			mMinuteSpinner.setMinValue(0);
			mMinuteSpinner.setMaxValue((60 / TIME_PICKER_INTERVAL) - 1);
			mMinuteSpinner.setValue(this._minute / TIME_PICKER_INTERVAL);
			List<String> displayedValues = new ArrayList<String>();
			for (int i = 0; i < 60; i += TIME_PICKER_INTERVAL) {
				displayedValues.add(String.format("%02d", i));
			}

			mMinuteSpinner.setDisplayedValues(displayedValues.toArray(new String[0]));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
