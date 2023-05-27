package com.uzhnu.notesapp.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.databinding.ItemFolderBinding;
import com.uzhnu.notesapp.dialogs.AddFolderDialog;
import com.uzhnu.notesapp.dialogs.EditFolderDialog;
import com.uzhnu.notesapp.events.SelectFolderEvent;
import com.uzhnu.notesapp.models.FolderModel;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;
import com.uzhnu.notesapp.utils.PreferencesManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

public class FoldersAdapter extends RecyclerView.Adapter<FoldersAdapter.FoldersViewHolder> {
    private static final int SPECIAL = 0;
    private static final int REGULAR = 1;
    private static final int ADD = 2;

    private FragmentActivity activity;

    private List<FolderModel> folderModels;

    private FoldersViewHolder currentFolderHolder;

    public static class FoldersViewHolder extends RecyclerView.ViewHolder {
        private ItemFolderBinding binding;

        public FoldersViewHolder(@NonNull ItemFolderBinding itemFolderBinding) {
            super(itemFolderBinding.getRoot());
            binding = itemFolderBinding;
        }

        private void setData(@NonNull FolderModel folderModel) {
            binding.textViewFolderName.setText(folderModel.getName());
        }

        @SuppressLint("SetTextI18n")
        private void chooseStyle(int type) {
            switch (type) {
                case SPECIAL:
                    binding.textViewFolderName.setText(Constants.KEY_COLLECTION_FOLDER_DEFAULT);
                    binding.textViewFolderName.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_outline_folder_special_24, 0, 0, 0);
                    break;
                case REGULAR:
                    binding.textViewFolderName.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_outline_folder_24, 0, 0, 0);
                    break;
                case ADD:
                    binding.textViewFolderName.setText("Add folder");
                    binding.textViewFolderName.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_outline_create_new_folder_24, 0, 0, 0);
                    break;
            }
        }
    }

    public FoldersAdapter(FragmentActivity activity, List<FolderModel> categoryModels) {
        this.activity = activity;
        this.folderModels = categoryModels;
    }

    @NonNull
    @Override
    public FoldersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FoldersViewHolder(
                ItemFolderBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull FoldersViewHolder holder, int position) {
        int type = getItemViewType(position);
        holder.chooseStyle(type);
        if (type == ADD) {
            AddFolderDialog addDialog = new AddFolderDialog(this, folderModels);

            holder.itemView.setOnClickListener(view -> {
                addDialog.show(activity.getSupportFragmentManager(), "Add folder dialog");
            });
        } else {
            FolderModel folder = folderModels.get(position);
            holder.setData(folder);

            String currentFolderName = (String) PreferencesManager.getInstance().get(Constants.KEY_CURRENT_FOLDER);
            if (folder.getName().equals(currentFolderName)) {
                setCurrentFolderHolder(holder);
            }

            holder.itemView.setOnClickListener(view -> {
                SelectFolderEvent event = new SelectFolderEvent(folder, holder);
                EventBus.getDefault().post(event);
            });

            if (type == SPECIAL) {
                return;
            }

            EditFolderDialog editDialog =
                    new EditFolderDialog(this, folderModels, holder, folder);

            holder.itemView.setOnLongClickListener(view -> {
                editDialog.show(activity.getSupportFragmentManager(), "Edit folder dialog");
                return false;
            });
        }
    }

    @Override
    public int getItemCount() {
        return 1 + folderModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == folderModels.size()) {
            return ADD;
        } else if (position == 0) {
            return SPECIAL;
        } else {
            return REGULAR;
        }
    }

    private void setCurrentFolderHolder(FoldersViewHolder holder) {
        if (this.currentFolderHolder != null) {
            removeSelection(currentFolderHolder);
        }
        addSelection(holder);
        currentFolderHolder = holder;
    }

    private void addSelection(@NonNull FoldersViewHolder holder) {
        holder.binding.getRoot()
                .setBackgroundColor(ContextCompat.getColor(
                                holder.binding.getRoot().getContext(),
                                R.color.md_grey_300
                        )
                );
    }

    private void removeSelection(@NonNull FoldersViewHolder holder) {
        holder.binding.getRoot().setBackgroundResource(R.drawable.ripple_effect);
    }

    @Subscribe
    public void onSelectFolderEvent(@NonNull SelectFolderEvent event) {
        FoldersViewHolder holder = event.getHolder();
        setCurrentFolderHolder(holder);
        currentFolderHolder = holder;
    }
}
