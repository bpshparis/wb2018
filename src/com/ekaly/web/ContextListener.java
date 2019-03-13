package com.ekaly.web;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.commons.lang3.StringUtils;

import com.ekaly.tools.Resource;
import com.ekaly.tools.BasicAuthInterceptor;
import com.ekaly.tools.Tools;
import com.ekaly.tools.UnsafeOkHttpClient;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Application Lifecycle Listener implementation class ContextListener
 *
 */
@WebListener
public class ContextListener implements ServletContextListener {

	InitialContext ic;
	String vcap_services;
	String realPath;
	Properties props = new Properties();
	OkHttpClient ta;
	Request.Builder tarb;
	OkHttpClient s2t;
	Request.Builder s2trb;
	List<Resource> resources;	
	
    /**
     * Default constructor. 
     */
    public ContextListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0)  { 
         // TODO Auto-generated method stub
       	try {
       		
    			ic = new InitialContext();
    			arg0.getServletContext().setAttribute("ic", ic);
    			realPath = arg0.getServletContext().getRealPath("/"); 
    	    	props.load(new FileInputStream(realPath + "/res/conf.properties"));
    			arg0.getServletContext().setAttribute("props", props);
    	    	
    			System.out.println("Context has been initialized...");
    			
    			initVCAP_SERVICES();
    			System.out.println("VCAP_SERVICES has been initialized...");
    			
    			initTA();
    			if(ta != null && tarb != null) {
	    			System.out.println("TA has been initialized...");
					arg0.getServletContext().setAttribute("ta", ta);
					arg0.getServletContext().setAttribute("tarb", tarb);
    			}

    			initS2T();
    			if(s2t != null && s2trb != null) {
	    			System.out.println("S2T has been initialized...");
					arg0.getServletContext().setAttribute("s2t", s2t);
					arg0.getServletContext().setAttribute("s2trb", s2trb);
    			}
    			
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}    	
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0)  { 
         // TODO Auto-generated method stub
    	arg0.getServletContext().removeAttribute("ic");
		System.out.println("Context has been destroyed...");    	
    }
    
    @SuppressWarnings("unchecked")
	public void initVCAP_SERVICES() throws FileNotFoundException, IOException{
    	
		vcap_services = System.getenv("VCAP_SERVICES");
		System.out.println("VCAP_SERVICES read from System ENV.");

		System.out.println("vcap_services=" + vcap_services);
		Map<String, Resource> json = (Map<String, Resource>) Tools.fromJSON(vcap_services, new TypeReference<Map<String, Resource>>(){});
		resources = Arrays.asList(json.values().toArray(new Resource[0]));

    }
    
    
	public void initTA() throws JsonParseException, JsonMappingException, IOException, InterruptedException{

    	String serviceName = props.getProperty("TA_NAME");
    	
		String url = "";
		String username = "apikey";
		String password = "";
		
		for(Resource resource: resources) {
			if(resource.getService().equalsIgnoreCase(serviceName)) {
				password = resource.getCredentials().get(0).getApikey();
				url = resource.getCredentials().get(0).getUrl();
			}
		}
		
		
		if(StringUtils.isNoneEmpty(url, password)){
			
			try {
			
				ta = new UnsafeOkHttpClient().getUnsafeOkHttpClient().newBuilder()
						.addInterceptor(new BasicAuthInterceptor(username, password))
					    .build();
					
				HttpUrl.Builder urlBuilder = HttpUrl.parse(url + props.getProperty("TA_TONE_CHAT_METHOD")).newBuilder();
				urlBuilder.addQueryParameter("version", props.getProperty("TA_VERSION"));
				
				tarb = new Request.Builder()
					.addHeader("Content-Language", props.getProperty("TA_CONTENT_LANGUAGE"))
					.addHeader("Accept-Language", props.getProperty("TA_ACCEPT_LANGUAGE"))
					.addHeader("Content-Type", props.getProperty("TA_CONTENT_TYPE"))
					.url(urlBuilder.toString());		
			}
			catch(Exception e) {
				System.err.println("Warning: OkHttpClient ta was not build successfully !!!");
				e.printStackTrace(System.err);
			}
		}
	}    
    
	public void initS2T() throws JsonParseException, JsonMappingException, IOException, InterruptedException{

    	String serviceName = props.getProperty("S2T_NAME");
    	
		String url = "";
		String username = "apikey";
		String password = "";
		
		for(Resource resource: resources) {
			if(resource.getService().equalsIgnoreCase(serviceName)) {
				password = resource.getCredentials().get(0).getApikey();
				url = resource.getCredentials().get(0).getUrl();
			}
		}
    	
		if(StringUtils.isNoneEmpty(url, password)){
			
			try {
			
				s2t = new UnsafeOkHttpClient().getUnsafeOkHttpClient().newBuilder()
						.addInterceptor(new BasicAuthInterceptor(username, password))
					    .build();
					
				HttpUrl.Builder urlBuilder = HttpUrl.parse(url + props.getProperty("S2T_RECOGNIZE_METHOD")).newBuilder();
				urlBuilder.addQueryParameter("model", props.getProperty("S2T_MODEL"));
				urlBuilder.addQueryParameter("speaker_labels", props.getProperty("S2T_SPEAKER_LABELS"));
				
				s2trb = new Request.Builder()
					.addHeader("Content-Type", props.getProperty("S2T_CONTENT_TYPE"))
					.addHeader("Transfer-Encoding", props.getProperty("S2T_TRANSFER_ENCODING"))		
					.url(urlBuilder.toString());		
			}
			catch(Exception e) {
				System.err.println("Warning: OkHttpClient s2t was not build successfully !!!");
				e.printStackTrace(System.err);
			}
		}
	}    
    
}
