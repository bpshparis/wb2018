package com.ekaly.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ekaly.tools.Tools;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;

/**
 * Servlet implementation class GetImportedKeysServlet
 */
@WebServlet("/RA")
public class RecognizeAudioServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RecognizeAudioServlet() {
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

			Path realPath = Paths.get(getServletContext().getRealPath("/"));
			Path soundsPath = Paths.get(realPath + "/sounds");
			Path soundFile = Paths.get(soundsPath + "/sound.mp3");
			
			if(!Files.exists(soundFile)) {
				result.put("ANSWER", soundFile.toString() + " not found.");
        		result.put("TROUBLESHOOTING", "Have a look at: http://watson-developer-cloud.github.io/java-sdk/docs/java-sdk-6.0.0/com/ibm/watson/developer_cloud/speech_to_text/v1/model/RecognizeOptions.html");
        		throw new Exception();				
			}
			
			SpeechToText s2t = (SpeechToText) request.getServletContext().getAttribute("s2t");
			RecognizeOptions.Builder rob = (RecognizeOptions.Builder) request.getServletContext().getAttribute("rob");
			
			RecognizeOptions ro = rob
					.audio(soundFile.toFile())
					.build();
			
			SpeechRecognitionResults analysis = s2t.recognize(ro).execute();

//			Path analysis = Paths.get("/opt/wks/wb2018/res/s2t0.resp.sound1.json");			
			
        	if(analysis != null) {
        		
        		Map<Long, Map<String, Object>> whoSaidWhatWhen = Tools.whoSaidWhatWhen(analysis.toString());
        		System.out.println(Tools.toJSON(whoSaidWhatWhen));
        		request.getServletContext().setAttribute("whoSaidWhatWhen", whoSaidWhatWhen);
    			result.put("STATUS", "OK");
        		result.put("ANSWER", Tools.toJSON(whoSaidWhatWhen));
        		
        	}
        	else {
        		result.put("ANSWER", "No valid SpeechRecognitionResults object returned.");
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

