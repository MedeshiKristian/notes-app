package com.uzhnu.notesapp.adapters;

import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uzhnu.notesapp.databinding.ItemUserBinding;
import com.uzhnu.notesapp.models.FolderModel;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utils.AndroidUtil;
import com.uzhnu.notesapp.utils.FirebaseStoreUtil;
import com.uzhnu.notesapp.utils.ImageUtil;

import java.util.List;
import java.util.Locale;

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
        FolderModel currentFolder = FirebaseStoreUtil.getCurrentFolder();
        UserModel userModel = userModels.get(position);
        holder.bind(userModel);

        if (currentFolder.getCreatedBy().equals(FirebaseStoreUtil.getCurrentUserId())) {
            holder.itemView.setOnClickListener(view -> {
                FirebaseStoreUtil.addAccessToCurrentFolder(userModel)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                AndroidUtil.showToast(view.getContext(), "User successfully given access to folder");
                            } else {
                                AndroidUtil.showToast(view.getContext(), "Failed to give access to folder");
                            }
                        });
            });
        }
    }

    @Override
    public int getItemCount() {
        return userModels.size();
    }
}
