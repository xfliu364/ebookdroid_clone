package org.ebookdroid.ui.settings;

import org.ebookdroid.common.settings.AppSettings;
import org.ebookdroid.common.settings.SettingsManager;
import org.ebookdroid.common.settings.books.BookSettings;
import org.ebookdroid.common.settings.definitions.AppPreferences;
import org.ebookdroid.common.settings.definitions.BookPreferences;
import org.ebookdroid.common.settings.definitions.LibPreferences;
import org.ebookdroid.common.settings.definitions.OpdsPreferences;
import org.ebookdroid.common.settings.types.DocumentViewMode;
import org.ebookdroid.common.settings.types.PageAlign;
import org.ebookdroid.core.curl.PageAnimationType;

import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.emdev.ui.preference.SeekBarPreference;
import org.emdev.utils.LengthUtils;
import org.emdev.utils.enums.EnumUtils;

/**
 * @author whippet
 *
 */
public class PreferencesDecorator implements IPreferenceContainer, AppPreferences, BookPreferences, LibPreferences, OpdsPreferences {

    private final Map<String, CharSequence> summaries = new HashMap<String, CharSequence>();

    private final Map<String, CompositeListener> listeners = new HashMap<String, CompositeListener>();

    private final IPreferenceContainer parent;

    public PreferencesDecorator(final IPreferenceContainer parent) {
        this.parent = parent;
    }

    @Override
    public Preference findPreference(final CharSequence key) {
        return parent.findPreference(key);
    }

    public void decorateSettings() {
        decorateBooksSettings();
        decorateBrowserSettings();
        decorateOpdsSettings();
        decorateMemorySettings();
        decorateRenderSettings();
        decorateScrollSettings();
        decorateUISettings();
    }

    public void decorateBooksSettings() {
        decoratePreferences(BOOK_CONTRAST.key, BOOK_EXPOSURE.key);
        decoratePreferences(BOOK_VIEW_MODE.key, BOOK_PAGE_ALIGN.key, BOOK_ANIMATION_TYPE.key);
        addViewModeListener(BOOK_VIEW_MODE.key, BOOK_PAGE_ALIGN.key, BOOK_ANIMATION_TYPE.key);
        addAnimationTypeListener(BOOK_ANIMATION_TYPE.key, BOOK_PAGE_ALIGN.key);

        BookSettings bs = SettingsManager.getBookSettings();
        if (bs != null) {
            enableSinglePageModeSetting(bs.viewMode, BOOK_PAGE_ALIGN.key, BOOK_ANIMATION_TYPE.key);
        }
    }

    public void decorateBrowserSettings() {
        decoratePreferences(AUTO_SCAN_DIRS.key);
    }

    public void decorateOpdsSettings() {
        decoratePreferences(OPDS_DOWNLOAD_DIR.key);
    }

    public void decorateMemorySettings() {
        decoratePreferences(PAGES_IN_MEMORY.key, VIEW_TYPE.key, DECODE_THREAD_PRIORITY.key, DRAW_THREAD_PRIORITY.key,
                BITMAP_SIZE.key, HEAP_PREALLOCATE.key);
    }

    public void decorateRenderSettings() {
        decoratePreferences(CONTRAST.key, EXPOSURE.key);
        decoratePreferences(VIEW_MODE.key, PAGE_ALIGN.key, ANIMATION_TYPE.key);
        addViewModeListener(VIEW_MODE.key, PAGE_ALIGN.key, ANIMATION_TYPE.key);
        addAnimationTypeListener(ANIMATION_TYPE.key, PAGE_ALIGN.key);

        enableSinglePageModeSetting(AppSettings.current().viewMode, PAGE_ALIGN.key, ANIMATION_TYPE.key);

        decoratePreferences(DJVU_RENDERING_MODE.key, PDF_CUSTOM_XDPI.key, PDF_CUSTOM_YDPI.key, FB2_FONT_SIZE.key);
    }

    public void decorateScrollSettings() {
        decoratePreferences(SCROLL_HEIGHT.key, TOUCH_DELAY.key);
    }

    public void decorateUISettings() {
        decoratePreferences(ROTATION.key, BRIGHTNESS.key, PAGE_NUMBER_TOAST_POSITION.key, ZOOM_TOAST_POSITION.key);
    }

    public void setPageAlign(final PageAnimationType type, final String alignPrefKey) {
        if (type != null && type != PageAnimationType.NONE) {
            final ListPreference alignPref = (ListPreference) findPreference(alignPrefKey);
            alignPref.setValue(PageAlign.AUTO.getResValue());
            setListPreferenceSummary(alignPref, alignPref.getValue());
        }
    }

    public void enableSinglePageModeSetting(final DocumentViewMode type, final String... relatedKeys) {
        for (final String relatedKey : relatedKeys) {
            final Preference pref = findPreference(relatedKey);
            if (pref != null) {
                pref.setEnabled(type == DocumentViewMode.SINGLE_PAGE);
            }
        }
    }

    protected void decoratePreferences(final String... keys) {
        for (final String key : keys) {
            decoratePreference(parent.findPreference(key));
        }
    }

    protected void decoratePreference(final Preference pref) {
        if (pref instanceof ListPreference) {
            decorateListPreference((ListPreference) pref);
        } else if (pref instanceof EditTextPreference) {
            decorateEditPreference((EditTextPreference) pref);
        } else if (pref instanceof SeekBarPreference) {
            decorateSeekPreference((SeekBarPreference) pref);
        }
    }

    protected void decorateEditPreference(final EditTextPreference textPrefs) {
        final CharSequence summary = textPrefs.getSummary();
        summaries.put(textPrefs.getKey(), summary);

        final String value = textPrefs.getText();

        setPreferenceSummary(textPrefs, value);

        addListener(textPrefs, new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                setPreferenceSummary(textPrefs, (String) newValue);
                return true;
            }
        });
    }

    protected void decorateSeekPreference(final SeekBarPreference textPrefs) {
        final CharSequence summary = textPrefs.getSummary();
        summaries.put(textPrefs.getKey(), summary);

        final int value = textPrefs.getValue();

        setPreferenceSummary(textPrefs, "" + value);

        addListener(textPrefs, new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                setPreferenceSummary(textPrefs, (String) newValue);
                return true;
            }
        });
    }

    protected void decorateListPreference(final ListPreference listPrefs) {
        final CharSequence summary = listPrefs.getSummary();
        summaries.put(listPrefs.getKey(), summary);

        final String value = listPrefs.getValue();

        setListPreferenceSummary(listPrefs, value);

        addListener(listPrefs, new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                setListPreferenceSummary(listPrefs, (String) newValue);
                return true;
            }
        });
    }

    protected void setListPreferenceSummary(final ListPreference listPrefs, final String value) {
        final int selected = Arrays.asList(listPrefs.getEntryValues()).indexOf(value);
        setPreferenceSummary(listPrefs, selected != -1 ? (String) listPrefs.getEntries()[selected] : null);
    }

    protected void setPreferenceSummary(final Preference listPrefs, final String value) {
        final CharSequence summary = summaries.get(listPrefs.getKey());
        listPrefs.setSummary(summary + (LengthUtils.isNotEmpty(value) ? (": " + value) : ""));
    }

    protected void addListener(final String key, final OnPreferenceChangeListener l) {
        final Preference pref = parent.findPreference(key);
        if (pref != null) {
            addListener(pref, l);
        }
    }

    protected void addListener(final Preference pref, final OnPreferenceChangeListener l) {
        final String key = pref.getKey();
        CompositeListener cl = listeners.get(key);
        if (cl == null) {
            cl = new CompositeListener();
            pref.setOnPreferenceChangeListener(cl);
            listeners.put(key, cl);
        }
        cl.add(l);
    }

    protected void addAnimationTypeListener(final String source, final String target) {
        addListener(source, new AnimationTypeListener(target));
    }

    protected void addViewModeListener(final String source, final String... targets) {
        addListener(source, new ViewModeListener(targets));
    }

    protected static class CompositeListener implements OnPreferenceChangeListener {

        final List<OnPreferenceChangeListener> listeners = new LinkedList<Preference.OnPreferenceChangeListener>();

        @Override
        public boolean onPreferenceChange(final Preference preference, final Object newValue) {
            for (final OnPreferenceChangeListener l : listeners) {
                if (!l.onPreferenceChange(preference, newValue)) {
                    return false;
                }
            }
            return true;
        }

        public boolean add(final OnPreferenceChangeListener object) {
            return listeners.add(object);
        }
    }

    protected class AnimationTypeListener implements OnPreferenceChangeListener {

        private final String relatedKey;

        public AnimationTypeListener(final String relatedKey) {
            this.relatedKey = relatedKey;
        }

        @Override
        public boolean onPreferenceChange(final Preference preference, final Object newValue) {
            final PageAnimationType type = EnumUtils.getByResValue(PageAnimationType.class, newValue.toString(), null);
            setPageAlign(type, relatedKey);
            return true;
        }
    }

    protected class ViewModeListener implements OnPreferenceChangeListener {

        private final String[] relatedKeys;

        public ViewModeListener(final String[] relatedKeys) {
            this.relatedKeys = relatedKeys;
        }

        @Override
        public boolean onPreferenceChange(final Preference preference, final Object newValue) {
            final DocumentViewMode type = EnumUtils.getByResValue(DocumentViewMode.class, newValue.toString(), null);
            enableSinglePageModeSetting(type, relatedKeys);
            return true;
        }
    }
}
