package com.uzhnu.notesapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.adapters.FoldersAdapter;
import com.uzhnu.notesapp.databinding.DialogRenameBinding;
import com.uzhnu.notesapp.events.SelectFolderEvent;
import com.uzhnu.notesapp.models.FolderModel;
import com.uzhnu.notesapp.utilities.AndroidUtil;
import com.uzhnu.notesapp.utilities.firebase.StoreUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

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

    public EditFolderDialog(FoldersAdapter adapter, List<FolderModel> folderModels,
                            @NonNull FoldersAdapter.FoldersViewHolder holder,
                            FolderModel folder) {
        this.folderModel = folder;
        this.listener = new EditFolderListener() {
            @Override
            public void onDialogPositiveClick(@NonNull DialogFragment dialog, String folderName) {
                folder.setName(folderName);
                adapter.notifyItemChanged(holder.getLayoutPosition());
//                StoreUtil.updateFolder(folder);
            }

            @Override
            public void onDialogNegativeClick(@NonNull DialogFragment dialog) {
//                StoreUtil.deleteFolder(folder);
                folderModels.remove(holder.getLayoutPosition());
                adapter.notifyItemRemoved(holder.getLayoutPosition());
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
