package com.docd.purefm.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.text.SpannableString;
import android.text.format.DateFormat;

import com.docd.purefm.file.GenericFile;
import com.docd.purefm.text.style.DashSpan;

import org.jetbrains.annotations.NotNull;

public final class TextUtil {

    private TextUtil() {}
    
    private static SimpleDateFormat format;
    
    public static void init(Context context) {
        if (DateFormat.is24HourFormat(context)) {
            format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        } else {
            format = new SimpleDateFormat("dd/MM/yyyy KK:mm aa", Locale.getDefault());
        }
    }

    @NotNull
    public static synchronized String humanReadableDate(long last) {
        return format.format(last);
    }
    
    public static int stringMonthToInt(String month) {
        if (month.equals("Jan")) {
            return Calendar.JANUARY;
        }
        if (month.equals("Feb")) {
            return Calendar.FEBRUARY;
        }
        if (month.equals("Mar")) {
            return Calendar.MARCH;
        }
        if (month.equals("Apr")) {
            return Calendar.APRIL;
        }
        if (month.equals("May")) {
            return Calendar.MAY;
        }
        if (month.equals("Jun")) {
            return Calendar.JUNE;
        }
        if (month.equals("Jul")) {
            return Calendar.JULY;
        }
        if (month.equals("Aug")) {
            return Calendar.AUGUST;
        }
        if (month.equals("Sep")) {
            return Calendar.SEPTEMBER;
        }
        if (month.equals("Oct")) {
            return Calendar.OCTOBER;
        }
        if (month.equals("Nov")) {
            return Calendar.NOVEMBER;
        }
        if (month.equals("Dec")) {
            return Calendar.DECEMBER;
        }
        return 0;
    }

    public static SpannableString fileListToDashList(final Iterable<GenericFile> files) {
        final StringBuilder fileList = new StringBuilder(66);
        for (final GenericFile file : files) {
            fileList.append(file.getName());
            fileList.append('\n');
        }
        // remove last '\n'
        fileList.deleteCharAt(fileList.length() - 1);
        final SpannableString ss = new SpannableString(fileList.toString());
        ss.setSpan(new DashSpan(), 0, ss.length(), 0);
        return ss;
    }
}