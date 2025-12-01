package com.shejan.theclock;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class AlarmRepository {

    private static final String PREFS_NAME = "alarm_prefs";
    private static final String KEY_ALARMS = "alarms_list";

    public static void saveAlarms(Context context, List<Alarm> alarmList) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        JSONArray jsonArray = new JSONArray();
        for (Alarm alarm : alarmList) {
            try {
                jsonArray.put(alarmToJson(alarm));
            } catch (JSONException e) {
                android.util.Log.e("AlarmRepository", "Error parsing alarms JSON", e);
            }
        }

        editor.putString(KEY_ALARMS, jsonArray.toString());
        editor.apply();
    }

    private static JSONObject alarmToJson(Alarm alarm) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", alarm.getId());
        jsonObject.put("hour", alarm.getHour());
        jsonObject.put("minute", alarm.getMinute());
        jsonObject.put("isEnabled", alarm.isEnabled());
        jsonObject.put("label", alarm.getLabel());
        jsonObject.put("isRecurring", alarm.isRecurring());

        jsonObject.put("ringtoneUri", alarm.getRingtoneUri());
        jsonObject.put("snoozeEnabled", alarm.isSnoozeEnabled());
        jsonObject.put("snoozeInterval", alarm.getSnoozeInterval());
        jsonObject.put("snoozeTimes", alarm.getSnoozeTimes());

        JSONArray daysArray = new JSONArray();
        for (Integer day : alarm.getDays()) {
            daysArray.put(day);
        }
        jsonObject.put("days", daysArray);
        return jsonObject;
    }

    public static List<Alarm> loadAlarms(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_ALARMS, null);
        List<Alarm> alarmList = new ArrayList<>();

        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Alarm alarm = new Alarm();
                    alarm.setId(jsonObject.optString("id"));
                    alarm.setHour(jsonObject.optInt("hour"));
                    alarm.setMinute(jsonObject.optInt("minute"));
                    alarm.setEnabled(jsonObject.optBoolean("isEnabled"));
                    alarm.setLabel(jsonObject.optString("label", "Alarm"));
                    alarm.setRecurring(jsonObject.optBoolean("isRecurring"));
                    alarm.setRingtoneUri(jsonObject.optString("ringtoneUri"));
                    alarm.setSnoozeEnabled(jsonObject.optBoolean("snoozeEnabled", true));
                    alarm.setSnoozeInterval(jsonObject.optInt("snoozeInterval", 5));
                    alarm.setSnoozeTimes(jsonObject.optInt("snoozeTimes", 3));

                    List<Integer> days = new ArrayList<>();
                    JSONArray daysArray = jsonObject.optJSONArray("days");
                    if (daysArray != null) {
                        for (int j = 0; j < daysArray.length(); j++) {
                            days.add(daysArray.getInt(j));
                        }
                    }
                    alarm.setDays(days);

                    alarmList.add(alarm);
                }
            } catch (JSONException e) {
                android.util.Log.e("AlarmRepository", "Error parsing alarms JSON", e);
            }
        }

        return alarmList;
    }
}
