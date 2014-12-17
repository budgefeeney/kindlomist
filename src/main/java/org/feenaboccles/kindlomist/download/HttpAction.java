package org.feenaboccles.kindlomist.download;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

/**
 * Performs some HTTP action using {@link #call()}, optionally returns 
 * the response a string.
 */
public abstract class HttpAction implements Callable<String>
{
	private static final int HTTP_200_OK = 200;

	public static enum Method {
		GET, POST
	}
	
	protected final HttpClient client;
	protected final RequestConfig reqConfig;
	
	public HttpAction (HttpClient client) {	
		this.client = client;
		this.reqConfig = RequestConfig.copy(RequestConfig.DEFAULT)
				.setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
				.setRedirectsEnabled(true)
				.setRelativeRedirectsAllowed(true)
				.setMaxRedirects(10)
				.build();
	}
	
	
	/**
	 * Convenience method to create and execute a HTTP request
	 * and return the given response body.
	 */
	protected String makeHttpRequest(URI url, URI referrerUrl) throws HttpActionException {
		return makeHttpRequest (url, referrerUrl.toASCIIString());
	}
	
	/**
	 * Convenience method to create and execute a HTTP request
	 * and return the given response body.
	 */
	protected String makeHttpRequest(URI url, String referrerUrl) throws HttpActionException {
		return makeHttpRequest (Method.GET, url, referrerUrl);
	}
	
	/**
	 * Convenience method to create and execute a HTTP request
	 * and return the given response body.
	 */
	protected String makeHttpRequest(Method method, URI url, String referrerUrl, NameValuePair... params) throws HttpActionException {
		final RequestBuilder reqBldr = defaultRequestBuilder(method, url);
	    
	    if (! StringUtils.isBlank(referrerUrl))
	    	reqBldr.addHeader(new BasicHeader("Referer", referrerUrl));
	    if (params.length > 0) {
	    	if (method != Method.POST)
	    		throw new IllegalArgumentException("Can only specify name-value pairs for POST actions.");
	    	reqBldr.setEntity(new UrlEncodedFormEntity(Arrays.asList(params), Consts.UTF_8));
	    }
	    
	    final String responseBody;
	    try  {
	    	HttpUriRequest req  = reqBldr.build();
		    HttpResponse   resp = client.execute(req);
		    
		    int respStatusCode = resp.getStatusLine().getStatusCode();
		    if (respStatusCode != HTTP_200_OK)
		      throw new HttpActionException ("Failed to download page " + url + ", received HTTP response code " + respStatusCode);
		            
		    responseBody = EntityUtils.toString(resp.getEntity());
	    }
	    catch (IOException ioe) {
	    	throw new HttpActionException ("Couldn't access resource on the web at " + url + " : " + ioe.getMessage(), ioe);
	    }

	    
	    return responseBody;
	  }

	/**
	 * Creates a request builder which will build a request designed to maximally
	 * resemble a real web-browser
	 */
	private RequestBuilder defaultRequestBuilder(Method method, URI url) {
		final RequestBuilder reqBldr;
		switch (method) {
		case GET: 
			reqBldr = RequestBuilder.get();
		    break;
		case POST:
			reqBldr = RequestBuilder.post();
			break;
		default:
			throw new IllegalArgumentException ("Unknown HTTP method " + method);
		}
		
		reqBldr.setConfig(reqConfig)
				.addHeader(new BasicHeader("Accept-Charset", "utf-8"))
			    .addHeader(new BasicHeader("Accept-Language", "en-US,en;q=0.8"))
			    .addHeader(new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"))
			    .addHeader(new BasicHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) AppleWebKit/536.30.1 (KHTML, like Gecko) Version/6.0.5 Safari/536.30.1"))
			    .setUri(url);
		return reqBldr;
	}
	
	/**
	 * Executes this action
	 */
	public abstract String call() throws HttpActionException;
}
