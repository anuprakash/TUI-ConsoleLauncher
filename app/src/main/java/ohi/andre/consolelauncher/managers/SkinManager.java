package ohi.andre.consolelauncher.managers;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.util.SparseIntArray;

import java.util.HashMap;

public class SkinManager {

    public static final int SYSTEM_WALLPAPER = -1;

    public static final int SUGGESTION_PADDING_VERTICAL = 15;
    public static final int SUGGESTION_PADDING_HORIZONTAL = 15;
    public static final int SUGGESTION_MARGIN = 20;
    //	default
    public static final int deviceDefault = 0xffff9800;
    public static final int inputDefault = 0xff00ff00;
    public static final int outputDefault = 0xffffffff;
    public static final int ramDefault = 0xfff44336;
    public static final int bgDefault = 0xff000000;

    public static final int suggestionTextColorDefault = 0xff000000;
    public static final int aliasSuggestionBgDefault = 0xffFF5722;
    public static final int appSuggestionBgDefault = 0xff00897B;
    public static final int commandSuggestionsBgDefault = 0xff76FF03;
    public static final int songSuggestionBgDefault = 0xffEEFF41;
    public static final int contactSuggestionBgDefault = 0xff64FFDA;
    public static final int fileSuggestionBgDeafult = 0xff03A9F4;
    public static final int defaultSuggestionBgDefault = 0xffFFFFFF;

    public static final int defaultSize = 15;
    private static final int deviceScale = 3;
    private static final int textScale = 2;
    private static final int ramScale = 3;
    private static final int suggestionScale = 0;

    private Typeface globalTypeface;
    private int globalFontSize;

    private int deviceColor;
    private int inputColor;
    private int outputColor;
    private int ramColor;
    private int bgColor;

    private boolean useSystemWp;
    private boolean showSuggestions;

    private int suggestionTextColor;

    private int defaulSuggestionColor;
    private int appSuggestionColor;
    private int aliasSuggestionColor;
    private int musicSuggestionColor;
    private int contactsSuggestionColor;
    private int commandSuggestionColor;
    private int fileSuggestionColor;

    private boolean multicolorSuggestions;
    private boolean transparentSuggestions;

    public SkinManager(PreferencesManager prefs, Typeface lucidaConsole) {
        boolean systemFont = Boolean.parseBoolean(prefs.getValue(PreferencesManager.USE_SYSTEMFONT));
        globalTypeface = systemFont ? Typeface.DEFAULT : lucidaConsole;

        try {
            globalFontSize = Integer.parseInt(prefs.getValue(PreferencesManager.FONTSIZE));
        } catch (Exception e) {
            globalFontSize = SkinManager.defaultSize;
        }

        try {
            useSystemWp = Boolean.parseBoolean(prefs.getValue(PreferencesManager.USE_SYSTEMWP));
            if (useSystemWp)
                bgColor = SYSTEM_WALLPAPER;
            else
                bgColor = Color.parseColor(prefs.getValue(PreferencesManager.BG));
        } catch (Exception e) {
            bgColor = bgDefault;
        }

        try {
            deviceColor = Color.parseColor(prefs.getValue(PreferencesManager.DEVICE));
        } catch (Exception e) {
            deviceColor = deviceDefault;
        }

        try {
            inputColor = Color.parseColor(prefs.getValue(PreferencesManager.INPUT));
        } catch (Exception e) {
            inputColor = inputDefault;
        }

        try {
            outputColor = Color.parseColor(prefs.getValue(PreferencesManager.OUTPUT));
        } catch (Exception e) {
            outputColor = outputDefault;
        }

        try {
            ramColor = Color.parseColor(prefs.getValue(PreferencesManager.RAM));
        } catch (Exception e) {
            ramColor = ramDefault;
        }

        showSuggestions = Boolean.parseBoolean(prefs.getValue(PreferencesManager.SHOWSUGGESTIONS));
        if (showSuggestions) {

            try {
                suggestionTextColor = Color.parseColor(prefs.getValue(PreferencesManager.SUGGESTIONTEXT_COLOR));
            } catch (Exception e) {
                suggestionTextColor = suggestionTextColorDefault;
            }

            try {
                transparentSuggestions = Boolean.parseBoolean(prefs.getValue(PreferencesManager.TRANSPARENT_SUGGESTIONS));
            } catch (Exception e) {
                transparentSuggestions = false;
            }

            try {
                multicolorSuggestions = Boolean.parseBoolean(prefs.getValue(PreferencesManager.USE_MULTICOLOR_SUGGESTIONS));
            } catch (Exception e) {
                multicolorSuggestions = false;
            }

            if(multicolorSuggestions) {
                transparentSuggestions = false;
            }

            if(transparentSuggestions && suggestionTextColor == bgColor) {
                suggestionTextColor = Color.GREEN;
            } else {
                try {
                    defaulSuggestionColor = Color.parseColor(prefs.getValue(PreferencesManager.DEFAULT_SUGGESTION_BG));
                } catch (Exception e) {
                    defaulSuggestionColor = defaultSuggestionBgDefault;
                }

                if(multicolorSuggestions) {
                    try {
                        appSuggestionColor = Color.parseColor(prefs.getValue(PreferencesManager.APP_SUGGESTION_BG));
                    } catch (Exception e) {
                        appSuggestionColor = appSuggestionBgDefault;
                    }

                    try {
                        contactsSuggestionColor = Color.parseColor(prefs.getValue(PreferencesManager.CONTACT_SUGGESTION_BG));
                    } catch (Exception e) {
                        contactsSuggestionColor = contactSuggestionBgDefault;
                    }

                    try {
                        commandSuggestionColor = Color.parseColor(prefs.getValue(PreferencesManager.COMMAND_SUGGESTION_BG));
                    } catch (Exception e) {
                        commandSuggestionColor = commandSuggestionsBgDefault;
                    }

                    try {
                        musicSuggestionColor = Color.parseColor(prefs.getValue(PreferencesManager.SONG_SUGGESTION_BG));
                    } catch (Exception e) {
                        musicSuggestionColor = songSuggestionBgDefault;
                    }

                    try {
                        fileSuggestionColor = Color.parseColor(prefs.getValue(PreferencesManager.FILE_SUGGESTION_BG));
                    } catch (Exception e) {
                        fileSuggestionColor = fileSuggestionBgDeafult;
                    }

                    try {
                        aliasSuggestionColor = Color.parseColor(prefs.getValue(PreferencesManager.ALIAS_SIGGESTION_BG));
                    } catch (Exception e) {
                        aliasSuggestionColor = aliasSuggestionBgDefault;
                    }
                }
            }
        }
    }

    public Typeface getGlobalTypeface() {
        return globalTypeface;
    }

    public int getDeviceColor() {
        return deviceColor;
    }

    public int getInputColor() {
        return inputColor;
    }

    public int getOutputColor() {
        return outputColor;
    }

    public int getRamColor() {
        return ramColor;
    }

    public int getBgColor() {
        return bgColor;
    }

    public boolean getUseSystemWp() {
        return useSystemWp;
    }

    public boolean getShowSuggestions() {
        return showSuggestions;
    }

    public ColorDrawable getSuggestionBg(Integer type) {
        if(transparentSuggestions) {
            type = 0;
        }

        if(transparentSuggestions) {
            return new ColorDrawable(Color.TRANSPARENT);
        } else if(!multicolorSuggestions) {
            return new ColorDrawable(defaulSuggestionColor);
        } else {
            switch (type) {
                case SuggestionsManager.Suggestion.TYPE_APP:
                    return new ColorDrawable(appSuggestionColor);
                case SuggestionsManager.Suggestion.TYPE_ALIAS:
                    return new ColorDrawable(aliasSuggestionColor);
                case SuggestionsManager.Suggestion.TYPE_COMMAND:
                    return new ColorDrawable(commandSuggestionColor);
                case SuggestionsManager.Suggestion.TYPE_CONTACT:
                    return new ColorDrawable(contactsSuggestionColor);
                case SuggestionsManager.Suggestion.TYPE_FILE:
                    return new ColorDrawable(fileSuggestionColor);
                case SuggestionsManager.Suggestion.TYPE_SONG:
                    return new ColorDrawable(musicSuggestionColor);
                default:
                    return new ColorDrawable(defaulSuggestionColor);
            }
        }
    }

    public int getSuggestionTextColor() {
        return suggestionTextColor;
    }

    public int getDeviceSize() {
        return globalFontSize - deviceScale;
    }

    public int getTextSize() {
        return globalFontSize - textScale;
    }

    public int getRamSize() {
        return globalFontSize - ramScale;
    }

    public int getSuggestionSize() {
        return globalFontSize - suggestionScale;
    }

}
