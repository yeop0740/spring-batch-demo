package com.heeyeop.springbatch_demo2.job.ch09;

import lombok.Getter;

@Getter
public class ClassInformation {
    private final String teacherName;
    private final int studentCount;

    public ClassInformation(String teacherName, int studentCount) {
        this.teacherName = teacherName;
        this.studentCount = studentCount;
    }
}
