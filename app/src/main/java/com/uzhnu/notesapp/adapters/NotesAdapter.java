package com.uzhnu.notesapp.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.databinding.ItemNotesBinding;
import com.uzhnu.notesapp.models.Note;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {
    private static final int TEXT_LIMIT = 45;

    private final List<Note> notes;

    private Function<Boolean, Void> setDeleteActionVisible;
    private Boolean isDeleteActionVisible;
    private SparseBooleanArray mSelectedItems;

    private RecyclerView recyclerView;

    public NotesAdapter(List<Note> notes) {
        this.notes = notes;
        isDeleteActionVisible = false;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotesViewHolder(
                ItemNotesBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        holder.setData(notes.get(position));

        mSelectedItems = new SparseBooleanArray();

        holder.itemView.setOnClickListener(view -> {
            if (isDeleteActionVisible) {
                // Select note to delete
                Log.i(Constants.TAG, "click listener called");
                toggleSelection(holder, position);
            } else {
                // Edit note
            }
        });

        holder.itemView.setOnLongClickListener(view -> {
            setDeleteActionVisible.apply(true);
            toggleSelection(holder, position);
            return true;
        });
    }

    private void removeSelectionAt(int position) {
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
        if (holder instanceof NotesViewHolder) {
            ((NotesViewHolder) holder).removeSelection();
        }
    }

    public void removeAllSelections(boolean clearSelectedItems) {
        if (recyclerView != null) {
            for (int i = 0; i < notes.size(); i++) {
                if (mSelectedItems.get(i)) {
                    removeSelectionAt(i);
                }
            }
            if (clearSelectedItems) {
                mSelectedItems.clear();
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void deleteAllSelectedItems() {
        if (recyclerView != null) {
            setDeleteActionVisible.apply(false);
            removeAllSelections(false);
            Log.i(Constants.TAG, Integer.toString(notes.size()));
            for (int i = notes.size() - 1; i >= 0; i--) {
                if (mSelectedItems.get(i)) {
                    int finalI = i;
                    FirebaseUtil.deleteNote(notes.get(i))
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    mSelectedItems.delete(finalI);
                                    notes.remove(finalI);
                                    notifyDataSetChanged();
                                }
                            });
                }
            }
        }
    }


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    private void toggleSelection(@NonNull NotesViewHolder holder, int position) {
        selectItem(holder, position, !mSelectedItems.get(position));
    }

    private void selectItem(@NonNull NotesViewHolder holder, int position, boolean value) {
        if (value) {
            mSelectedItems.put(position, true);
            holder.selectItem();
        } else {
            Log.i(Constants.TAG, "remove item called");
            mSelectedItems.delete(position);
            holder.removeSelection();
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public Boolean getDeleteActionVisible() {
        return isDeleteActionVisible;
    }

    public void setDeleteActionVisible(Boolean deleteActionVisible) {
        isDeleteActionVisible = deleteActionVisible;
    }

    public void setSetDeleteActionVisible(Function<Boolean, Void> setDeleteActionVisible) {
        this.setDeleteActionVisible = setDeleteActionVisible;
    }

    static class NotesViewHolder extends RecyclerView.ViewHolder {
        private ItemNotesBinding binding;

        public NotesViewHolder(@NonNull ItemNotesBinding itemNotesBinding) {
            super(itemNotesBinding.getRoot());
            binding = itemNotesBinding;
        }

        private void setData(@NonNull Note note) {
            binding.textViewNoteTitle.setText(StringUtils.abbreviate(note.getText(), TEXT_LIMIT));
            SimpleDateFormat simpleDateFormat
                    = new SimpleDateFormat("MMMM/dd/yyyy - HH:mm:ss", Locale.getDefault());
            binding.textViewLastEdited.setText(simpleDateFormat.format(note.getLastEdited()));

            setListeners();
        }

        private void selectItem() {
            binding.layoutNote
                    .setBackgroundColor(ContextCompat.getColor(
                                    binding.layoutNote.getContext(),
                                    R.color.md_grey_200
                            )
                    );
            binding.imageViewSelected.setVisibility(View.VISIBLE);
        }

        private void removeSelection() {
            binding.imageViewSelected.setVisibility(View.GONE);
            binding.layoutNote.setBackground(ContextCompat.getDrawable(
                            binding.layoutNote.getContext(),
                            R.drawable.white_rounded_corners_background
                    )
            );
        }

        private void setListeners() {
        }
    }
}