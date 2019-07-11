package calegari.murilo.qacadscrapper.utils;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

public abstract class Tools {

	public static LocalDate getLocalDate(String dateText) {
		/*
		For some reason sometimes user gets a
		org.threeten.bp.format.DateTimeParseException: Text '26/06/2019 12:32:00' could not be parsed
		exception. So I first remove any subsequent words to avoid this error.
		*/

		dateText = dateText.split(" ")[0];

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		return LocalDate.parse(dateText, dateTimeFormatter);
	}

	public static void log(String message) {
		System.out.println("QAcad: " + message);
	}

}
