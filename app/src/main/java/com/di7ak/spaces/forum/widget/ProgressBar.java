package com.di7ak.spaces.forum.widget;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.view.View;
import com.di7ak.spaces.forum.R;
import com.rey.material.widget.ProgressView;

public class ProgressBar {
    private Snackbar bar;
    private Activity activity;

    public ProgressBar(Activity activity) {
        this.activity = activity;
    }

    public void hide() {
        activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (bar != null) bar.dismiss();
                }
            });
    }

    public boolean isShown() {
        return bar == null ? false : bar.isShown();
    }

    public void showProgress(final String message) {
        activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (isShown()) bar.dismiss();
                    bar = Snackbar.make(activity.getWindow().getDecorView(), message, Snackbar.LENGTH_INDEFINITE);

                    Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) bar.getView();
                    View snackView = activity.getLayoutInflater().inflate(R.layout.progress_snackbar, layout, false);
                    ProgressView pv = (ProgressView)snackView.findViewById(R.id.progress_pv_circular_determinate);
                    pv.start();
                    layout.addView(snackView, 0);

                    bar.show();
                }
            });
    }

    public void showError(final String message, final String btnAction, final View.OnClickListener action) {
        activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (isShown()) bar.dismiss();
                    bar = Snackbar.make(activity.getWindow().getDecorView(), message, Snackbar.LENGTH_INDEFINITE);
                    if (btnAction != null && action != null) {
                        bar.setAction(btnAction, action);
                    }
                    bar.show();
                }
            });
    }
}
