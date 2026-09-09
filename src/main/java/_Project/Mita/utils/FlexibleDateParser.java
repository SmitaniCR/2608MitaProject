package _Project.Mita.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class FlexibleDateParser {
	
	private FlexibleDateParser(){//鍵
		}

	public static LocalDate parseFlexibleDate(String dateStr) {
		if (dateStr == null || dateStr.trim().isEmpty()) {
			return null;
		}

		String normalization = dateStr.trim();
		if (normalization.length() == 6) { // yyyyMM の場合
			normalization += "01";
		} else if (normalization.length() == 4) { // yyyy の場合
			normalization += "0101";
		}

		try {
			DateTimeFormatter strictFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
					.withResolverStyle(ResolverStyle.STRICT);
			return LocalDate.parse(normalization, strictFormatter);
		} catch (DateTimeParseException e) {
			return null;
		}
	}
}