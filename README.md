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
User user = new User("USERNAME", "PASSWORD");
QAcadScrapper qAcadScrapper = new QAcadScrapper("https://academico3.cefetes.br/qacademico", user);

List<Subject> subjects = null;
try {
  subjects = qAcadScrapper.getAllSubjectsAndGrades(); // Automatically handles login if state is not logged
} catch (LoginException e) {
  // DO SOMETHING IF LOGIN IS INVALID
} catch (IOException e) {
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

Calling any retrieve data method automatically logins to QAcad, so you can call it and then retrieve the cookieMap by using getCookieMap()

```java
  qAcadScrapper.setCookieMap(cookieMap);

  Log.d(TAG, "Getting all subjects and grades from QAcad");
  List<Subject> subjectList = qAcadScrapper.getAllSubjectsAndGrades();
  Log.d(TAG, "All subjects and grades from QAcad were obtained!");
```

Or:

```java
  User user = new User("USERNAME", "PASSWORD");
  QAcadScrapper qAcadScrapper = new QAcadScrapper("https://academico3.cefetes.br/qacademico", user);

  Log.d(TAG, "Getting all subjects and grades from QAcad");
  List<Subject> subjectList = qAcadScrapper.getAllSubjectsAndGrades();
  Log.d(TAG, "All subjects and grades from QAcad were obtained!");
  
  Map<String, String> = qAcadScrapper.getCookieMap();
```

getAllSubjectsAndGrades() will throw an LoginException if the login results in a non logged state, so it is important that you catch it and do the appropriate handling of this error.

## Example of implemented library

I've used this library in my Ifes Acadêmico project, which you can check in this link:

https://github.com/MuriloCalegari/Ifes-Academico

I've used some AsyncTasks which are implemented in utils -> QAcadIntegration -> (QAcadFetchDataTask and QAcadCheckLoginTask)
