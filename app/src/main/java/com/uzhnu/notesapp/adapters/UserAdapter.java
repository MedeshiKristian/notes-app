package com.uzhnu.notesapp.adapters;

import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uzhnu.notesapp.databinding.ItemUserBinding;
import com.uzhnu.notesapp.models.FolderModel;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utilities.AndroidUtil;
import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.firebase.AuthUtil;
import com.uzhnu.notesapp.utilities.firebase.StoreUtil;
import com.uzhnu.notesapp.utilities.ImageUtil;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<UserModel> userModels;

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserBinding binding;

        public UserViewHolder(@NonNull ItemUserBinding itemUserBinding) {
            super(itemUserBinding.getRoot());
            binding = itemUserBinding;
        }

        private void bind(@NonNull UserModel userModel) {
            binding.imageViewUser.setImageBitmap(ImageUtil.decodeImage(userModel.getImage()));
            binding.textViewUsername.setText(userModel.getUsername());
            binding.textViewPhoneNumber.setText(
                    PhoneNumberUtils.formatNumber(
                            userModel.getPhoneNumber(),
                            Locale.getDefault().getCountry()
                    )
            );
        }

        private void setIcon(boolean isEditor) {
            if (isEditor) {
                binding.imageViewAdd.setVisibility(View.GONE);
                binding.imageViewRemove.setVisibility(View.VISIBLE);
            } else {
                binding.imageViewRemove.setVisibility(View.GONE);
                binding.imageViewAdd.setVisibility(View.VISIBLE);
            }
        }
    }

    public UserAdapter(List<UserModel> userModels) {
        this.userModels = userModels;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                ItemUserBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        FolderModel currentFolder = StoreUtil.getCurrentFolder();
        UserModel userModel = userModels.get(position);
        holder.bind(userModel);

        StoreUtil.getUser(currentFolder.getCreatedBy())
                .collection(currentFolder.getCollectionName())
                .document(Constants.KEY_EDITORS).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Object editor = documentSnapshot.get(userModel.getId());
                    setListeners(holder, currentFolder, userModel, editor != null);
                });
    }

    private void setListeners(@NonNull UserViewHolder holder,
                              FolderModel currentFolder,
                              @NonNull UserModel userModel,
                              boolean isEditor) {
        if (isEditor) {
            holder.itemView.setOnClickListener(view -> {
                StoreUtil.deleteFolderEditor(currentFolder, userModel)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                AndroidUtil.showToast(view.getContext(), "Successfully took access from user");
                                setListeners(holder, currentFolder, userModel, !isEditor);
                            } else {
                                AndroidUtil.showToast(view.getContext(), "Failed to take access from user");
                            }
                        });
            });
        } else {
            holder.itemView.setOnClickListener(view -> {
                StoreUtil.updateFolderEditors(currentFolder, userModel)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                AndroidUtil.showToast(view.getContext(), "Successfully gave access to folder");
                                setListeners(holder, currentFolder, userModel, !isEditor);
                            } else {
                                AndroidUtil.showToast(view.getContext(), "Failed to access folder");
                            }
                        });
            });
        }
        holder.setIcon(isEditor);
    }

    @Override
    public int getItemCount() {
        return userModels.size();
    }
}
