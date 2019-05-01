package calegari.murilo.qacadtest;

import calegari.murilo.qacadscrapper.QAcadScrapper;
import calegari.murilo.qacadscrapper.utils.Subject;
import calegari.murilo.qacadscrapper.utils.User;
import org.junit.Test;
import static org.junit.Assert.*;


import javax.security.auth.login.LoginException;
import java.net.ConnectException;
import java.util.List;

public class QAcadTest {

    @Test
    public void testGetAllSubjects() {
        QAcadScrapper qAcadScrapper = new QAcadScrapper("https://academico3.cefetes.br/qacademico");

        List<Subject> subjects = null;
        try {
            User user = new User("USERNAME", "PASSWORD");

            qAcadScrapper.loginToQAcad(user);


            subjects = qAcadScrapper.getAllSubjectsAndGrades();
        } catch (ConnectException | LoginException e) {
            e.printStackTrace();
        }

        assertNotNull(subjects);
    }
}
