package com.zzj.jdbctool.entity;

import com.zzj.jdbctool.annotation.Column;
import com.zzj.jdbctool.annotation.Id;
import com.zzj.jdbctool.annotation.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 学生表：id，name，major，age，enrollDate(入学时间)，graduated(是否毕业)，tuition(学费)。
 */
@Table("student")
public class Student {
    @Id
    @Column("id")
    private String id;

    @Column("name")
    private String name;

    @Column("major")
    private String major;

    @Column("age")
    private Integer age;

    @Column("enroll_date")
    private LocalDate enrollDate;

    @Column("graduated")
    private Boolean graduated;

    @Column("tuition")
    private BigDecimal tuition;

    public Student() {
    }

    public Student(String id, String name, String major, Integer age, LocalDate enrollDate, Boolean graduated, BigDecimal tuition) {
        this.id = id;
        this.name = name;
        this.major = major;
        this.age = age;
        this.enrollDate = enrollDate;
        this.graduated = graduated;
        this.tuition = tuition;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocalDate getEnrollDate() {
        return enrollDate;
    }

    public void setEnrollDate(LocalDate enrollDate) {
        this.enrollDate = enrollDate;
    }

    public Boolean getGraduated() {
        return graduated;
    }

    public void setGraduated(Boolean graduated) {
        this.graduated = graduated;
    }

    public BigDecimal getTuition() {
        return tuition;
    }

    public void setTuition(BigDecimal tuition) {
        this.tuition = tuition;
    }

    @Override
    public String toString() {
        return "Student{id='" + id + "', name='" + name + "', major='" + major
                + "', age=" + age + ", enrollDate=" + enrollDate
                + ", graduated=" + graduated + ", tuition=" + tuition + "}";
    }
}
