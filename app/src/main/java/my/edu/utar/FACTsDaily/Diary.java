package my.edu.utar.FACTsDaily;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Diary {
    private String diaryTitle;
    private String diaryDetail;
    private Date selDate;
    private String emoji;
    private String imageUpload;

    public Diary(String diaryTitle, String diaryDetail, Date selDate, String emoji, String imageUpload){
        this.diaryTitle = diaryTitle;
        this.diaryDetail = diaryDetail;
        this.selDate = selDate;
        this.emoji = emoji;
        this.imageUpload = imageUpload;
    }

    public Diary(){}

    public String getTitle(){
        return diaryTitle;
    }

    public String getDetail(){
        return diaryDetail;
    }

    public Date getDate(){
        return selDate;
    }

    public String getEmoji(){
        return emoji;
    }

    public String getImageUpload(){
        return imageUpload;
    }

    public void setTitle(String title) {
        this.diaryTitle = title;
    }

    public void setDetail(String detail) {
        this.diaryDetail = detail;
    }

    public void setDate(Date date) {
        this.selDate = date;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public void setImageUpload(String imageUpload) {
        this.imageUpload = imageUpload;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("diaryTitle", diaryTitle);
        result.put("diaryDetail", diaryDetail);
        result.put("sDate", selDate);
        result.put("emoji", emoji);
        result.put("imageUrl", imageUpload);
        return result;
    }

}
