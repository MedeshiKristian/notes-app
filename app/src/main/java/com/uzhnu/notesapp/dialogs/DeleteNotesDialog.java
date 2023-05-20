package com.uzhnu.notesapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.uzhnu.notesapp.R;

public class DeleteNotesDialog extends DialogFragment {
    public interface DeleteNotesListener {
        void onDialogPositiveClick(@NonNull DialogFragment dialog);

        void onDialogCancelClick(@NonNull DialogFragment dialog);
    }

    public DeleteNotesDialog() {
    }

    public DeleteNotesDialog(DeleteNotesListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(R.string.dialog_delete_notes)
                .setPositiveButton(R.string.string_ok, (dialog, id) -> {
                    if (listener != null) {
                        listener.onDialogPositiveClick(DeleteNotesDialog.this);
                    }
                })
                .setNeutralButton(R.string.string_cancel, (dialog, id) -> {
                    if (listener != null) {
                        listener.onDialogCancelClick(DeleteNotesDialog.this);
                    }
                });
        return builder.create();
    }

    private DeleteNotesListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (listener != null) return;
        try {
            listener = (DeleteNotesListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity().toString()
                    + " must implement DeleteNotesListener");
        }
    }
}
