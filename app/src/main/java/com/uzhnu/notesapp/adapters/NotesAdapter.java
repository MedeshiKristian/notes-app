package com.uzhnu.notesapp.adapters;

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
import com.uzhnu.notesapp.events.MultiSelectEvent;
import com.uzhnu.notesapp.events.SelectNoteEvent;
import com.uzhnu.notesapp.models.NoteModel;
import com.uzhnu.notesapp.utils.AndroidUtil;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;
import com.uzhnu.notesapp.utils.PreferencesManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {
    private static final int TEXT_LIMIT = 45;

    private final Context context;

    private final List<NoteModel> noteModels;

    private RecyclerView recyclerView;

    private SparseBooleanArray mSelectedPositions;

    public NotesAdapter(List<NoteModel> noteModels, Context context) {
        this.noteModels = noteModels;
        this.context = context;
        mSelectedPositions = new SparseBooleanArray();
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
        holder.setIsRecyclable(false);

        holder.bind(noteModels.get(position));

        holder.itemView.setOnClickListener(view -> {
            if (isMultiSelect()) {
                view.performLongClick();
            } else {
                Intent intent = new Intent(context, EditNoteActivity.class);
                PreferencesManager.getInstance().put(Constants.KEY_NOTE, noteModels.get(position));
                PreferencesManager.getInstance().put(Constants.KEY_POSITION, position);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(view -> {
            toggleSelection(holder, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return noteModels.size();
    }

    private void toggleSelection(@NonNull NotesViewHolder holder, int position) {
        if (isSelected(position)) {
            removeSelection(holder, position);
        } else {
            addSelection(holder, position);
        }
        EventBus.getDefault().post(new SelectNoteEvent());
    }

    private boolean isSelected(int position) {
        return Boolean.TRUE.equals(mSelectedPositions.get(position));
    }

    public int getCountSelectedNotes() {
        return mSelectedPositions.size();
    }

    public boolean isMultiSelect() {
        return mSelectedPositions.size() != 0;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Subscribe
    public void onMultiSelect(@NonNull MultiSelectEvent event) {
        boolean show = event.isShow();
        if (!show) {
            for (int i = getItemCount() - 1; i >= 0; i--) {
                if (isSelected(i)) {
                    removeSelectionAt(i);
                }
            }
        }
        assert isMultiSelect() == show;
    }

    public void deleteAllSelectedNotes() {
        for (int i = getItemCount() - 1; i >= 0; --i) {
            if (isSelected(i)) {
                int finalI = i;
                FirebaseUtil.deleteUserNote(noteModels.get(i))
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                noteModels.remove(finalI);
                                notifyItemRemoved(finalI);
                            }
                        });
            }
        }
    }

    private void addSelection(@NonNull NotesViewHolder holder, int position) {
        if (getCountSelectedNotes() == 0) {
            EventBus.getDefault().post(new MultiSelectEvent(true));
        }
        ItemNoteBinding binding = holder.binding;
        binding.layoutNote
                .setBackgroundColor(ContextCompat.getColor(
                                binding.layoutNote.getContext(),
                                R.color.md_grey_200
                        )
                );
        binding.imageViewSelected.setVisibility(View.VISIBLE);
        mSelectedPositions.put(position, true);
    }

    private void removeSelection(@NonNull NotesViewHolder holder, int position) {
        ItemNoteBinding binding = holder.binding;
        binding.imageViewSelected.setVisibility(View.GONE);
        binding.layoutNote.setBackground(ContextCompat.getDrawable(
                        binding.layoutNote.getContext(),
                        R.drawable.white_rounded_corners_background
                )
        );
        mSelectedPositions.delete(position);
        if (getCountSelectedNotes() == 0) {
            EventBus.getDefault().post(new MultiSelectEvent(false));
        }
    }

    private void removeSelectionAt(int position) {
        NotesViewHolder holder =
                (NotesViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        assert holder != null;
        removeSelection(holder, position);
    }

    public static class NotesViewHolder extends RecyclerView.ViewHolder {
        private final ItemNoteBinding binding;

        public NotesViewHolder(@NonNull ItemNoteBinding itemNoteBinding) {
            super(itemNoteBinding.getRoot());
            binding = itemNoteBinding;
            Log.i(Constants.TAG, "construct");
        }

        private void bind(@NonNull NoteModel noteModel) {
            binding.textViewNoteTitle.setText(AndroidUtil.getPlainTextFromHtmlp(noteModel.getText()));
            SimpleDateFormat simpleDateFormat
                    = new SimpleDateFormat("MMMM/dd/yyyy - HH:mm:ss", Locale.getDefault());
            binding.textViewLastEdited.setText(simpleDateFormat.format(noteModel.getLastEdited()));
        }
    }
}