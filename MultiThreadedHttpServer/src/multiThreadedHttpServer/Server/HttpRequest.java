package multiThreadedHttpServer.Server;

import java.io.File;

import static multiThreadedHttpServer.util.Constants.ROOT_DIR;

public class HttpRequest {
	private String requestType;
	private String resourceURI;
	private String protocol;

	public HttpRequest(String requestIn) {
		String[] request = requestIn.split(" ");
		requestType = request[0].replaceAll("\\s", "");
		resourceURI = request[1].replaceAll("\\s", "");
		resourceURI = resourceURI.substring(1, resourceURI.length());
		protocol = request[2].replaceAll("\\s", "");
	}

	/**
	 * @return the requestType
	 */
	public String getRequestType() {
		return requestType;
	}

	/**
	 * @param requestTypeIn
	 *            the requestType to set
	 */
	public void setRequestType(String requestTypeIn) {
		requestType = requestTypeIn;
	}

	/**
	 * @return the resourceURI
	 */
	public String getResourceURI() {
		return resourceURI;
	}

	/**
	 * @param resourceURIIn
	 *            the resourceURI to set
	 */
	public void setResourceURI(String resourceURIIn) {
		resourceURI = resourceURIIn;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocolIn
	 *            the protocol to set
	 */
	public void setProtocol(String protocolIn) {
		protocol = protocolIn;
	}

	public boolean validateRequest() {
		if (!("GET".equals(requestType)))
			throw new IllegalArgumentException("Request Type " + requestType + " is not supported.");
		if (!(protocol.contains("HTTP")))
			throw new IllegalArgumentException("Protocol Type " + protocol + " is not supported.");
		File file = new File(ROOT_DIR + File.separator + resourceURI);
		if (!file.exists())
			return false;
		return true;
	}
}
