package com.ekaly.web;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.commons.lang3.StringUtils;

import com.ekaly.tools.Cmd;
import com.ekaly.tools.Tools;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneChatOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneOptions;

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
	ToneAnalyzer ta;
	ToneOptions.Builder tob;
	ToneChatOptions.Builder tcob;
	SpeechToText s2t;
	RecognizeOptions.Builder rob;
	List<Map<String, Object>> historical = new ArrayList<Map<String, Object>>();
	
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
    			
    			vcap_services = initVCAP_SERVICES();
    			if(vcap_services != null) {
    				if(!vcap_services.trim().isEmpty()) {
    					System.out.println("VCAP_SERVICES has been initialized...");
    				}
    			}

    			initTA();
    			System.out.println("TA has been initialized...");
				arg0.getServletContext().setAttribute("ta", ta);
				arg0.getServletContext().setAttribute("tob", tob);
				arg0.getServletContext().setAttribute("tcob", tcob);
				
				initS2T();
    			System.out.println("S2T has been initialized...");
				arg0.getServletContext().setAttribute("s2t", s2t);
				arg0.getServletContext().setAttribute("rob", rob);
				
				arg0.getServletContext().setAttribute("historical", historical);
    			
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
    
    public String initVCAP_SERVICES() throws FileNotFoundException, IOException{
    	
    	String value = props.getProperty("VCAP_SERVICES");
    	
    	if(value != null && !value.trim().isEmpty()){
			Path path = Paths.get(realPath + value);
			Charset charset = StandardCharsets.UTF_8;
			if(Files.exists(path)){
				System.out.println("VCAP_SERVICES read from " + value + ".");
				return new String(Files.readAllBytes(path), charset);
			}
    	}
    	else{
    		vcap_services = System.getenv("VCAP_SERVICES");
			System.out.println("VCAP_SERVICES System ENV=" + vcap_services);
    		return vcap_services;
    	}
    	
    	return null;

    }
    
    @SuppressWarnings({ "unchecked" })
	public void initTA() throws JsonParseException, JsonMappingException, IOException, InterruptedException{

		
		String url = "";
		String username = "";
		String password = "";
		String version = props.getProperty("TA_VERSION");
    	String serviceName = props.getProperty("TA_NAME");
		
		if(vcap_services != null && !vcap_services.trim().isEmpty()) {
	    	
			Map<String, Object> input = Tools.fromJSON(vcap_services);
			
			List<Map<String, Object>> l0s = (List<Map<String, Object>>) input.get(serviceName);
			
			for(Map<String, Object> l0: l0s){
				for(Map.Entry<String, Object> e: l0.entrySet()){
					if(e.getKey().equalsIgnoreCase("credentials")){
						System.out.println(e.getKey() + "=" + e.getValue());
						Map<String, Object> credential = (Map<String, Object>) e.getValue();
						url = (String) credential.get("url");
						username = (String) credential.get("username");
						password = (String) credential.get("password");
						System.out.println(serviceName + " service key set by VCAP_SERVICES");
					}
				}
			}
		}
		else {
			Path path = Paths.get(realPath + (String) props.getProperty("TA_KEY_CMD"));

			if(Files.exists(path)){
				
				Cmd cmd = (Cmd) Tools.fromJSON(path.toFile(), new TypeReference<Cmd>(){});
				
				if(cmd != null) {
					Map<String, Object> key = Tools.fromJSON((String) cmd.run().get("OUTPUT"));
					username = (String) key.get("username");
					password = (String) key.get("password");
					url = (String) key.get("url");
					System.out.println(serviceName + " service key set by " + cmd.getName());
				}
			}
		}
		
		if(StringUtils.isNoneEmpty(url, username, password)){
		
			ta = new ToneAnalyzer(version, username, password);
			ta.setEndPoint(url);
	
			try {
				tob = new ToneOptions.Builder()
						  .contentLanguage(props.getProperty("TA_CONTENT_LANGUAGE"))
						  .sentences(Boolean.valueOf(props.getProperty("TA_SENTENCES")))
						  .acceptLanguage(props.getProperty("TA_ACCEPT_LANGUAGE"));
				
			}
			catch(Exception e) {
				System.err.println("Warning: ToneOptions tob was not build successfully !!!");
			}
			
			try {
				tcob = new ToneChatOptions.Builder()
						.contentLanguage(props.getProperty("TA_CONTENT_LANGUAGE"))
						.acceptLanguage(props.getProperty("TA_ACCEPT_LANGUAGE"));
				
			}
			catch(Exception e) {
				System.err.println("Warning: ToneChatOptions tcob was not build successfully !!!");
			}
			
		}	

		System.out.println(ta.getName() + " " + ta.getEndPoint());
		System.out.println(tob);
		System.out.println(tcob);
		
		return;
    }    

	@SuppressWarnings("unchecked")
	public void initS2T() throws JsonParseException, JsonMappingException, IOException, InterruptedException{

		
		String url = "";
		String username = "";
		String password = "";
		String apikey = "";
    	String serviceName = props.getProperty("S2T_NAME");
		
		if(vcap_services != null && !vcap_services.trim().isEmpty()) {
	    	
			Map<String, Object> input = Tools.fromJSON(vcap_services);
			
			List<Map<String, Object>> l0s = (List<Map<String, Object>>) input.get(serviceName);
			
			for(Map<String, Object> l0: l0s){
				for(Map.Entry<String, Object> e: l0.entrySet()){
					if(e.getKey().equalsIgnoreCase("credentials")){
						System.out.println(e.getKey() + "=" + e.getValue());
						Map<String, Object> credential = (Map<String, Object>) e.getValue();
						url = (String) credential.get("url");
						username = (String) credential.get("username");
						password = (String) credential.get("password");
						apikey = (String) credential.get("apikey");
						System.out.println(serviceName + " service key set by VCAP_SERVICES");
					}
				}
			}
		}
		else {
			Path path = Paths.get(realPath + (String) props.getProperty("S2T_KEY_CMD"));

			if(Files.exists(path)){
				
				Cmd cmd = (Cmd) Tools.fromJSON(path.toFile(), new TypeReference<Cmd>(){});
				
				if(cmd != null) {
					Map<String, Object> key = Tools.fromJSON((String) cmd.run().get("OUTPUT"));
					username = (String) key.get("username");
					password = (String) key.get("password");
					apikey = (String) key.get("apikey");
					url = (String) key.get("url");
					System.out.println(serviceName + " service key set by " + cmd.getName());
				}
			}
		}
		
		if(StringUtils.isNoneEmpty(url, username, password)){
		
			s2t = new SpeechToText(username, password);
			s2t.setEndPoint(url);
			
		}	

		if(StringUtils.isNoneEmpty(url, apikey)){

			IamOptions options = new IamOptions.Builder()
					  .apiKey(apikey)
					  .build();

			s2t = new SpeechToText(options);
			s2t.setEndPoint(url);
			
		}	
		
		try {
			rob = new RecognizeOptions.Builder()
					.contentType(props.getProperty("S2T_CONTENT_TYPE"))
					.speakerLabels(true)
					.model(props.getProperty("S2T_MODEL"));
			
		}
		catch(Exception e) {
			System.err.println("Warning: RecognizeOptions rob was not build successfully !!!");
		}
		
		System.out.println(s2t.getName() + " " + s2t.getEndPoint());
		System.out.println(rob);
		
		return;
    }    
    
}
