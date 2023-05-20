package com.uzhnu.notesapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.uzhnu.notesapp.utils.AndroidUtil;
import com.uzhnu.notesapp.utils.Constants;
import com.uzhnu.notesapp.utils.FirebaseUtil;
import com.uzhnu.notesapp.utils.PreferencesManager;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
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

    private final Set<Integer> mSelectedPositions;

    public NotesAdapter(List<NoteModel> noteModels, LinearLayoutManager layoutManager, Context context) {
        this.noteModels = noteModels;
        this.context = context;
        this.layoutManager = layoutManager;
        mSelectedPositions = new HashSet<>();
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
            mSelectedPositions.add(position);
        } else {
            holder.removeSelection();
            mSelectedPositions.remove(position);
            if (getCountSelectedNotes() == 0) {
                EventBus.getDefault().post(new MultiSelectEvent(false));
            }
        }
        EventBus.getDefault().post(new SelectNoteEvent());
    }

    private boolean isSelected(int position) {
        return mSelectedPositions.contains(position);
    }

    public int getCountSelectedNotes() {
        return mSelectedPositions.size();
    }

    public boolean isMultiSelect() {
        return !mSelectedPositions.isEmpty();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDataSet(List<NoteModel> filteredNotes) {
        noteModels = filteredNotes;
        layoutManager.removeAllViews();
        notifyDataSetChanged();
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
            mSelectedPositions.clear();
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
                layoutManager.removeViewAt(i);
                noteModels.remove(i);
                notifyItemRemoved(i);
            }
        }
        mSelectedPositions.clear();
        EventBus.getDefault().post(new MultiSelectEvent(false));
    }

    public static class NotesViewHolder extends RecyclerView.ViewHolder {
        private final ItemNoteBinding binding;

        public NotesViewHolder(@NonNull ItemNoteBinding itemNoteBinding, boolean selected) {
            super(itemNoteBinding.getRoot());
            binding = itemNoteBinding;
            if (selected) {
                addSelection();
            } else {
                removeSelection();
            }
        }

        private void bind(@NonNull NoteModel noteModel) {
//            Log.i(Constants.TAG, "bind");
            binding.textViewNoteTitle.setText(
                    StringUtils.abbreviate(
                            AndroidUtil.getPlainTextFromHtmlp(noteModel.getText()), TEXT_LIMIT
                    )
            );
            SimpleDateFormat simpleDateFormat
                    = new SimpleDateFormat("MMMM/dd/yyyy - HH:mm:ss", Locale.getDefault());
            binding.textViewLastEdited.setText(simpleDateFormat.format(noteModel.getLastEdited()));
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
                            R.drawable.white_rounded_corners_background
                    )
            );
        }
    }
}