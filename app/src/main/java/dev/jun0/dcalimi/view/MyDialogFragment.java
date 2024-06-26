package dev.jun0.dcalimi.view;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MyDialogFragment extends DialogFragment {
    private final String mStrTitle;
    private final String mStrMessage;
    private final Boolean mHasPositiveButton;

    public MyDialogFragment(String strTitle, String strMessage, Boolean hasPositiveButton){
        mStrTitle = strTitle;
        mStrMessage = strMessage;
        mHasPositiveButton = hasPositiveButton;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(requireActivity());
        materialAlertDialogBuilder.setTitle(mStrTitle);
        materialAlertDialogBuilder.setMessage(mStrMessage);

        if(mHasPositiveButton)
            materialAlertDialogBuilder.setPositiveButton(android.R.string.ok, null);

        return materialAlertDialogBuilder.create();
    }
}
