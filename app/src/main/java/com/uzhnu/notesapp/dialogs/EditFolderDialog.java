package com.uzhnu.notesapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.uzhnu.notesapp.databinding.DialogEditFolderBinding;

public class EditFolderDialog extends DialogFragment {
    private EditFolderListener listener;

    public interface EditFolderListener {
        void onDialogPositiveClick(@NonNull DialogFragment dialog, String folderName);

        void onDialogNegativeClick(@NonNull DialogFragment dialog);

        void onDialogCancelClick(@NonNull DialogFragment dialog);
    }

    public EditFolderDialog() {
    }

    public EditFolderDialog(EditFolderListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        DialogEditFolderBinding binding = DialogEditFolderBinding.inflate(getLayoutInflater());

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (listener == null) {
            try {
                listener = (EditFolderListener) context;
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }
}
