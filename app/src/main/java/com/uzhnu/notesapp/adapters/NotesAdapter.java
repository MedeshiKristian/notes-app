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
import com.uzhnu.notesapp.databinding.ItemNoteBinding;
import com.uzhnu.notesapp.events.SelectNoteEvent;
import com.uzhnu.notesapp.models.NoteModel;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;
import com.uzhnu.notesapp.utils.PreferencesManager;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {
    private static final int TEXT_LIMIT = 45;

    private final Context context;

    private final List<NoteModel> noteModels;

    private SparseBooleanArray mSelectedNotes;
    private RecyclerView recyclerView;

    public NotesAdapter(List<NoteModel> noteModels, Context context) {
        this.noteModels = noteModels;
        this.context = context;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotesViewHolder(
                ItemNoteBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        holder.setData(noteModels.get(position));

        mSelectedNotes = new SparseBooleanArray();

        holder.itemView.setOnClickListener(view -> {
            if (isDeleteActionVisible()) {
                // Select note to delete
                view.performLongClick();
            } else {
                // Edit note
                Intent intent = new Intent(context, EditNoteActivity.class);
                PreferencesManager.getInstance().put(Constants.KEY_NOTE, noteModels.get(position));
                PreferencesManager.getInstance().put(Constants.KEY_POSITION, position);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(view -> {
            EventBus.getDefault().post(new SelectNoteEvent(getCountSelectedNotes()));
            toggleSelection(holder, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return noteModels.size();
    }

    private void removeSelectionAt(int position) {
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
        if (holder instanceof NotesViewHolder) {
            ((NotesViewHolder) holder).removeSelection();
        }
    }

    public void removeAllSelections(boolean clearSelections) {
        if (recyclerView != null) {
            for (int i = 0; i < noteModels.size(); i++) {
                if (mSelectedNotes.get(i)) {
                    removeSelectionAt(i);
                }
            }
            if (clearSelections) {
                mSelectedNotes.clear();
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void deleteAllSelectedNotes() {
        if (recyclerView != null) {
            EventBus.getDefault().post(new SelectNoteEvent(0));
            removeAllSelections(false);
            for (int i = noteModels.size() - 1; i >= 0; i--) {
                if (mSelectedNotes.get(i)) {
                    int finalI = i;
                    FirebaseUtil.deleteUserNote(noteModels.get(i))
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    mSelectedNotes.delete(finalI);
                                    noteModels.remove(finalI);
                                    notifyItemRemoved(finalI);
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
        if (mSelectedNotes.get(position)) {
            mSelectedNotes.delete(position);
            if (mSelectedNotes.size() == 0) {
                EventBus.getDefault().post(new SelectNoteEvent(0));
            }
            holder.removeSelection();
        } else {
            mSelectedNotes.put(position, true);
            holder.selectItem();
        }
        EventBus.getDefault().post(new SelectNoteEvent(getCountSelectedNotes()));
        //selectNote(holder, position, !mSelectedNotes.get(position));
    }

    public int getCountSelectedNotes() {
        return mSelectedNotes.size();
    }

    public boolean isDeleteActionVisible() {
        return getCountSelectedNotes() != 0;
    }

    public static class NotesViewHolder extends RecyclerView.ViewHolder {
        private final ItemNoteBinding binding;

        public NotesViewHolder(@NonNull ItemNoteBinding itemNoteBinding) {
            super(itemNoteBinding.getRoot());
            binding = itemNoteBinding;
        }

        private void setData(@NonNull NoteModel noteModel) {
            binding.textViewNoteTitle.setText(StringUtils.abbreviate(noteModel.getText(), TEXT_LIMIT));
            SimpleDateFormat simpleDateFormat
                    = new SimpleDateFormat("MMMM/dd/yyyy - HH:mm:ss", Locale.getDefault());
            binding.textViewLastEdited.setText(simpleDateFormat.format(noteModel.getLastEdited()));

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