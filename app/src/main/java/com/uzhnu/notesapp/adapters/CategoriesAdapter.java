package com.uzhnu.notesapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uzhnu.notesapp.databinding.ItemCategoryBinding;
import com.uzhnu.notesapp.models.CategoryModel;

import java.util.List;
import java.util.function.Function;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder> {
    private List<CategoryModel> categories;

    private Function<String, Void> showCategoryCallback;

    public CategoriesAdapter(List<CategoryModel> categoryModels,
                             Function<String, Void> showCategoryCallback) {
        this.categories = categoryModels;
        this.showCategoryCallback = showCategoryCallback;
    }

    @NonNull
    @Override
    public CategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoriesViewHolder(
                ItemCategoryBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesViewHolder holder, int position) {
        holder.setData(categories.get(position));

        holder.itemView.setOnClickListener(view -> {
            showCategoryCallback.apply(categories.get(position).getName());
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class CategoriesViewHolder extends RecyclerView.ViewHolder {
        private ItemCategoryBinding binding;

        public CategoriesViewHolder(@NonNull ItemCategoryBinding itemCategoryBinding) {
            super(itemCategoryBinding.getRoot());
            binding = itemCategoryBinding;
        }

        private void setData(@NonNull CategoryModel categoryModel) {
            binding.textViewCategoryName.setText(categoryModel.getName());
        }
    }
}
