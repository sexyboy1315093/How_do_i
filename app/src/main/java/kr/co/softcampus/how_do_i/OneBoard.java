package kr.co.softcampus.how_do_i;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OneBoard {
    private String title;
    private String content;
    private String imageUrl;
    private String publisher;
    private Date createAt;
    private String board_id;
    private String file_name;

    public OneBoard(String title, String content, String imageUrl, String publisher, Date createAt, String board_id, String file_name) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.publisher = publisher;
        this.createAt = createAt;
        this.board_id = board_id;
        this.file_name = file_name;
    }

    public OneBoard(){

    }


    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getBoard_id() {
        return board_id;
    }

    public void setBoard_id(String board_id) {
        this.board_id = board_id;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
