package com.busgo.trip;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class WeekdayUtils {

    // Accept inputs as:
    // - JSON array string like ["MONDAY","WEDNESDAY"]
    // - comma-separated string 'MON,WED' or 'MONDAY,WEDNESDAY'
    // - array object from the client (List or String[] when serialized)
    // Returns a JSON array string of full DayOfWeek names: ["MONDAY","WEDNESDAY"]

    public static String normalizeWeekdays(Object input) {
        if (input == null) return "[]";
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            // If it's already a JSON array string, try parsing
            if (input instanceof String) {
                String s = (String) input;
                s = s.trim();
                // If it looks like JSON array
                if (s.startsWith("[")) {
                    String[] arr = om.readValue(s, String[].class);
                    return normalizeArray(arr);
                }
                // otherwise treat as comma-separated
                String[] parts = s.split(",");
                for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
                return normalizeArray(parts);
            }
            // If it's a list or array object
            if (input instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Object> lst = (java.util.List<Object>) input;
                String[] arr = lst.stream().map(Object::toString).toArray(String[]::new);
                return normalizeArray(arr);
            }
            if (input instanceof String[]) {
                String[] arr = (String[]) input;
                return normalizeArray(arr);
            }
            // fallback to toString
            return normalizeArray(new String[]{input.toString()});
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid weekdays format: " + ex.getMessage());
        }
    }

    private static String normalizeArray(String[] arr) {
        List<String> out = new ArrayList<>();
        for (String raw : arr) {
            if (raw == null) continue;
            String token = raw.trim().toUpperCase(Locale.ROOT);
            if (token.length() == 0) continue;
            String mapped = mapToFullDay(token);
            out.add(mapped);
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            return om.writeValueAsString(out);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to serialize weekdays: " + ex.getMessage());
        }
    }

    private static String mapToFullDay(String token) {
        // Accept full name
        try {
            java.time.DayOfWeek dow = java.time.DayOfWeek.valueOf(token);
            return dow.name();
        } catch (Exception ignored) {}
        // Accept 3-letter codes MON, TUE, WED, THU, FRI, SAT, SUN
        if (token.length() == 3) {
            switch (token) {
                case "MON": return java.time.DayOfWeek.MONDAY.name();
                case "TUE": return java.time.DayOfWeek.TUESDAY.name();
                case "WED": return java.time.DayOfWeek.WEDNESDAY.name();
                case "THU": return java.time.DayOfWeek.THURSDAY.name();
                case "FRI": return java.time.DayOfWeek.FRIDAY.name();
                case "SAT": return java.time.DayOfWeek.SATURDAY.name();
                case "SUN": return java.time.DayOfWeek.SUNDAY.name();
            }
        }
        // Accept numeric 1-7 (ISO day-of-week, 1=MON,7=SUN)
        try {
            int v = Integer.parseInt(token);
            if (v >=1 && v <=7) {
                return java.time.DayOfWeek.of(v).name();
            }
        } catch (Exception ignored) {}

        throw new IllegalArgumentException("Invalid weekday token: " + token);
    }
}
