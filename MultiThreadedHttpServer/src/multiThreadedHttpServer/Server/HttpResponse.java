package multiThreadedHttpServer.Server;

import multiThreadedHttpServer.util.Constants;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.concurrent.ConcurrentMap;

import multiThreadedHttpServer.util.RFC7321FormattedDate;
import multiThreadedHttpServer.util.ResponseHeader;
import multiThreadedHttpServer.util.Status;

public class HttpResponse {

	private Status status;
	private String protocol;
	private ResponseHeader header;
	private String content;
	private File fileResource;

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(Status statusIn) {
		status = statusIn;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol
	 *            the protocol to set
	 */
	public void setProtocol(String requestIn) {
		if (null != requestIn) {
			String[] request = requestIn.split(" ");
			protocol = request[2].replaceAll("\\s", "");
		} else {
			protocol = null;
		}
	}

	/**
	 * @return the header
	 */
	public ResponseHeader getHeader() {
		return header;
	}

	/**
	 * @param header
	 *            the header to set
	 */
	public void setHeader(ResponseHeader headerIn) {
		header = headerIn;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(String contentIn) {
		content = contentIn;
	}

	/**
	 * @return the fileResource
	 */
	public File getFileResource() {
		return fileResource;
	}

	/**
	 * @param fileResource
	 *            the fileResource to set
	 */
	public void setFileResource(String requestIn) {
		if (null != requestIn) {
			String[] request = requestIn.split(" ");
			String resourceURI = request[1].replaceAll("\\s", "");
			resourceURI = resourceURI.substring(1, resourceURI.length());
			fileResource = new File(Constants.ROOT_DIR + File.separator + resourceURI);
		} else {
			fileResource = null;
		}
	}

	public void buildHeader(HttpResponse response, ConcurrentMap<String, RFC7321FormattedDate> lastModificationLog) {
		header = new ResponseHeader();
		header.setDate(new RFC7321FormattedDate(Calendar.getInstance()));
		if (null != fileResource) {
			header.setLastModifiedDateTime(lastModificationLog.get(response.getFileResource().toString()));
			header.setContentType(fileResource);
			header.setContentLength(fileResource.length());
		}
	}

	public String generateResponseHeader() {
		StringBuilder response = new StringBuilder();
		response.append(protocol + Constants.HEADER_SPACE + status.getStatusCode() + Constants.HEADER_SPACE
				+ status.getStatusDescription() + Constants.HEADER_LINE_END);
		if (null != header.getDate())
			response.append(Constants.HEADER_DATE + Constants.HEADER_SPACE + header.getDate().getDate()
					+ Constants.HEADER_LINE_END);
		if (null != header.getServerName())
			response.append(Constants.HEADER_SERVER + Constants.HEADER_SPACE + header.getServerName()
					+ Constants.HEADER_LINE_END);
		if (null != header.getLastModifiedDateTime())
			response.append(Constants.HEADER_LAST_MODIFIED + Constants.HEADER_SPACE
					+ header.getLastModifiedDateTime().getDate() + Constants.HEADER_LINE_END);
		if (null != header.getContentType())
			response.append(Constants.HEADER_CONTENT_TYPE + Constants.HEADER_SPACE + header.getContentType()
					+ Constants.HEADER_LINE_END);
		if (null != header.getContentLength())
			response.append(Constants.HEADER_CONTENT_LENGTH + Constants.HEADER_SPACE + header.getContentLength()
					+ Constants.HEADER_LINE_END);
		response.append(Constants.HEADER_END);
		return response.toString();
	}

	public void generateResourceResponse(DataOutputStream outputStreamIn) {
		byte[] buffer = new byte[8192];
		try {
			InputStream fileInput = new FileInputStream(fileResource);
			int fileSize = 0;
			while (0 < (fileSize = fileInput.read(buffer, 0, buffer.length))) {
				outputStreamIn.write(buffer, 0, fileSize);
				outputStreamIn.flush();
			}
			fileInput.close();
		} catch (IOException exception) {
			System.err.println(exception.getStackTrace());
		}
	}
}
