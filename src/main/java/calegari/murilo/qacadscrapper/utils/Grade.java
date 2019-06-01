package calegari.murilo.qacadscrapper.utils;

import org.threeten.bp.LocalDate;

public class Grade {

    private int acadSubjectId;

    private String gradeDescription;
    private LocalDate date;

    private float obtainedGrade;
    private float maximumGrade;
    private float weight = 1f; // By default is one

    private boolean isObtainedGradeNull = false; // By default is false

    public int getAcadSubjectId() {
        return acadSubjectId;
    }

    public void setAcadSubjectId(int acadSubjectId) {
        this.acadSubjectId = acadSubjectId;
    }

    public void setGradeDescription(String gradeDescription) {
        this.gradeDescription = gradeDescription;
    }

    public boolean isObtainedGradeNull() {
        return isObtainedGradeNull;
    }

    public void setObtainedGradeNull(boolean obtainedGradeNull) {
        isObtainedGradeNull = obtainedGradeNull;
    }

    public Grade() {
    }

    public Grade(String gradeDescription, float obtainedGrade, float maximumGrade) {
        this.gradeDescription = gradeDescription;
        this.obtainedGrade = obtainedGrade;
        this.maximumGrade = maximumGrade;
    }

    public Grade(float obtainedGrade, float maximumGrade) {
        this.obtainedGrade = obtainedGrade;
        this.maximumGrade = maximumGrade;
    }

    public boolean getIsObtainedGradeNull() {
        return isObtainedGradeNull;
    }

    public void setIsObtainedGradeNull(boolean isObtainedGradeNull) {
        this.isObtainedGradeNull = isObtainedGradeNull;
    }

    public String getGradeDescription() {
        return gradeDescription;
    }

    public void setName(String gradeName) {
        this.gradeDescription = gradeName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public float getObtainedGrade() {
        return obtainedGrade;
    }

    public void setObtainedGrade(float obtainedGrade) {
        this.obtainedGrade = obtainedGrade;
    }

    public float getMaximumGrade() {
        return maximumGrade;
    }

    public void setMaximumGrade(float maximumGrade) {
        this.maximumGrade = maximumGrade;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}