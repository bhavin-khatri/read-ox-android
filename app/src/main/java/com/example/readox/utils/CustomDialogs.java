package com.example.readox.utils;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ActionMenuView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.readox.R;

public class CustomDialogs {

    Context mContext;

    public CustomDialogs(Context mContext) {
        this.mContext = mContext;
    }

    Dialog dialog;

    public Dialog onWifiDisConnectedDialog(Activity context, final OnWifiDisConnectedDialog onDisConnectedDialog) {
        dialog = new Dialog(context);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog.setContentView(R.layout.no_internet_dialog);
        dialog.setCancelable(false);

        TextView descriptionTV = dialog.findViewById(R.id.descriptionTV);
        TextView titleTV = dialog.findViewById(R.id.titleTV);
        TextView settingsText = (TextView) dialog.findViewById(R.id.settingBtn);
        TextView retryText = (TextView) dialog.findViewById(R.id.cancelBtn);

        titleTV.setText(R.string.no_internet_title);
        descriptionTV.setText(R.string.no_internet_description);
        settingsText.setText(R.string.txtSettings);
        retryText.setText(R.string.txtRetry);
        settingsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (onDisConnectedDialog != null)
                    onDisConnectedDialog.onGotoSettings(dialog);
            }
        });

        retryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (onDisConnectedDialog != null)
                    onDisConnectedDialog.onSkip(dialog);
            }
        });
        Window window = dialog.getWindow();
        window.setLayout(RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
        return dialog;
    }

    public interface OnWifiDisConnectedDialog {

        void onGotoSettings(Dialog dialog);

        void onSkip(Dialog dialog);


    }

    public interface OnDialogListener {

        void onDialogYes(Dialog dialog);

        void onDialogNo(Dialog dialog);

    }

    public void customLogoutScreen(Activity mContext, OnDialogListener onDialogListener) {
//
        Log.e("TAG", "customLogoutScreen: ");
        dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.custom_exit_logout_dialog);
        dialog.setCancelable(false);

        TextView dialogYesBtn = dialog.findViewById(R.id.yesBtn);
        TextView dialogNoBtn = dialog.findViewById(R.id.noBtn);
        TextView descriptionTV = dialog.findViewById(R.id.descriptionTV);
        TextView titleTV = dialog.findViewById(R.id.titleTV);

        titleTV.setText(R.string.logout_title);
        descriptionTV.setText(R.string.logout_description);
        dialogYesBtn.setText(R.string.txtYes);
        dialogNoBtn.setText(R.string.txtNo);


        dialogYesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (onDialogListener != null) {
                    onDialogListener.onDialogYes(dialog);
                }
            }
        });

        dialogNoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        Window window = dialog.getWindow();
        window.setLayout(RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

    }

    //custom dialog for exit
    public void customExitDialog(Activity mContext,OnDialogListener onDialogListener) {
        dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.custom_exit_logout_dialog);
        dialog.setCancelable(false);

        TextView dialogYesBtn = dialog.findViewById(R.id.yesBtn);
        TextView dialogNoBtn = dialog.findViewById(R.id.noBtn);
        TextView descriptionTV = dialog.findViewById(R.id.descriptionTV);
        TextView titleTV = dialog.findViewById(R.id.titleTV);

        titleTV.setText(R.string.exit_title);
        descriptionTV.setText(R.string.exit_description);
        dialogYesBtn.setText(R.string.txtYes);
        dialogNoBtn.setText(R.string.txtNo);

        dialogYesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.finishAffinity();
            }
        });


        dialogNoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        Window window = dialog.getWindow();
        window.setLayout(RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }

    public void onDialogDismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

}
