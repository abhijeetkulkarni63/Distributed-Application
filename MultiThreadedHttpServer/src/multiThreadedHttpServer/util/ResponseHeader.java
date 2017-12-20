package multiThreadedHttpServer.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ResponseHeader {

	private RFC7321FormattedDate date;
	private final String serverName = "akulka16-httpServer";
	private RFC7321FormattedDate lastModifiedDateTime;
	private String contentType;
	private Long contentLength;

	/**
	 * @return the date
	 */
	public RFC7321FormattedDate getDate() {
		return date;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(RFC7321FormattedDate dateIn) {
		date = dateIn;
	}

	/**
	 * @return the lastModifiedDateTime
	 */
	public RFC7321FormattedDate getLastModifiedDateTime() {
		return lastModifiedDateTime;
	}

	/**
	 * @param lastModifiedDateTime
	 *            the lastModifiedDateTime to set
	 */
	public void setLastModifiedDateTime(RFC7321FormattedDate lastModifiedDateTimeIn) {
		lastModifiedDateTime = lastModifiedDateTimeIn;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param contentType
	 *            the contentType to set
	 */
	public void setContentType(File fileResourceIn) {
		try {
			contentType = Files.probeContentType(Paths.get(fileResourceIn.getPath()));
		} catch (IOException exception) {
			System.err.println(exception.getStackTrace());
		}
	}

	/**
	 * @return the contentLength
	 */
	public Long getContentLength() {
		return contentLength;
	}

	/**
	 * @param contentLength
	 *            the contentLength to set
	 */
	public void setContentLength(Long contentLengthIn) {
		contentLength = contentLengthIn;
	}

	/**
	 * @return the serverName
	 */
	public String getServerName() {
		return serverName;
	}

}
