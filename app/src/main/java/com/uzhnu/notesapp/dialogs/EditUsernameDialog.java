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
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utils.FirebaseUtil;

public class EditUsernameDialog extends DialogFragment {
    private EditUsernameListener listener;

    public interface EditUsernameListener {
        void onDialogPositiveClick(@NonNull DialogFragment dialog, String newUsername);

        void onDialogCancelClick(@NonNull DialogFragment dialog);
    }

    public EditUsernameDialog() {
    }

    public EditUsernameDialog(EditUsernameListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        DialogRenameBinding binding = DialogRenameBinding.inflate(getLayoutInflater());
        FirebaseUtil.getCurrentUserDetails().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserModel userModel = task.getResult().toObject(UserModel.class);
                        assert userModel != null;
                        binding.editTextRename.setText(userModel.getUsername());
                    }
                });

        builder.setView(binding.getRoot())
                .setTitle(R.string.dialog_edit_folder)
                .setPositiveButton(R.string.string_rename, (dialog, id) -> {
                    if (listener != null) {
                        listener.onDialogPositiveClick(EditUsernameDialog.this,
                                binding.editTextRename.getText().toString());
                    }
                })
                .setNeutralButton(R.string.string_cancel, (dialog, id) -> {
                    if (listener != null) {
                        listener.onDialogCancelClick(EditUsernameDialog.this);
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (listener == null) {
            try {
                listener = (EditUsernameListener) context;
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }
}
