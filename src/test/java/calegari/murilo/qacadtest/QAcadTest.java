package calegari.murilo.qacadtest;

import calegari.murilo.qacadscrapper.QAcadScrapper;
import calegari.murilo.qacadscrapper.utils.Subject;
import calegari.murilo.qacadscrapper.utils.User;
import org.junit.Test;
import static org.junit.Assert.*;


import javax.security.auth.login.LoginException;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

public class QAcadTest {

    @Test
    public void testLogin() throws IOException, LoginException {
        getLoggedQAcadInstance();
    }

    @Test
    public void testGetAllSubjects() throws IOException, LoginException {
        QAcadScrapper qAcadScrapper = getLoggedQAcadInstance();

        List<Subject> subjects = null;
        try {

            qAcadScrapper.getUser().setOptimizeEncryptionEnabled(true);
            qAcadScrapper.getUser().setMultiThreadEnabled(true);

            subjects = qAcadScrapper.getAllSubjectsAndGrades();
        } catch (IOException | LoginException e) {
            e.printStackTrace();
        }

        assertNotNull(subjects);
    }

    @Test
    public void testEncryption() {
        User user = new User("20171xxxxx0289", "xxxxxxxxxxx");

        user.setOptimizeEncryptionEnabled(true);
        user.setMultiThreadEnabled(true);
        user.encryptFields("67e522132eedc63fc983e14a2b35335", "fd31baa1c64b1e6233345e4685b91e1");
    }

    @Test
    public void testExtractDate() {
        String dateText = "03/05/2019";

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date = LocalDate.parse(dateText, dateTimeFormatter);

        assertNotNull(date);
    }

    @Test
    public void testGetFullName() throws IOException, LoginException {
        assertNotNull(getLoggedQAcadInstance().getUser().getFullName());
    }

    public QAcadScrapper getLoggedQAcadInstance() throws IOException, LoginException {
        Properties p = new Properties();
        p.load(new FileReader(new File("C:\\Users\\muril\\IdeaProjects\\QAcadScrapper\\src\\test\\resources\\keystore.properties")));

        User user = new User(p.get("login").toString(),  p.get("password").toString());
        QAcadScrapper qAcadScrapper = new QAcadScrapper("https://academico3.cefetes.br/qacademico", user);

        qAcadScrapper.loginToQAcad();

        return qAcadScrapper;
    }
}
