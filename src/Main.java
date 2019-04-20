import calegari.murilo.qacadscrapper.QAcadScrapper;
import calegari.murilo.qacadscrapper.utils.Subject;
import calegari.murilo.qacadscrapper.utils.User;

import javax.security.auth.login.LoginException;
import java.net.ConnectException;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        QAcadScrapper qAcadScrapper = new QAcadScrapper("https://academico3.cefetes.br/qacademico");

        try {
            User user = new User("USERNAME", "PASSWORD");

            qAcadScrapper.loginToQAcad(user);

            List<Subject> subjects = qAcadScrapper.getAllSubjectsAndGrades();
        } catch (ConnectException | LoginException e) {
           e.printStackTrace();
        }
    }
}
