package calegari.murilo.qacadtest;

import calegari.murilo.qacadscrapper.QAcadScrapper;
import calegari.murilo.qacadscrapper.utils.ClassMaterial;
import calegari.murilo.qacadscrapper.utils.Subject;
import calegari.murilo.qacadscrapper.utils.Tools;
import calegari.murilo.qacadscrapper.utils.User;
import org.junit.Test;
import org.threeten.bp.LocalDate;

import static org.junit.Assert.*;


import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class QAcadTest {

    private static QAcadScrapper qAcadScrapper;

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
    public void testGetAllMaterials() throws IOException, LoginException {
        QAcadScrapper qAcadScrapper = getLoggedQAcadInstance();

        List<ClassMaterial> classMaterials = qAcadScrapper.getAllMaterials();

        assertNotNull(classMaterials);
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
        String dateText = "26/06/2019";
        LocalDate date1 = Tools.getLocalDate(dateText);

        dateText = "26/06/2019 12:32:00";
        LocalDate date2 = Tools.getLocalDate(dateText);

        LocalDate expectedDate = LocalDate.of(2019, 6, 26);

        assertEquals(expectedDate, date1);
        assertEquals(expectedDate, date2);
    }

    @Test
    public void testGetFullName() throws IOException, LoginException {
        assertNotNull(getLoggedQAcadInstance().getUser().getFullName());
    }

    @Test
    public void testMultipleInstances() throws IOException, InterruptedException, LoginException {
        log("Preparing for multiple instances test");

        log("Logging to qAcad");

        QAcadScrapper qAcadScrapper = getLoggedQAcadInstance();
        Map<String, String> cookieMap = qAcadScrapper.getCookieMap();

        User user = getDefaultUser() ;

        AtomicReference<List<Subject>> subjects = new AtomicReference<>();
        AtomicReference<List<ClassMaterial>> materials = new AtomicReference<>();

        List<Thread> activeThreads = new ArrayList<>();

        for(int i = 0; i <= 1; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                QAcadScrapper threadedQAcadScrapper = new QAcadScrapper("https://academico3.cefetes.br", user);

                threadedQAcadScrapper.setCookieMap(cookieMap);

                try {
                    switch (finalI) {
                        case 0:
                            log("Getting subjects and grades");
                            subjects.set(threadedQAcadScrapper.getAllSubjectsAndGrades());
                            break;
                        case 1:
                            log("Getting materials");
                            materials.set(threadedQAcadScrapper.getAllMaterials());
                            break;
                    }
                } catch (IOException | LoginException e) {
                    e.printStackTrace();
                }
            });

            thread.start();
            activeThreads.add(thread);
        }

        for (int i = activeThreads.size() - 1; i >= 0; i--) {
            // We have to wait until all threads are done to proceed.
            activeThreads.get(i).join();
            activeThreads.remove(i);
        }

        assertNotNull(subjects);
        assertNotNull(materials);
    }

    private QAcadScrapper getLoggedQAcadInstance() throws IOException, LoginException {
        if(qAcadScrapper == null /*|| !qAcadScrapper.isLogged()*/) {

            User user = getDefaultUser();
            QAcadScrapper qAcadScrapper = new QAcadScrapper("https://academico3.cefetes.br", user);

            qAcadScrapper.loginToQAcad();

            QAcadTest.qAcadScrapper = qAcadScrapper;

            return qAcadScrapper;
        } else {
            return qAcadScrapper;
        }
    }

    private void log(String message) {
        System.out.println("Test: " + message);
    }

    private User getDefaultUser() {
        Properties p = new Properties();
        try {
            p.load(new FileReader(new File("C:\\Users\\muril\\IdeaProjects\\QAcadScrapper\\src\\test\\resources\\keystore.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new User(p.get("login").toString(), p.get("password").toString());
    }
}
