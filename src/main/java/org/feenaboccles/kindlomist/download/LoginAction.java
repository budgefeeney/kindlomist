package org.feenaboccles.kindlomist.download;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Uses the given HttpClient, username, and password to log into the
 * Economist. The client should be configured to accept cookies.
 * Returns the successful page.
 * 
 * This involves several steps:
 * <ul>
 * <li>Downloading the login form page
 * <li>POSTing that form to the appropriate URL, including the hidden fields
 *     in the form
 * <li>Verifying that the returned response indicates a successful login.
 * </ul>
 * 
 * This will just thrown a {@link HttpActionException} if the login fails, otherwise
 * it'll return the empty string.
 * @author bryanfeeney
 *
 */
public class LoginAction extends HttpAction {

	private static final int MAX_USER_EMAIL_DISPLAYABLE_CHARACTERS = 16;

	private final static Log LOG = LogFactory.getLog(LoginAction.class);
	
	private final static String
		NAME_FIELD          = "name",
		PASSWORD_FIELD      = "pass",
		STAY_LOGGED_FIELD   = "persistent_login",
		FORM_BUILD_ID_FIELD = "form_build_id",
		FORM_ID_FIELD       = "form_id",
		SECURE_LOGIN_URL_FIELD = "securelogin_original_baseurl";
	
	private final static String 
		FORM_ID_TAG          = "edit-user-login",
		SECURE_LOGIN_URL_TAG = "edit-securelogin-original-baseurl";

	private final static String LOGIN_FORM_TAG = "user-login";
	
	private final String username;
	private final String password;
	
	private final static URI LOGIN_PAGE;
	static {
		try {
			LOGIN_PAGE = new URI("https://www.economist.com/user");
		}
		catch (Exception e) {
			throw new IllegalStateException ("Can't convert the hardcoded login-page URL to a URI object : " + e.getMessage());
		}
	}
	
	public LoginAction(HttpClient client, String username, String password) {
		super(client);
		this.username = username;
		this.password = password;
	}

	@Override
	public String call() throws HttpActionException {
		// Download and parse the login page
		LOG.debug("Downloading the login page from " + LOGIN_PAGE);
		String formId, formBuildId = null, formSecureLogin;
		String loginPage = makeHttpRequest(LOGIN_PAGE, "https://www.economist.com");
		
		LOG.debug("Parsing the login " + loginPage.length() + "-character login page");
		Document document = Jsoup.parse(loginPage);

		formId          = document.getElementById(FORM_ID_TAG).attr("value");
		formSecureLogin = document.getElementById(SECURE_LOGIN_URL_TAG).attr("value");
		
		Element form = document.getElementById(LOGIN_FORM_TAG);
		URI postUri;
		try {
			postUri = new URI (form.attr("action"));
		}
		catch (URISyntaxException e) {
			throw new HttpActionException("The post-to URL in the downloaded form is not a valid URL : " + form.attr("action") + " : " + e.getMessage());
		}
		
		Element div = form.getElementsByTag("div").first();
		Elements inputs = div.getElementsByTag("input");
		for (Element input : inputs) {
			if (input.attr("name").equals(FORM_BUILD_ID_FIELD)) {
				formBuildId = input.attr("value");
				break;
			};
		}
		if (formBuildId == null)
			throw new HttpActionException("Couldn't access the form build ID value from the page at " + LOGIN_PAGE);

		
	    // Post the completed form
		LOG.debug("Posting the login details");
		String loggedInPage = makeHttpRequest (Method.POST, postUri, LOGIN_PAGE.toASCIIString(), 
				new BasicNameValuePair(NAME_FIELD, username),
				new BasicNameValuePair(PASSWORD_FIELD, password),
				new BasicNameValuePair(STAY_LOGGED_FIELD, "0"),
				new BasicNameValuePair(FORM_BUILD_ID_FIELD, formBuildId),
				new BasicNameValuePair(FORM_ID_FIELD, formId),
				new BasicNameValuePair(SECURE_LOGIN_URL_FIELD, formSecureLogin));
		
		System.out.println (loggedInPage);
		if (! loggedInPage.contains(StringUtils.left(username, MAX_USER_EMAIL_DISPLAYABLE_CHARACTERS)))
			throw new HttpActionException("Login failed");
		
		return "";
	}

	public static void main (String[] args) throws IOException, HttpActionException
	{	String password = Files.readAllLines(Paths.get("/Users/bryanfeeney/Desktop/eco.passwd")).get(0);
		
		HttpClient client = HttpClientBuilder.create()
						   .setRedirectStrategy(new LaxRedirectStrategy())
						   .build();
		System.out.println (new LoginAction (client, "bryan.feeney@gmail.com", password).call());
	}

}