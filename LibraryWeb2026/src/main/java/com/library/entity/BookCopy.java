package com.library.entity;

public class BookCopy extends Base {

    /** 属于哪种书 */
    private Book book;

    /** 借阅状态, true为已借出,false为未借阅 */
    private boolean isBorrowed;

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public boolean isBorrowed() {
        return isBorrowed;
    }

    public void setBorrowed(boolean borrowed) {
        isBorrowed = borrowed;
    }
}
