package com.savemaster.savefromfb.util.dialog;

import android.content.Context;
import android.content.DialogInterface;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.savemaster.savefromfb.R;

public class DialogUtils {

    public static void showEnjoyAppDialog(Context context, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.app_name)
                .setMessage(R.string.savemasterdown_rating_message)
                .setCancelable(false)
                .setPositiveButton(R.string.savemasterdown_rating_yes, positiveListener)
                .setNegativeButton(R.string.savemasterdown_rating_not_really, negativeListener)
                .show();
    }

    public static void showAskRatingAppDialog(Context context, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.app_name)
                .setMessage(R.string.savemasterdown_rating_on_play_store)
                .setCancelable(false)
                .setPositiveButton(R.string.savemasterdown_rating_ok_sure, positiveListener)
                .setNegativeButton(R.string.savemasterdown_rating_no_thanks, negativeListener)
                .show();
    }

    public static void showFeedBackDialog(Context context, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.app_name)
                .setMessage(R.string.savemasterdown_rating_giving_feedback)
                .setCancelable(false)
                .setPositiveButton(R.string.savemasterdown_rating_ok_sure, positiveListener)
                .setNegativeButton(R.string.savemasterdown_rating_no_thanks, negativeListener)
                .show();
    }
}
