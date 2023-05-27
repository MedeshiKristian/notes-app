package com.uzhnu.notesapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.adapters.FoldersAdapter;
import com.uzhnu.notesapp.databinding.DialogAddFolderBinding;
import com.uzhnu.notesapp.models.FolderModel;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;

import java.util.List;

public class AddFolderDialog extends DialogFragment {
    private AddFolderListener listener;

    public interface AddFolderListener {
        void onDialogPositiveClick(@NonNull DialogFragment dialog, String folderName);

        void onDialogCancelClick(@NonNull DialogFragment dialog);
    }

    public AddFolderDialog() {
    }

    public AddFolderDialog(AddFolderListener listener) {
        this.listener = listener;
    }

    public AddFolderDialog(FoldersAdapter adapter, List<FolderModel> folderModels) {
        this.listener = new AddFolderListener() {
            @Override
            public void onDialogPositiveClick(@NonNull DialogFragment dialog, String folderName) {
                OnCompleteListener<QuerySnapshot> onCompleteListener = task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        boolean ok = true;
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            FolderModel folder = FirebaseUtil
                                    .getFolderFromDocument(queryDocumentSnapshot);
                            if (folder.getName().equals(folderName)) {
                                ok = false;
                            }
                        }
                        if (ok) {
                            FolderModel folder = new FolderModel(folderName);
                            FirebaseUtil.addFolder(folder)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            folder.setDocumentId(task1.getResult().getId());
                                            folderModels.add(folder);
                                            adapter.notifyItemInserted(folderModels.size() - 1);
                                        }
                                    });
                        }
                    }
                };
                FirebaseUtil.getFolders().get().addOnCompleteListener(onCompleteListener);
            }

            @Override
            public void onDialogCancelClick(@NonNull DialogFragment dialog) {
                assert dialog.getDialog() != null;
                dialog.getDialog().cancel();
            }
        };
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        DialogAddFolderBinding binding = DialogAddFolderBinding.inflate(getLayoutInflater());

        builder.setView(binding.getRoot())
                .setTitle(R.string.dialog_add_folder)
                .setPositiveButton(R.string.string_add, (dialog, id) -> {
                    if (listener != null) {
                        listener.onDialogPositiveClick(AddFolderDialog.this,
                                binding.editTextFolderName.getText().toString());
                    }
                })
                .setNeutralButton(R.string.string_cancel, (dialog, id) -> {
                    if (listener != null) {
                        listener.onDialogCancelClick(AddFolderDialog.this);
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (listener != null) return;
        try {
            listener = (AddFolderListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity()
                    + " must implement DeleteNotesListener");
        }
    }
}
