package calegari.murilo.qacadscrapper;

import calegari.murilo.qacadscrapper.utils.Grade;
import calegari.murilo.qacadscrapper.utils.Subject;
import calegari.murilo.qacadscrapper.utils.User;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.security.auth.login.LoginException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class QAcadScrapper {

    private final String KEY_GENERATOR_PAGE = "/lib/rsa/gerador_chaves_rsa.asp?form=frmLogin&action=%2Fqacademico%2Flib%2Fvalidalogin%2Easp";
    private final String VALIDATE_LOGIN_PAGE = "/lib/validalogin.asp";
    private final String MAIN_PAGE = "/index.asp?t=2000";
    private final String GRADES_PAGE = "/index.asp?t=2071";
    private final String NOT_LOGGED_ERROR_TEXT = "Acesso Negado";
    private final String PAGE_CHARSET = "windows-1252"; // use document.charset in chrome developer mode to obtain it.

    private String url;

    private String keyA;
    private String keyB;

    public Map<String, String> getCookieMap() {
        return cookieMap;
    }

    public void setCookieMap(Map<String, String> cookieMap) {
        this.cookieMap = cookieMap;
    }

    private Map<String, String> cookieMap;

    private boolean isLogged = false;
    private Connection.Response gradesResponse;

    /**
     * QAcadScrapper can extract data from the Q-Acadêmico website in a simple way
     * @param url URL in the form https://[YOUR_INSTITUTION's_Q_ACADEMICO_HOST]/qacademico, for example: https://academico3.cefetes.br/qacademico
     */
    public QAcadScrapper(String url) {
        if(url.endsWith("/")) { // remove the final bar from the url if necessary
            // Don't know if this actually necessary but, since I'm using all URL suffixes starting with "/", it is nice to keep this
            this.url = url.substring(0, url.length() - 1);
        } else {
            this.url = url;
        }
    }

    private void loadAcadTokensAndCookies() throws ConnectException{
        try {
            Connection.Response tokenResponse = Jsoup.connect(this.url + KEY_GENERATOR_PAGE)
                    .method(Connection.Method.GET)
                    .execute();

            // Get the keypair used in Q-Acadêmico website

            String keys = tokenResponse.body().substring(tokenResponse.body().indexOf("RSAKeyPair("), tokenResponse.body().lastIndexOf(")"));
            keys = keys.substring(keys.indexOf("\"") + 1, keys.lastIndexOf("\""));

            // The websites use a third-party RSA library to encrypt all data, I'm just using the same internal logic they use
            keyA = keys.substring(0, keys.indexOf("\"")); // Probably the public key
            keyB = keys.substring(keys.lastIndexOf("\"") + 1); // Probably the modulus

            cookieMap = tokenResponse.cookies();

        } catch (Exception e) {
            e.printStackTrace();
            throw new ConnectException("QAcad: Couldn't connect to Q-Acadêmico, please check if there's an internet connection available and if Q-Acadêmico website is working");
        }
    }

    public Map<String, String> loginToQAcad(User user) throws ConnectException, LoginException {
        if(!isLogged()) {
            System.out.println("QAcad: Logging into QAcad");
            loadAcadTokensAndCookies();

            try {
                user.encryptFields(keyA, keyB);

                Jsoup.connect(this.url + VALIDATE_LOGIN_PAGE)
                        .data("LOGIN", user.getEncryptedUsername())
                        .data("SENHA", user.getEncryptedPassword())
                        .data("Submit", user.getEncryptedSubmitText())
                        .data("TIPO_USU", user.getEncryptedUserTypeText())
                        .cookies(cookieMap)
                        .post();

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!isLogged()) {
                throw new LoginException("Invalid credentials, please check if you can login with them directly from the website");
            }
        }

        return cookieMap;
    }

    public boolean isLogged() {
        try {
            if(cookieMap == null) {
                return false;
            } else {
                Connection.Response loggedResponse = Jsoup.connect(this.url + MAIN_PAGE)
                        .cookies(cookieMap)
                        .execute();

                Document page = loggedResponse.parse();

                if (page.select(String.format("strong:contains(%s)", NOT_LOGGED_ERROR_TEXT)).isEmpty()) {
                    isLogged = true;
                    return true;
                } else {
                    isLogged = false;
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return this.isLogged;
    }

    public List<Subject> getAllSubjectsAndGrades(Map<String, String> cookieMap) throws ConnectException {
        this.cookieMap = cookieMap;

        return getAllSubjectsAndGrades();
    }

    public List<Subject> getAllSubjectsAndGrades() throws ConnectException {

        List<Subject> subjectList = new ArrayList<>();

        if(!isLogged()) {
            throw new IllegalStateException("Some error happended due to login status, have you called loginToQAcad()?");
        }

        try {
            gradesResponse = Jsoup.connect(this.url + GRADES_PAGE)
                    .cookies(cookieMap)
                    .execute();

            if (gradesResponse != null) {
                Document doc = gradesResponse.charset(PAGE_CHARSET).parse();

                for (Element element : doc.select("tr.rotulo").first().parent().children()) {
                    Subject subject = new Subject();

                    if (!element.className().equals("rotulo")) {

                        if (!element.children().select("strong").isEmpty()) { // If its a subject
                            // Extracts all information from subject text

                            element = element.children().select("strong").first();

                            String elementText = element.ownText();
                            int subjectId = Integer.valueOf(elementText.split(" - ")[0]);
                            String subjectClass = elementText.split(" - ")[1];
                            String subjectName = elementText.split(" - ")[2];
                            String subjectProfessor = elementText.split(" - ")[elementText.split(" - ").length - 1];

                            if (subjectProfessor.equals(subjectName)) { // if professor name is null, subjectProfessor should be equal to subjectName
                                subjectProfessor = null;
                            }

                            subject.setId(subjectId);
                            subject.setSubjectClass(subjectClass);
                            subject.setName(subjectName);
                            subject.setProfessor(subjectProfessor);

                            subjectList.add(subject);
                        } else {
                            // If its not a subject then we can assume the following elements are grades referring to the last added subject.

                            for (Element gradeElement : element.select("tr.conteudoTexto > * tr.conteudoTexto")) {
                                Grade grade = new Grade();

                                String name = gradeElement.child(1).ownText().substring(12).split(": ")[1];
                                grade.setName(name);

                                String dateText = gradeElement.child(1).ownText().split(",")[0]; // e.g.: 03/05/2019
                                grade.setDate(dateText);

                                float weight = Float.valueOf(gradeElement.child(2).ownText().split(":")[1]);
                                grade.setWeight(weight);

                                float maximumGrade = Float.valueOf(gradeElement.child(3).ownText().split(":")[1]);
                                grade.setMaximumGrade(maximumGrade);

                                float obtainedGrade = 0f;
                                boolean isObtainedGradeNull;
                                if (gradeElement.child(4).ownText().split(":").length != 1) { // Meaning "if there is a grade here"
                                    isObtainedGradeNull = false;
                                    obtainedGrade = Float.valueOf(gradeElement.child(4).ownText().split(":")[1]);
                                } else {
                                    isObtainedGradeNull = true;
                                }
                                grade.setObtainedGrade(obtainedGrade);
                                grade.setIsObtainedGradeNull(isObtainedGradeNull);
                                subjectList.get(subjectList.size() - 1).addGrade(grade);
                            }
                        }
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
            throw new ConnectException("QAcad: Couldn't connect to Q-Acadêmico, please check if there's an internet connection available and if Q-Acadêmico website is working");
        }
        return subjectList;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
