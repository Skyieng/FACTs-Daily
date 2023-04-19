package my.edu.utar.FACTsDaily;
public class Task {
    private String text;
    private boolean completed;
    private String id;

    public Task() {
    }

    public Task(String text, boolean completed, String id) {
        this.text = text;
        this.completed = completed;
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
