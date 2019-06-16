package calegari.murilo.qacadscrapper.utils;

import com.sun.script.javascript.RhinoScriptEngine;
import org.mozilla.javascript.Context;

import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static calegari.murilo.qacadscrapper.utils.Tools.log;

public class User {

    private boolean optimizeEncryptionEnabled = false;

    private boolean multiThreadEnabled = true;
    private static final int RHINO_OPTIMIZATION_LEVEL = 9;

    private String username;
    private String password;
    private String fullName;

    private List<Field> fieldsToEncrypt = new ArrayList<>();
    private Map<String, String> encryptedFields = new HashMap<>();

    private List<Thread> activeThreads = new ArrayList<>();

    private final String USERNAME_KEY = "usernamekey";
    private final String PASSWORD_KEY = "passwordkey";
    private final String SUBMIT_TEXT_KEY = "submittextkey";
    private final String USERTYPE_TEXT_KEY = "usertypetextkey";

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void encryptFields(String keyA, String keyB) {

        fieldsToEncrypt.add(new Field(USERNAME_KEY, username));
        fieldsToEncrypt.add(new Field(PASSWORD_KEY, password));
        fieldsToEncrypt.add(new Field(SUBMIT_TEXT_KEY, "OK"));
        fieldsToEncrypt.add(new Field(USERTYPE_TEXT_KEY, "1"));

        try {
            log("Encrypting fields...");

            if(multiThreadEnabled) {
                encryptFieldsSingleOrMultiThreaded(fieldsToEncrypt, keyA, keyB, true);

                for (int i = activeThreads.size() - 1; i >= 0; i--) {
                    // We have to wait until all threads are done to proceed.
                    activeThreads.get(i).join();
                    activeThreads.remove(i);
                }
            } else {
                encryptFieldsSingleOrMultiThreaded(fieldsToEncrypt, keyA, keyB, false);
            }

            log("Done encrypting fields...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void encryptFieldsSingleOrMultiThreaded(List<Field> fields, String keyA, String keyB, boolean multiThreadEnabled) {
        Encrypter encrypter = null;

        if(!multiThreadEnabled) {
            // If multiThread is not enabled, then use a single encrypter instance for all the sequential encryptions
            encrypter = new Encrypter(keyA, keyB);
        }

        // Encrypts the fields from the last one to the first so when removing the field from fieldsToEncrypt it doesn't
        // alter other fields indexes

        for (int i = fields.size() - 1; i >= 0; i--) {
            Field field = fields.get(i);

            if(multiThreadEnabled) {
                Thread encryptionThread = new Thread(() -> {
                    try {
                        // I have to call a new encrypter instance for every field since concurrency on the same context was
                        // breaking the encryption

                        Encrypter singleRunEncrypter = new Encrypter(keyA, keyB);
                        field.encryptedField = singleRunEncrypter.encrypt(field.plainTextField);
                        singleRunEncrypter.exit();

                        encryptedFields.put(field.fieldKey, field.encryptedField);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                activeThreads.add(encryptionThread);
                encryptionThread.start();
            } else {
                try {
                    field.encryptedField = encrypter.encrypt(field.plainTextField);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                encryptedFields.put(field.fieldKey, field.encryptedField);
            }

            fieldsToEncrypt.remove(i);
        }

        if(!multiThreadEnabled) {
            encrypter.exit();
        }
    }

    private class Encrypter {
        private RhinoScriptEngine engine;
        private Context context;

        private Encrypter(String keyA, String keyB){

            if(optimizeEncryptionEnabled) {
                context = Context.enter();
                context.setOptimizationLevel(RHINO_OPTIMIZATION_LEVEL);
            }

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
        }

        private String encrypt(String username) throws ScriptException {
            return (String) engine.eval(String.format("encryptedString(key, \"%s\")", username));
        }

        private void exit() {
            if (optimizeEncryptionEnabled) {
                Context.exit();
            }
        }
    }

    public String getEncryptedUsername() {
        return encryptedFields.get(USERNAME_KEY);
    }

    public String getEncryptedPassword() {
        return encryptedFields.get(PASSWORD_KEY);
    }

    public String getEncryptedSubmitText() {
        return encryptedFields.get(SUBMIT_TEXT_KEY);
    }

    public String getEncryptedUserTypeText() {
        return encryptedFields.get(USERTYPE_TEXT_KEY);
    }

    public boolean isOptimizeEncryptionEnabled() {
        return optimizeEncryptionEnabled;
    }

    /**
     * By default the library does not optimize the encryption of fields since it might not work
     * on some system, for example, on Android it throw a java.lang.UnsupportedOperationException
     * @param optimizeEncryptionEnabled Should the library optimize the encryption process
     */

    public void setOptimizeEncryptionEnabled(boolean optimizeEncryptionEnabled) {
        this.optimizeEncryptionEnabled = optimizeEncryptionEnabled;
    }

    public void setMultiThreadEnabled(boolean multiThreadEnabled) {
        this.multiThreadEnabled = multiThreadEnabled;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    private class Field {
        private String fieldKey;
        private String plainTextField;
        private String encryptedField;

        private Field(String fieldKey, String plainTextField) {
            this.fieldKey = fieldKey;
            this.plainTextField = plainTextField;
        }
    }

    private Field getFieldFromList(List<Field> fields, String fieldKey) {
        for(Field field: fields) {
            if(field.fieldKey.equals(fieldKey)) {
                return field;
            }
        }

        // If no field is found, throw IllegalArgumentException

        throw new IllegalArgumentException("No field with given key was found");
    }
}
