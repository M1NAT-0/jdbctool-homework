package com.library.entity;

import java.time.LocalDate;

public class Admin extends User {

    /** 权限等级 */
    private Integer permissionLevel;

    /** 入职时间 */
    private LocalDate entryTime;

    @Override
    public String[] hasPermission() {
        return new String[]{"userQuery", "loanRecordQuery", "loanStats", "borrow", "return", "reviewRecommend"};
    }

    public Integer getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(Integer permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public LocalDate getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDate entryTime) {
        this.entryTime = entryTime;
    }
}
