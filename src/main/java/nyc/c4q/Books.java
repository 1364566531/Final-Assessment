package nyc.c4q;

/**
 * Created by sufeizhao on 8/30/15.
 */
public class Books {

    private int id, publishyear;
    private String title, author, isbn, isbn13, publisher;

    public Books(String author, int id, String isbn13, String isbn, String publisher, int publishyear, String title) {
        this.author = author;
        this.id = id;
        this.isbn13 = isbn13;
        this.isbn = isbn;
        this.publisher = publisher;
        this.publishyear = publishyear;
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIsbn13() {
        return isbn13;
    }

    public void setIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getPublishyear() {
        return publishyear;
    }

    public void setPublishyear(int publishyear) {
        this.publishyear = publishyear;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
