package calegari.murilo.qacadscrapper.utils;

import java.util.ArrayList;
import java.util.List;

public class Subject {

    private int id;
    private String subjectClass;
    private String name;
    private String professor;
    private String abbreviation;
    private Float obtainedGrade = 0f;
    private Float maximumGrade = 0f;

    private List<Grade> gradeList = new ArrayList<>();

    public Subject() {}

    public Subject(String name, String professor, String abbreviation) {
        this.name = name;
        this.professor = professor;
        this.abbreviation = abbreviation;
    }

    public Subject(String name, String abbreviation, Float obtainedGrade, Float maximumGrade) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.obtainedGrade = obtainedGrade;
        this.maximumGrade = maximumGrade;
    }

    public Subject(String name, Float obtainedGrade, Float maximumGrade) {
        this.name = name;
        this.obtainedGrade = obtainedGrade;
        this.maximumGrade = maximumGrade;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public void addGrade(Grade grade) {
        grade.setAcadSubjectId(this.id);
        this.gradeList.add(grade);

        if(!grade.isObtainedGradeNull()) {
            this.obtainedGrade += grade.getObtainedGrade();
            this.maximumGrade += grade.getMaximumGrade();
        }
    }

    public List<Grade> getGradeList() {
        return gradeList;
    }

    public void setGradeList(List<Grade> gradeList) {
        this.gradeList = gradeList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSubjectClass() {
        return subjectClass;
    }

    public void setSubjectClass(String subjectClass) {
        this.subjectClass = subjectClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfessor() {
        return professor;
    }

    public void setProfessor(String professor) {
        this.professor = professor;
    }

    public Float getMaximumGrade() {
        return maximumGrade;
    }

    public void setMaximumGrade(Float maximumGrade) {
        this.maximumGrade = maximumGrade;
    }

    public Float getObtainedGrade() {
        return obtainedGrade;
    }

    public void setObtainedGrade(Float obtainedGrade) {
        this.obtainedGrade = obtainedGrade;
    }
}
