package com.ekaly.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ekaly.tools.Tools;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneChatOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneChatScore;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.Utterance;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.UtteranceAnalyses;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.UtteranceAnalysis;

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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Map<String, Object> result = new HashMap<String, Object>();

		result.put("SESSIONID", request.getSession().getId());		
		result.put("CLIENT", request.getRemoteAddr() + ":" + request.getRemotePort());
		result.put("SERVER", request.getLocalAddr() + ":" + request.getLocalPort());
		result.put("FROM", this.getServletName());
		
		try{

			@SuppressWarnings("unchecked")
			Map<Long, Map<String, Object>> whoSaidWhatWhen = (Map<Long, Map<String, Object>>) request.getServletContext().getAttribute("whoSaidWhatWhen");
			
        	if(!whoSaidWhatWhen.isEmpty()) {
        		
	        	ToneAnalyzer ta = (ToneAnalyzer) request.getServletContext().getAttribute("ta");
	        	ToneChatOptions.Builder tcob = (ToneChatOptions.Builder) request.getServletContext().getAttribute("tcob");

	        	List<Utterance> us = new ArrayList<Utterance>();
	        	
	        	for(Map.Entry<Long, Map<String, Object>> obj: whoSaidWhatWhen.entrySet()){
	        	
	        		us.add(new Utterance.Builder()
	        				.text((String) obj.getValue().get("sentence"))
	        				.user((String) String.valueOf(obj.getValue().get("speaker")))
	        				.build());
	        	}
	        	
	        	ToneChatOptions tco = tcob
	        			.utterances(us)
	        			.build();

	        	UtteranceAnalyses analysis = ta.toneChat(tco).execute();
	        	
	        	if(analysis != null) {
	        		
	        		for(UtteranceAnalysis ua: analysis.getUtterancesTone()){
	        			Map<String, Object> obj = whoSaidWhatWhen.get(ua.getUtteranceId());
	        			
	        			List<ToneChatScore> tones = ua.getTones();
	        			Map<String, Double> tonesMap = new HashMap<String, Double>();
	        			
		        		if(tones != null) {
			        		for(ToneChatScore tone: tones) {
			        			tonesMap.put(tone.getToneId(), tone.getScore());
			        		}
		        		}	        			
	        			
	        			obj.put("tones", tonesMap);
	        		}
	        		
	        		result.put("ANSWER", whoSaidWhatWhen);
		        	result.put("STATUS", "OK");
	        		
	        	}
	        	else {
	        		result.put("ANSWER", "No valid UtteranceAnalyses object returned.");
	        		throw new Exception();
	        	}
        		
        	}
			
	        else {
        		result.put("ANSWER", "No valid Utterance object received.");
        		result.put("TROUBLESHOOTING", "Have a look at: http://watson-developer-cloud.github.io/java-sdk/docs/java-sdk-6.0.0/com/ibm/watson/developer_cloud/tone_analyzer/v3/model/Utterance.html");
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

