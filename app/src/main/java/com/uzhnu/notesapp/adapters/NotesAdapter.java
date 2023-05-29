package com.uzhnu.notesapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.activities.EditNoteActivity;
import com.uzhnu.notesapp.databinding.ItemNoteBinding;
import com.uzhnu.notesapp.events.MultiSelectEvent;
import com.uzhnu.notesapp.events.SelectNoteEvent;
import com.uzhnu.notesapp.models.NoteModel;
import com.uzhnu.notesapp.models.UserModel;
import com.uzhnu.notesapp.utils.AndroidUtil;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;
import com.uzhnu.notesapp.utils.PreferencesManager;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {
    private static final int TEXT_LIMIT = 45;
    private final Context context;
    private List<NoteModel> noteModels;
    private RecyclerView recyclerView;
    private final LinearLayoutManager layoutManager;
    private final Set<Integer> selectedPositions;

    public static class NotesViewHolder extends RecyclerView.ViewHolder {
        private static final String DATE_FORMAT = "MMMM/dd/yyyy - HH:mm:ss";
        private final ItemNoteBinding binding;
        private static final String currentUserId = FirebaseUtil.getCurrentUserId();

        public NotesViewHolder(@NonNull ItemNoteBinding itemNoteBinding, boolean selected) {
            super(itemNoteBinding.getRoot());
            binding = itemNoteBinding;
            if (selected) {
                addSelection();
            } else {
                removeSelection();
            }
        }

        @SuppressLint("SetTextI18n")
        private void bind(@NonNull NoteModel noteModel) {
            binding.textViewNoteTitle.setText(
                    StringUtils.abbreviate(
                            AndroidUtil.getPlainTextFromHtmlp(noteModel.getText()), TEXT_LIMIT
                    )
            );
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            if (noteModel.getLastEditedBy().equals(currentUserId)) {
                binding.textViewMetaData
                        .setText(simpleDateFormat.format(noteModel.getLastEdited()) + " by You");
            } else {
                FirebaseUtil.getUserName(noteModel.getLastEditedBy()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserModel userModel = task.getResult().toObject(UserModel.class);
                        if (userModel != null) {
                            String userName = " by " + userModel.getUsername();
                            binding.textViewMetaData
                                    .setText(simpleDateFormat.format(noteModel.getLastEdited()) + userName);
                        }
                    } else {
                        Log.e(Constants.TAG, "Failed to load username");
                    }
                });
            }
            if (noteModel.isPined()) {
                binding.imageViewPinned.setVisibility(View.VISIBLE);
            }
        }

        private void addSelection() {
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
                            R.drawable.ripple_effect_grey_rounded_courners_background
                    )
            );
        }
    }

    public NotesAdapter(List<NoteModel> noteModels, LinearLayoutManager layoutManager, Context context) {
        this.noteModels = noteModels;
        this.context = context;
        this.layoutManager = layoutManager;
        selectedPositions = new HashSet<>();
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        Log.i(Constants.TAG, viewType + " " + isSelected(viewType));
        return new NotesViewHolder(
                ItemNoteBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                ),
                isSelected(viewType)
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
                PreferencesManager.getInstance().put(Constants.KEY_NOTE, noteModels.get(position));
//                PreferencesManager.getInstance().put(Constants.KEY_POSITION, position);
                Intent intent = new Intent(context, EditNoteActivity.class);
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

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void toggleSelection(@NonNull NotesViewHolder holder, int position) {
        if (!isSelected(position)) {
            if (getCountSelectedNotes() == 0) {
                EventBus.getDefault().post(new MultiSelectEvent(true));
            }
            holder.addSelection();
            selectedPositions.add(position);
        } else {
            holder.removeSelection();
            selectedPositions.remove(position);
            if (getCountSelectedNotes() == 0) {
                EventBus.getDefault().post(new MultiSelectEvent(false));
            }
        }
        EventBus.getDefault().post(new SelectNoteEvent());
    }

    private boolean isSelected(int position) {
        return selectedPositions.contains(position);
    }

    public int getCountSelectedNotes() {
        return selectedPositions.size();
    }

    public boolean isMultiSelect() {
        return !selectedPositions.isEmpty();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDataSet(List<NoteModel> filteredNotes) {
        noteModels = filteredNotes;
        layoutManager.removeAllViews();
        notifyDataSetChanged();
    }

    public List<NoteModel> getDataSet() {
        return this.noteModels;
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
//            Log.i(Constants.TAG, "multiselect off " + mSelectedPositions.size());
            final int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
            final int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
            for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition; ++i) {
                if (isSelected(i)) {
                    NotesViewHolder holder = (NotesViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                    assert holder != null;
                    holder.removeSelection();
                }
            }
            selectedPositions.clear();
        }
    }

    public void deleteAllSelectedNotes() {
        recyclerView.getRecycledViewPool().clear();
        for (int i = noteModels.size() - 1; i >= 0; i--) {
            if (isSelected(i)) {
                FirebaseUtil.deleteUserNote(noteModels.get(i));
            }
        }
        for (int i = noteModels.size() - 1; i >= 0; i--) {
            if (isSelected(i)) {
                remove(i);
            }
        }
        selectedPositions.clear();
        EventBus.getDefault().post(new MultiSelectEvent(false));
    }

    public void togglePinOnSelectedNotes() {
        recyclerView.getRecycledViewPool().clear();
        for (int i = noteModels.size() - 1; i >= 0; i--) {
            if (isSelected(i)) {
                noteModels.get(i).togglePin();
            }
        }
        for (int i = noteModels.size() - 1; i >= 0; i--) {
            if (isSelected(i)) {
                FirebaseUtil.updateNote(noteModels.get(i));
            }
        }
        selectedPositions.clear();
        EventBus.getDefault().post(new MultiSelectEvent(false));
    }

    public void remove(int position) {
        layoutManager.removeViewAt(position);
        noteModels.remove(position);
        notifyItemRemoved(position);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void restore(@NonNull NoteModel note, int position) {
        noteModels.add(position, note);
        notifyDataSetChanged();
    }

    public void filter(String s, @NonNull List<NoteModel> allNotes) {
        s = s.toLowerCase();
        ArrayList<NoteModel> filteredNotes = new ArrayList<>();
        for (NoteModel note : allNotes) {
            if (s.isEmpty() || AndroidUtil.getPlainTextFromHtmlp(note.getText()).toLowerCase().contains(s)) {
                filteredNotes.add(note);
            }
        }
        setDataSet(filteredNotes);
    }
}