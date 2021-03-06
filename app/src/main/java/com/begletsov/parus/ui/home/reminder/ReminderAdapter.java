package com.begletsov.parus.ui.home.reminder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.begletsov.parus.R;
import com.begletsov.parus.databinding.ReminderItemBinding;
import com.begletsov.parus.viewmodels.ReminderViewModel;
import com.begletsov.parus.viewmodels.data.models.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReminderAdapter extends ListAdapter<Reminder, ReminderAdapter.ViewHolder> {

    private boolean delete;
    private final List<Reminder> deletingReminders;
    private final OnItemClickInterface onItemClickInterface;
    private final ReminderViewModel reminderViewModel;

    ReminderAdapter(@NonNull ReminderDiffCallback diffCallback, OnItemClickInterface onItemClickInterface, ReminderViewModel reminderViewModel) {
        super(diffCallback);
        deletingReminders = new ArrayList<>();
        this.onItemClickInterface = onItemClickInterface;
        this.reminderViewModel = reminderViewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolder.from(parent);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = getItem(position);
        holder.bind(reminder);
        holder.setListeners(reminder, delete, onItemClickInterface, deletingReminders);
    }

    LiveData<Integer> delete() {
        return reminderViewModel.deleteReminders(new ArrayList<>(deletingReminders));
    }

    void setDeleting() {
        delete = true;
    }

    void setNoDeleting() {
        delete = false;
        deletingReminders.clear();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView background;
        private final TextView name;
        private final TextView time;
        private final CheckBox checkBox;
        private final Context context;

        private ViewHolder(ReminderItemBinding binding) {
            super(binding.getRoot());
            context = binding.getRoot().getContext();
            background = binding.reminderBackgroung;
            name = binding.reminderName;
            time = binding.reminderTime;
            checkBox = binding.reminderCheck;
        }

        private static ViewHolder from(@NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ReminderItemBinding binding = ReminderItemBinding.inflate(inflater, parent, false);
            return new ViewHolder(binding);
        }

        @SuppressLint("SetTextI18n")
        private void bind(Reminder reminder) {
            this.checkBox.setVisibility(View.GONE);
            this.checkBox.setClickable(false);
            this.name.setText(reminder.getName());
            if (reminder.getType() == 1) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Date d : reminder.getTimers()) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(d);
                    String t = convertDate(c.get(Calendar.HOUR_OF_DAY)) + ":" + convertDate(c.get(Calendar.MINUTE));
                    stringBuilder.append(t).append(", ");
                }
                this.time.setText(stringBuilder.toString().substring(0, stringBuilder.length()-2));
            } else if (reminder.getType() == 0) {
                Calendar s = Calendar.getInstance();
                s.setTime(reminder.getTimeStart());
                Calendar e = Calendar.getInstance();
                e.setTime(reminder.getTimeEnd());
                Calendar i = Calendar.getInstance();
                i.setTime(reminder.getTimeInterval());
                this.time.setText("С " + convertDate(s.get(Calendar.HOUR_OF_DAY)) + ":" + convertDate(s.get(Calendar.MINUTE)) +
                        " по " + convertDate(e.get(Calendar.HOUR_OF_DAY)) + ":" + convertDate(e.get(Calendar.MINUTE)) + "\n");
                if (i.get(Calendar.HOUR_OF_DAY) == 0)
                    this.time.setText(this.time.getText().toString() + "Каждые " + i.get(Calendar.MINUTE) + " минут(-ы)");
                else
                    this.time.setText(this.time.getText().toString() + "Каждые " + convertDate(i.get(Calendar.HOUR_OF_DAY)) + ":" + convertDate(i.get(Calendar.MINUTE)) + " час(-ов)");
            }
        }

        private void setListeners(Reminder reminder, boolean delete, OnItemClickInterface onItemClickInterface, List<Reminder> deletingReminders){
            this.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    deletingReminders.add(reminder);
                    this.checkBox.setVisibility(View.VISIBLE);
                    this.background.setBackground(ContextCompat.getDrawable(context, R.drawable.reminder_background));
                } else {
                    deletingReminders.remove(reminder);
                    this.checkBox.setVisibility(View.GONE);
                    this.background.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_link_location));
                }
            });
            this.background.setOnClickListener(l -> {
                if (!delete) {
                    DialogChangeReminder dialogChangeReminder;
                    if (reminder.getType() == 0) {
                        Calendar c = Calendar.getInstance();
                        c.setTime(reminder.getTimeStart());
                        StringBuilder start = new StringBuilder();
                        start.append(convertDate(c.get(Calendar.HOUR_OF_DAY))).append(":").append(convertDate(c.get(Calendar.MINUTE)));
                        c.setTime(reminder.getTimeEnd());
                        StringBuilder end = new StringBuilder();
                        end.append(convertDate(c.get(Calendar.HOUR_OF_DAY))).append(":").append(convertDate(c.get(Calendar.MINUTE)));
                        c.setTime(reminder.getTimeInterval());
                        StringBuilder interval = new StringBuilder();
                        interval.append(convertDate(c.get(Calendar.HOUR_OF_DAY))).append(":").append(convertDate(c.get(Calendar.MINUTE)));
                        dialogChangeReminder = DialogChangeReminder.newInstance(reminder.getId(), reminder.getName(), start.toString(), end.toString(), interval.toString());
                        onItemClickInterface.onDialogClick(dialogChangeReminder);
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (Date date : reminder.getTimers()) {
                            Calendar d = Calendar.getInstance();
                            d.setTime(date);
                            stringBuilder.append(convertDate(d.get(Calendar.HOUR_OF_DAY))).append(":").append(convertDate(d.get(Calendar.MINUTE))).append(" ");
                        }
                        new DialogChangeReminder();
                        dialogChangeReminder = DialogChangeReminder.newInstance(reminder.getId(), reminder.getName(), stringBuilder.toString().split(" "));
                        onItemClickInterface.onDialogClick(dialogChangeReminder);
                    }
                } else {
                    this.checkBox.setChecked(!this.checkBox.isChecked());
                    if (this.checkBox.isChecked()) {
                        onItemClickInterface.onItemClick(1);
                        this.background.setContentDescription("Напоминание " + this.name.getText().toString() + " выбрано для удаления");
                    } else {
                        onItemClickInterface.onItemClick(-1);
                        this.background.setContentDescription("Напоминание " + this.name.getText().toString() + " не выбрано для удаления");
                    }
                }
            });
            if (delete) {
                this.background.setContentDescription("Напоминание " + this.name.getText().toString() + " не выбрано для удаления");
            } else {
                this.background.setContentDescription("Напоминание " + this.name.getText().toString() + ", " + this.time.getText().toString());
            }
        }

        private static String convertDate(int input) {
            if (input >= 10) {
                return String.valueOf(input);
            } else {
                return "0" + input;
            }
        }
    }
}
