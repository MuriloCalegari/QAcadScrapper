package calegari.murilo.qacadscrapper.utils;

import com.sun.script.javascript.RhinoScriptEngine;
import org.mozilla.javascript.Context;

import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class User {

    public static final int RHINO_OPTIMIZATION_LEVEL = 9;
    private String username;
    private String password;

    private String encryptedUsername;
    private String encryptedPassword;
    private String encryptedSubmitText;
    private String encryptedUserTypeText;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void encryptFields(String keyA, String keyB) {

        try {
            System.out.println("QAcad: Encrypting fields...");
            Encrypter encrypter = new Encrypter(keyA, keyB);

            encryptedUsername = encrypter.encrypt(username);
            encryptedPassword = encrypter.encrypt(password);
            encryptedSubmitText = encrypter.encrypt("OK");
            encryptedUserTypeText = encrypter.encrypt("1");
            System.out.println("QAcad: Done encrypting fields...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class Encrypter {
        private RhinoScriptEngine engine;
        private Context context;

        private Encrypter(String keyA, String keyB){
            context = Context.enter();
            context.setOptimizationLevel(RHINO_OPTIMIZATION_LEVEL);
            engine = new RhinoScriptEngine();
            try {

                /*
                Uses the same JavaScript code used in the website, I wasn't able to reproduce its behavior,
                so I need to load the code using the ScriptEngine.
                */

                InputStream rsaInputStream = getClass().getResourceAsStream("/calegari/murilo/qacadscrapper/lib/RSA.js");
                BufferedReader rsaReader = new BufferedReader(new InputStreamReader(rsaInputStream));

                InputStream bigIntInputStream = getClass().getResourceAsStream("/calegari/murilo/qacadscrapper/lib/BigInt.js");
                BufferedReader bigIntReader = new BufferedReader(new InputStreamReader(bigIntInputStream));

                InputStream barrettInputStream = getClass().getResourceAsStream("/calegari/murilo/qacadscrapper/lib/Barrett.js");
                BufferedReader barrettReader = new BufferedReader(new InputStreamReader(barrettInputStream));

                // Loads all scripts into the engine

                engine.eval(rsaReader);
                engine.eval(bigIntReader);
                engine.eval(barrettReader);

                // Defines the key variable used in encrypt()
                String generateKeyPairCommand = String.format("var key = new RSAKeyPair(\"%s\",\"\",\"%s\")", keyA, keyB);
                engine.eval(generateKeyPairCommand);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Context.exit();
        }

        private String encrypt(String username) throws ScriptException {
            return (String) engine.eval(String.format("encryptedString(key, \"%s\")", username));
        }
    }



    public String getEncryptedUsername() {
        return encryptedUsername;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public String getEncryptedSubmitText() {
        return encryptedSubmitText;
    }

    public String getEncryptedUserTypeText() {
        return encryptedUserTypeText;
    }
}
