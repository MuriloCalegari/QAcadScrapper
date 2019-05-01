[![Release](https://jitpack.io/v/MuriloCalegari/QAcadScrapper.svg)](https://jitpack.io/#MuriloCalegari/QAcadScrapper)

# QAcadScrapper
Scrapper library that parses info from Q-Acadêmico website

## Implementation

Step 1.
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.MuriloCalegari:QAcadScrapper:v0.0.2'
	}
  
  
## Usage
```java
QAcadScrapper qAcadScrapper = new QAcadScrapper("https://academico3.cefetes.br/qacademico" /* URL TO QACAD PAGE */);

List<Subject> subjects = null;
try {
  User user = new User("USERNAME", "PASSWORD");

  qAcadScrapper.loginToQAcad(user);

  subjects = qAcadScrapper.getAllSubjectsAndGrades();
} catch (LoginException e) {
  // DO SOMETHING IF LOGIN IS INVALID
} catch (ConnectException e) {
  // DO SOMETHING IF THERE'S A CONNECTION EXCEPTION
}
```

### Available methods:

#### QAcadScrapper class

> `loginToQAcad()`, `isLogged()`, `getAllSubjectsAndGrades()` and getter and setter for cookieMap.

#### Subject class

> Getters and settes for id, subjectClass, name, professor, abbreviation, obtainedGrade, maximumGrade and gradeList

#### Grade class

> Getters and settes for acadSubjectId, gradeDescription, date (as String), obtainedGrade, maximumGrade, weight and isObtainedGradeNull

## Some useful information

Logging into QAcad takes some resources since it needs to encrypt the credentials using the same JavaScript library as QAcad, so it is advised to only call `loginToQAcad()` if really necessary.

`loginToQAcad()` returns the cookies as a `Map<String, String>`, so you can assign this to a variable and use it in the QAcadScrapper instance by calling `qAcadScrapper.setCookieMap(cookieMap)`. For example:

```java
qAcadScrapper.setCookieMap(cookieMap);
  if (!qAcadScrapper.isLogged()) {
    Log.d(TAG, "Status is not logged");
    cookieMap = qAcadScrapper.loginToQAcad(user);
    Log.d(TAG, "Finished logging into QAcad");
  } else {
    Log.d(TAG, "We've already logged");
  }

  Log.d(TAG, "Getting all subjects and grades from QAcad");
  List<Subject> subjectList = qAcadScrapper.getAllSubjectsAndGrades();
  Log.d(TAG, "All subjects and grades from QAcad were obtained!");
```

getAllSubjectsAndGrades() will throw an IllegalStateException if the cookieMap results in a non logged state, so it is important that you check if the cookieMap is valid by using isLogged(), as it was done in the previous example.

## Example of implemented library

I've used this library in my Ifes Acadêmico project, which you can check in this link:

https://github.com/MuriloCalegari/Ifes-Academico

I've used some AsyncTasks which are implemented in utils -> QAcadIntegration -> (QAcadFetchDataTask and QAcadCheckLoginTask)
