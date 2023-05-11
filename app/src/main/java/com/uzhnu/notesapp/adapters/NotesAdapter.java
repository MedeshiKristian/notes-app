package com.uzhnu.notesapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.activities.EditNoteActivity;
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

    private Context context;

    private final List<Note> notes;

    private Function<Boolean, Void> setDeleteActionVisibleInMainCallback;
    private Boolean isDeleteActionVisible;
    private SparseBooleanArray mSelectedItems;
    private final Function<Void, Void> updateToolBar;
    private RecyclerView recyclerView;

    public NotesAdapter(List<Note> notes, Function<Void, Void> updateToolBar, Context context) {
        this.notes = notes;
        this.updateToolBar = updateToolBar;
        isDeleteActionVisible = false;
        this.context = context;
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
                Intent intent = new Intent(context, EditNoteActivity.class);
                intent.putExtra(Constants.KEY_NOTE, notes.get(position));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(view -> {
            setDeleteActionVisibleInMainCallback.apply(true);
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

    public void removeAllSelections(boolean clearSelections) {
        if (recyclerView != null) {
            for (int i = 0; i < notes.size(); i++) {
                if (mSelectedItems.get(i)) {
                    removeSelectionAt(i);
                }
            }
            if (clearSelections) {
                mSelectedItems.clear();
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void deleteAllSelectedItems() {
        if (recyclerView != null) {
            setDeleteActionVisibleInMainCallback.apply(false);
            removeAllSelections(false);
            Log.i(Constants.TAG, Integer.toString(notes.size()));
            for (int i = notes.size() - 1; i >= 0; i--) {
                if (mSelectedItems.get(i)) {
                    int finalI = i;
                    FirebaseUtil.deleteUserNote(notes.get(i))
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
            mSelectedItems.delete(position);
            if (mSelectedItems.size() == 0) {
                setDeleteActionVisibleInMainCallback.apply(false);
            }
            holder.removeSelection();
        }
        updateToolBar.apply(null);
    }

    public int getCountSelectedItems() {
        return mSelectedItems.size();
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public Boolean getDeleteActionVisible() {
        return isDeleteActionVisible;
    }

    public void setIsDeleteActionVisible(Boolean deleteActionVisible) {
        isDeleteActionVisible = deleteActionVisible;
    }

    public void setSetDeleteActionVisibleInMainCallback(Function<Boolean, Void> setDeleteActionVisibleInMainCallback) {
        this.setDeleteActionVisibleInMainCallback = setDeleteActionVisibleInMainCallback;
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