package com.library.entity;

public class Teacher extends User {

    /** 所属学院 */
    private String college;

    /** 职称 */
    private String title;

    @Override
    public String[] hasPermission() {
        return new String[]{"borrow", "return", "myLoans", "recommend"};
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
