package org.emdev.ui;

import org.ebookdroid.R;
import org.ebookdroid.ui.about.AboutActivity;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import org.emdev.ui.actions.ActionController;
import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.actions.ActionMethod;

public abstract class AbstractActionActivity<A extends Activity, C extends ActionController<A>> extends Activity {

    public static final String ACTIVITY_RESULT_DATA = "activityResultData";
    public static final String ACTIVITY_RESULT_CODE = "activityResultCode";
    public static final String ACTIVITY_RESULT_ACTION_ID = "activityResultActionId";

    private C controller;

    protected AbstractActionActivity() {
    }

    @Override
    public final Object onRetainNonConfigurationInstance() {
        return getController();
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    public final C restoreController() {
        final Object last = this.getLastNonConfigurationInstance();
        if (last instanceof ActionController) {
            this.controller = (C) last;
            return controller;
        }
        return null;
    }

    public final C getController() {
        if (controller == null) {
            controller = createController();
        }
        return controller;
    }

    protected abstract C createController();

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        final int actionId = item.getItemId();
        final ActionEx action = getController().getOrCreateAction(actionId);
        if (action.getMethod().isValid()) {
            action.run();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final int actionId = item.getItemId();
        final ActionEx action = getController().getOrCreateAction(actionId);
        if (action.getMethod().isValid()) {
            action.run();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public final void onButtonClick(final View view) {
        final int actionId = view.getId();
        final ActionEx action = getController().getOrCreateAction(actionId);
        action.onClick(view);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        if (data != null) {
            final int actionId = data.getIntExtra(ACTIVITY_RESULT_ACTION_ID, 0);
            if (actionId != 0) {
                final ActionEx action = getController().getOrCreateAction(actionId);
                action.putValue(ACTIVITY_RESULT_CODE, Integer.valueOf(resultCode));
                action.putValue(ACTIVITY_RESULT_DATA, data);
                action.run();
            }
        }
    }

    public final void setActionForView(final int id) {
        final View view = findViewById(id);
        final ActionEx action = getController().getOrCreateAction(id);
        if (view != null && action != null) {
            view.setOnClickListener(action);
        }
    }

    @ActionMethod(ids = R.id.mainmenu_about)
    public void showAbout(final ActionEx action) {
        final Intent i = new Intent(this, AboutActivity.class);
        startActivity(i);
    }
}
