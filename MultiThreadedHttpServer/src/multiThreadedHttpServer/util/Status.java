package multiThreadedHttpServer.util;

public class Status {

	private int statusCode;
	private String statusDescription;

	public Status(int statusCodeIn, String statusDescriptionIn) {
		statusCode = statusCodeIn;
		statusDescription = statusDescriptionIn;
	}

	/**
	 * @return the statusCode
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode
	 *            the statusCode to set
	 */
	public void setStatusCode(int statusCodeIn) {
		statusCode = statusCodeIn;
	}

	/**
	 * @return the statusDescription
	 */
	public String getStatusDescription() {
		return statusDescription;
	}

	/**
	 * @param statusDescription
	 *            the statusDescription to set
	 */
	public void setStatusDescription(String statusDescriptionIn) {
		statusDescription = statusDescriptionIn;
	}

}
