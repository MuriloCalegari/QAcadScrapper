package calegari.murilo.qacadscrapper.utils;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

public abstract class Tools {

	public static LocalDate getLocalDate(String dateText) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		return LocalDate.parse(dateText, dateTimeFormatter);
	}

}
