package com.uzhnu.notesapp.events;

public class MultiSelectEvent {
    private boolean show;

    public MultiSelectEvent(boolean show) {
        this.show = show;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }
}
