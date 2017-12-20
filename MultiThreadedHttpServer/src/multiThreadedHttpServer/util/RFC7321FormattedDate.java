package multiThreadedHttpServer.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class RFC7321FormattedDate {

	private String date;

	public RFC7321FormattedDate(Calendar calender) {
		if (null == calender)
			throw new IllegalArgumentException("Date Cannot be null.");
		SimpleDateFormat simpleDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		simpleDate.setTimeZone(TimeZone.getTimeZone("GMT"));
		date = simpleDate.format(calender.getTime());
	}

	public String getDate() {
		return date;
	}

	public void setDate(String dateIn) {
		date = dateIn;
	}
}
