package net.osmand.plus.views.mapwidgets.configure.settings;

import android.widget.TextView;

import com.google.android.material.slider.Slider;

import net.osmand.plus.R;
import net.osmand.plus.utils.ColorUtilities;
import net.osmand.plus.utils.UiUtilities;
import net.osmand.plus.views.mapwidgets.AverageSpeedComputer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import androidx.annotation.NonNull;

public abstract class BaseAverageSpeedSettingFragment extends WidgetSettingsBaseFragment {

	protected long selectedIntervalMillis;

	protected void setupIntervalSlider() {
		TextView interval = view.findViewById(R.id.interval);
		TextView selectedIntervalText = view.findViewById(R.id.selected_interval);
		Slider slider = view.findViewById(R.id.interval_slider);

		Map<Long, String> intervals = getAvailableIntervals();
		List<Entry<Long, String>> intervalsList = new ArrayList<>(intervals.entrySet());
		int initialIntervalIndex = getInitialIntervalIndex();

		slider.setValueFrom(0);
		slider.setValueTo(intervals.size() - 1);
		slider.setValue(initialIntervalIndex);
		slider.clearOnChangeListeners();
		slider.addOnChangeListener((slider1, intervalIndex, fromUser) -> {
			Entry<Long, String> newInterval = intervalsList.get((int) intervalIndex);
			selectedIntervalMillis = newInterval.getKey();
			selectedIntervalText.setText(newInterval.getValue());
		});
		UiUtilities.setupSlider(slider, nightMode, ColorUtilities.getActiveColor(app, nightMode), true);

		interval.setText(getString(R.string.ltr_or_rtl_combine_via_colon, getString(R.string.shared_string_interval), ""));
		selectedIntervalText.setText(intervalsList.get(initialIntervalIndex).getValue());

	}

	private int getInitialIntervalIndex() {
		List<Long> intervals = new ArrayList<>(getAvailableIntervals().keySet());
		for (int i = 0; i < intervals.size(); i++) {
			long interval = intervals.get(i);
			if (selectedIntervalMillis == interval) {
				return i;
			}
		}

		return 0;
	}

	@NonNull
	private Map<Long, String> getAvailableIntervals() {
		Map<Long, String> intervals = new LinkedHashMap<>();
		for (long interval : AverageSpeedComputer.MEASURED_INTERVALS) {
			boolean seconds = interval < 60 * 1000;
			String timeInterval = seconds
					? String.valueOf(interval / 1000)
					: String.valueOf(interval / 1000 / 60);
			String timeUnit = interval < 60 * 1000
					? getString(R.string.shared_string_sec)
					: getString(R.string.shared_string_minute_lowercase);
			intervals.put(interval, getString(R.string.ltr_or_rtl_combine_via_space, timeInterval, timeUnit));
		}
		return intervals;
	}

	protected void setupMinMaxIntervals() {
		List<String> intervals = new ArrayList<>(getAvailableIntervals().values());
		String minIntervalValue = intervals.get(0);
		String maxIntervalValue = intervals.get(intervals.size() - 1);

		TextView minInterval = view.findViewById(R.id.min_interval);
		TextView maxInterval = view.findViewById(R.id.max_interval);

		minInterval.setText(minIntervalValue);
		maxInterval.setText(maxIntervalValue);
	}
}