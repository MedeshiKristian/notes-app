package com.uzhnu.notesapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.databinding.DialogRenameBinding;
import com.uzhnu.notesapp.models.FolderModel;

public class EditFolderDialog extends DialogFragment {
    private EditFolderListener listener;
    private final FolderModel folderModel;

    public interface EditFolderListener {
        void onDialogPositiveClick(@NonNull DialogFragment dialog, String folderName);

        void onDialogNegativeClick(@NonNull DialogFragment dialog);

        void onDialogCancelClick(@NonNull DialogFragment dialog);
    }

    public EditFolderDialog(FolderModel folderModel) {
        this.folderModel = folderModel;
    }

    public EditFolderDialog(FolderModel folderModel, EditFolderListener listener) {
        this.folderModel = folderModel;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        DialogRenameBinding binding = DialogRenameBinding.inflate(getLayoutInflater());
        binding.editTextRename.setText(folderModel.getName());

        builder.setView(binding.getRoot())
                .setTitle(R.string.dialog_edit_folder)
                .setPositiveButton(R.string.string_rename, (dialog, id) -> {
                    if (listener != null) {
                        listener.onDialogPositiveClick(EditFolderDialog.this,
                                binding.editTextRename.getText().toString());
                    }
                })
                .setNegativeButton(R.string.string_delete, (dialog, id) -> {
                    if (listener != null) {
                        listener.onDialogNegativeClick(EditFolderDialog.this);
                    }
                })
                .setNeutralButton(R.string.string_cancel, (dialog, id) -> {
                    if (listener != null) {
                        listener.onDialogCancelClick(EditFolderDialog.this);
                    }
                });

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
