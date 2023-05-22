package com.uzhnu.notesapp.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.databinding.ItemFolderBinding;
import com.uzhnu.notesapp.events.SelectFolderEvent;
import com.uzhnu.notesapp.models.FolderModel;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.PreferencesManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

public class FoldersAdapter extends RecyclerView.Adapter<FoldersAdapter.FoldersViewHolder> {
    private static final int SPECIAL = 0;
    private static final int REGULAR = 1;
    private static final int ADD = 2;

    private List<FolderModel> folders;

    private FoldersViewHolder currentFolderHolder;

    public FoldersAdapter(List<FolderModel> categoryModels) {
        this.folders = categoryModels;
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
            holder.itemView.setOnClickListener(view -> {
                // TODO Edit folders activity
            });
        } else {
            FolderModel folder = folders.get(position);
            holder.setData(folder);

            String currentFolderName = (String) PreferencesManager.getInstance().get(Constants.KEY_CURRENT_FOLDER);
            if (folder.getName().equals(currentFolderName)) {
                setCurrentFolderHolder(holder);
            }

            holder.itemView.setOnClickListener(view -> {
                SelectFolderEvent event = new SelectFolderEvent(folder.getName(), holder);
                EventBus.getDefault().post(event);
            });
        }
    }

    @Override
    public int getItemCount() {
        return 1 + folders.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == folders.size()) {
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
        Log.i(Constants.TAG, "called event in folder adapter");
        FoldersViewHolder holder = event.getHolder();
        setCurrentFolderHolder(holder);
        currentFolderHolder = holder;
        Log.i(Constants.TAG, "called");
    }

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
                    binding.textViewFolderName.setText("Edit folders");
                    binding.textViewFolderName.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_outline_create_new_folder_24, 0, 0, 0);
                    break;
            }
        }
    }
}
