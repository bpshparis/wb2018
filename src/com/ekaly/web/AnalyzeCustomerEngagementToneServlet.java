package com.ekaly.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ekaly.tools.Tools;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Servlet implementation class GetImportedKeysServlet
 */
@WebServlet("/ACET")
public class AnalyzeCustomerEngagementToneServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AnalyzeCustomerEngagementToneServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Map<String, Object> result = new HashMap<String, Object>();

		result.put("SESSIONID", request.getSession().getId());		
		result.put("CLIENT", request.getRemoteAddr() + ":" + request.getRemotePort());
		result.put("SERVER", request.getLocalAddr() + ":" + request.getLocalPort());
		result.put("FROM", this.getServletName());
		
		try{

			Map<Long, Map<String, Object>> whoSaidWhatWhen = (Map<Long, Map<String, Object>>) request.getServletContext().getAttribute("whoSaidWhatWhen");
			
        	if(!whoSaidWhatWhen.isEmpty()) {
        		
	        	OkHttpClient ta = (OkHttpClient) request.getServletContext().getAttribute("ta");
	        	Request.Builder tarb = (Request.Builder) request.getServletContext().getAttribute("tarb");
				Properties props = (Properties) request.getServletContext().getAttribute("props");

	        	Map<String, List<Map<String, String>>> utterances = new HashMap<String, List<Map<String, String>>>();
	        	List<Map<String, String>> sentences = new ArrayList<Map<String, String>>();
	        	
	        	for(Map.Entry<Long, Map<String, Object>> obj: whoSaidWhatWhen.entrySet()){
	        		Map<String, String> sentence = new HashMap<String, String>();
	        		sentence.put("text", (String) obj.getValue().get("sentence"));
	        		sentence.put("user", (String) String.valueOf(obj.getValue().get("speaker")));
	        		sentences.add(sentence);
	        	}
	        	
	        	utterances.put("utterances", sentences);

	    		RequestBody body = RequestBody.create(MediaType.parse(props.getProperty("TA_CONTENT_TYPE")), Tools.toJSON(utterances).getBytes());		
	    		
	    		Request tar = tarb
	    			.post(body)
	    			.build();
	    		
	    		Response taResponse = ta.newCall(tar).execute();
	        	
	    		String analysis = taResponse.body().string();
	    		
	        	if(analysis != null) {
	        		
	        		List<Map<String, Object>> ust = (List<Map<String, Object>>) Tools.fromJSON(analysis).get("utterances_tone"); 

	        		for(Map<String, Object> ut: ust) {

	        			Long id = Long.parseLong(String.valueOf((Integer) ut.get("utterance_id")));
	        			Map<String, Object> obj = whoSaidWhatWhen.get(id);
	        			
	        			List<Map<String, Object>> tones = new ArrayList<Map<String, Object>>();
	        			tones = (List<Map<String, Object>>) ut.get("tones");
	        			
	        			Map<String, Double> tonesMap = new HashMap<String, Double>();
	        			
	            		if(tones != null) {
	                		for(Map<String, Object> tone: tones) {
	                			tonesMap.put((String) tone.get("tone_id"), (double)tone.get("score"));
	                		}
	            		}	        			
	        			
	            		if(obj != null) {
	            			obj.put("tones", tonesMap);
	            			obj.put("_tones", tones);
	            		}

	        		}
	        		
	        		result.put("ANSWER", whoSaidWhatWhen);
		        	result.put("STATUS", "OK");
	        		
	        	}
	        	else {
	        		result.put("ANSWER", "No valid object returned.");
	        		throw new Exception();
	        	}
        		
        	}
			
	        else {
        		result.put("ANSWER", "No valid object received.");
        		result.put("TROUBLESHOOTING", "Have a look at: https://console.bluemix.net/apidocs/tone-analyzer");
        		throw new Exception();
	        }
	        
		}
		catch(Exception e){
			result.put("STATUS", "KO");
            result.put("EXCEPTION", e.getClass().getName());
            result.put("MESSAGE", e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            result.put("STACKTRACE", sw.toString());
            e.printStackTrace(System.err);
		}			
		
		finally {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(Tools.toJSON(result));
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

