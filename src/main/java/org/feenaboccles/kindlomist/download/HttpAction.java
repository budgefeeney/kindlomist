package org.feenaboccles.kindlomist.download;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
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
 * Performs some HTTP action using {@link #makeHttpRequest(URI, Optional)},
 * {@link #makeBinaryHttpRequest(URI, Optional)} and their overloaded variants.
 * the response a string.
 */
public abstract class HttpAction
{
	private static final int HTTP_200_OK = 200;

	public enum Method {
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
	protected String makeHttpRequest(URI url, Optional<URI> referrerUrl) throws HttpActionException {
		return makeHttpRequestWithUnvalidatedRef (url, referrerUrl.map(URI::toASCIIString));
	}
	
	/**
	 * Convenience method to create and execute a HTTP request
	 * and return the given response body. The name comes from the fact
	 * that by using a string for the referrer URL parameter there's
	 * no guarantee that it actuall contains a valid URL.
	 */
	protected String makeHttpRequestWithUnvalidatedRef(URI url, Optional<String> referrerUrl) throws HttpActionException {
		try {
			return EntityUtils.toString (makeHttpRequest (Method.GET, url, referrerUrl));
		}
		catch (IOException e) {
			throw new HttpActionException ("Can't convert web-content to a string : " + e.getMessage(), e);
		}
	}
	
	
	/**
	 * Convenience method to create and execute a HTTP request
	 * and return the given response body.
	 */
	protected byte[] makeBinaryHttpRequest(URI url, Optional<URI> referrerUrl) throws HttpActionException {
		return makeBinaryHttpRequestWithUnvalidatedRef(url, referrerUrl.map(URI::toASCIIString));
	}
	
	/**
	 * Convenience method to create and execute a HTTP request
	 * and return the given response body. The name comes from the
	 * fact that the use of strings means that the referrerUrl
	 * parameter may not represent a valid URL
	 */
	protected byte[] makeBinaryHttpRequestWithUnvalidatedRef(URI url, Optional<String> referrerUrl) throws HttpActionException {
		try {
			return EntityUtils.toByteArray(makeHttpRequest (Method.GET, url, referrerUrl));
		}
		catch (IOException e) {
			throw new HttpActionException ("Can't convert web-content to a byte-array : " + e.getMessage(), e);
		}
	}
	
	
	
	/**
	 * Convenience method to create and execute a HTTP request
	 * and return the given response body.
	 */
	protected HttpEntity makeHttpRequest(Method method, URI url, Optional<String> referrerUrl, NameValuePair... params) throws HttpActionException {
		final RequestBuilder reqBldr = defaultRequestBuilder(method, url);
	    
	    referrerUrl.ifPresent(r -> reqBldr.addHeader(new BasicHeader("Referer", r)));
	    if (params.length > 0) {
	    	if (method != Method.POST)
	    		throw new IllegalArgumentException("Can only specify name-value pairs for POST actions.");
	    	reqBldr.setEntity(new UrlEncodedFormEntity(Arrays.asList(params), Consts.UTF_8));
	    }
	    
	    try  {
	    	HttpUriRequest req  = reqBldr.build();
		    HttpResponse   resp = client.execute(req);
		    
		    int respStatusCode = resp.getStatusLine().getStatusCode();
		    if (respStatusCode != HTTP_200_OK)
		      throw new HttpActionException ("Failed to download page " + url + ", received HTTP response code " + respStatusCode);
		            
		    return resp.getEntity();
	    }
	    catch (IOException ioe) {
	    	throw new HttpActionException ("Couldn't access resource on the web at " + url + " : " + ioe.getMessage(), ioe);
	    }
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
}
