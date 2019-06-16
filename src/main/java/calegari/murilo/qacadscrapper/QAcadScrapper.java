package calegari.murilo.qacadscrapper;

import calegari.murilo.qacadscrapper.utils.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static calegari.murilo.qacadscrapper.utils.Tools.log;


@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class QAcadScrapper {

    private final String KEY_GENERATOR_PAGE = "/qacademico/lib/rsa/gerador_chaves_rsa.asp?form=frmLogin&action=%2Fqacademico%2Flib%2Fvalidalogin%2Easp";
    private final String VALIDATE_LOGIN_PAGE = "/qacademico/lib/validalogin.asp";
    private final String MAIN_PAGE = "/qacademico/index.asp?t=2000";
    private final String GRADES_PAGE = "/qacademico/index.asp?t=2071";
    private final String MATERIALS_PAGE = "/qacademico/index.asp?t=2061";
    private final String NOT_LOGGED_ERROR_TEXT = "Acesso Negado";
    private final String PAGE_CHARSET = "windows-1252"; // use document.charset in chrome developer mode to obtain it.

    private User user;

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
    private Connection.Response gradesResponse;

    /**
     * QAcadScrapper can extract data from the Q-Acadêmico website in a simple way
     * @param url URL in the form https://[YOUR_INSTITUTION's_Q_ACADEMICO_HOST]/qacademico, for example: https://academico3.cefetes.br/qacademico
     */
    public QAcadScrapper(String url, User user) {
        if(url.endsWith("/")) { // remove the final bar from the url if necessary
            // Don't know if this actually necessary but, since I'm using all URL suffixes starting with "/", it is nice to keep this
            this.url = url.substring(0, url.length() - 1);
        } else {
            this.url = url;
        }

        this.user = user;
    }

    private void loadAcadTokensAndCookies() throws IOException {
        try {
            log("Loading tokens and cookies");
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

            log("Finished loading tokens and cookies");
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("QAcad: Couldn't connect to Q-Acadêmico, please check if there's an internet connection available and if Q-Acadêmico website is working");
        }
    }

    public Map<String, String> loginToQAcad() throws IOException, LoginException {
        return loginToQAcad(user);
    }

    private Map<String, String> loginToQAcad(User user) throws IOException, LoginException {
        log("Logging into QAcad");
        loadAcadTokensAndCookies();

        user.encryptFields(keyA, keyB);

        log("Validating login");
        Document response = Jsoup.connect(this.url + VALIDATE_LOGIN_PAGE)
                .data("LOGIN", user.getEncryptedUsername())
                .data("SENHA", user.getEncryptedPassword())
                .data("Submit", user.getEncryptedSubmitText())
                .data("TIPO_USU", user.getEncryptedUserTypeText())
                .cookies(cookieMap)
                .post();
        log("Finished validating login");

        if (!isLogged(response)) {
            throw new LoginException("Invalid credentials, please check if you can login with them directly from the website");
        }

        user.setFullName(parseFullNameFromMainPage(response));

        log("Finished login into QAcad");
        return cookieMap;
    }

    private String parseFullNameFromMainPage(Document response) {
        String fullWelcomeText;
        String fullName = null;
        try {
            fullWelcomeText = response.select("td.titulo > * td.titulo").first().ownText();
            fullName = fullWelcomeText.substring(fullWelcomeText.lastIndexOf(",") + 1, fullWelcomeText.lastIndexOf("!")).trim();
        } catch (Exception ignored) {}

        if(fullName != null) {
            return fullName;
        } else {
            return null;
        }
    }

    public boolean isLogged() throws IOException {
        if(cookieMap == null) {
            return false;
        } else {
            Connection.Response loggedResponse = Jsoup.connect(this.url + MAIN_PAGE)
                    .cookies(cookieMap)
                    .execute();
            return isLogged(loggedResponse.parse());
        }
    }

    private boolean isLogged(Document page) {
        if(page == null || cookieMap == null) {
            return false;
        } else if (page.select(String.format("strong:contains(%s)", NOT_LOGGED_ERROR_TEXT)).isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public List<Subject> getAllSubjectsAndGrades(Map<String, String> cookieMap) throws IOException, LoginException {
        this.cookieMap = cookieMap;

        return getAllSubjectsAndGrades();
    }

    public List<Subject> getAllSubjectsAndGrades() throws IOException, LoginException {

        /*
        This method has the intention to save as many web pages loads as possible, so I automatically check
        the login status checking for null cookies or using the same response I would use if it was already
        logged.
         */

        if(cookieMap == null) {
            loginToQAcad();
            return getAllSubjectsAndGrades();
        } else {
            log("Getting response from grades page");

            gradesResponse = Jsoup.connect(this.url + GRADES_PAGE)
                    .cookies(cookieMap)
                    .execute();

            if (gradesResponse != null) {
                Document doc = gradesResponse.charset(PAGE_CHARSET).parse();

                if (!isLogged(doc)) {
                    loginToQAcad();
                    return getAllSubjectsAndGrades();
                } else {
                    return parseGradesPage(doc);
                }
            }
        }

        return new ArrayList<>(); // returns empty list by default
    }

    private List<Subject> parseGradesPage(Document gradesPage) {
        log("Parsing grades page");

        List<Subject> subjectList = new ArrayList<>();

        for (Element element : gradesPage.select("tr.rotulo").first().parent().children()) {
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

                        String name = gradeElement.child(1).ownText().substring(12).split(": ", 2)[1];
                        grade.setName(name);

                        String dateText = gradeElement.child(1).ownText().split(",")[0]; // e.g.: 03/05/2019
                        grade.setDate(Tools.getLocalDate(dateText));

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

        log("Finished parsing grades page");

        return subjectList;
    }

    public List<ClassMaterial> getAllMaterials() throws IOException, LoginException {
        if(cookieMap == null) {
            loginToQAcad();
            return getAllMaterials();
        } else {
            log("Getting response from materials page");
            Document materialsPage = Jsoup.connect(this.url + MATERIALS_PAGE)
                    .cookies(cookieMap)
                    .execute()
                    .parse();

            if(!isLogged(materialsPage)) {
                loginToQAcad();
                return getAllMaterials();
            } else {
                return parseMaterialsPage(materialsPage);
            }
        }
    }

    private List<ClassMaterial> parseMaterialsPage(Document materialsPage) throws MalformedURLException {
        log("Parsing materials page");

        List<ClassMaterial> classesMaterials = new ArrayList<>();

        Elements materialsTable = materialsPage.select("td:contains(Data de publicação)").last().parent().parent().children();

        int lastFoundSubjectId = -1;

        for(Element element: materialsTable) {
            if(!element.attr("bgcolor").equals("#CCCCCC")) { // If it's not the header
                if(element.className().equals("rotulo")) { // If it's a subject identification
                    lastFoundSubjectId = Integer.valueOf(element.text().split(" - ", 2)[0]);
                } else { // We can assume the following are materials referring to the last found subject id
                    ClassMaterial classMaterial = new ClassMaterial();

                    classMaterial.setSubjectId(lastFoundSubjectId);

                    String dateText = element.child(0).ownText();
                    classMaterial.setReleaseDate(Tools.getLocalDate(dateText));

                    String completeUrlSuffix = element.select("a[href]").first().attr("href");

                    String urlSuffix = completeUrlSuffix.substring(0, completeUrlSuffix.lastIndexOf("/") + 1);
                    String urlFile = completeUrlSuffix.substring(completeUrlSuffix.lastIndexOf("/") + 1);
                    String urlProtocol = element.baseUri().split(":", 2)[0];
                    String host = this.url.substring(this.url.indexOf("/") + 2) + urlSuffix;
                    URL downloadUrl = new URL(urlProtocol, host, urlFile);

                    classMaterial.setDownloadURL(downloadUrl);

                    String materialTitle = element.select("a").first().ownText();
                    classMaterial.setTitle(materialTitle);

                    classesMaterials.add(classMaterial);
                }
            }
        }

        log("Finished parsing materials page");

        return classesMaterials;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}