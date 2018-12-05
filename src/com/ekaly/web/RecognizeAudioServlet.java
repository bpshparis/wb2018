package com.ekaly.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
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
			Path soundPath = Paths.get(soundsPath + "/sound.mp3");
			System.out.println("Files.size(soundPath)=" + Files.size(soundPath));
			
			if(!Files.exists(soundPath)) {
				result.put("ANSWER", soundPath.toString() + " not found.");
        		result.put("TROUBLESHOOTING", "Have a look at: https://console.bluemix.net/apidocs/speech-to-text");
        		throw new Exception();				
			}
			
			OkHttpClient s2t = (OkHttpClient) request.getServletContext().getAttribute("s2t");
			Request.Builder s2trb = (Request.Builder) request.getServletContext().getAttribute("s2trb");
			Properties props = (Properties) request.getServletContext().getAttribute("props");
			
//			RequestBody body = new MultipartBody.Builder()
//			.setType(MultipartBody.FORM)
//	        .addFormDataPart("file", soundPath.toString(), RequestBody.create(MediaType.parse(props.getProperty("S2T_CONTENT_TYPE")), soundPath.toFile()))
//	        .build();
			
			RequestBody body = RequestBody.create(MediaType.parse(props.getProperty("S2T_CONTENT_TYPE")), Files.readAllBytes(soundPath));
			
			Request s2tr = s2trb
				.post(body)
				.build();
			
			Response s2tResponse = s2t.newCall(s2tr).execute();

			String analysis = s2tResponse.body().string();
			
        	if(analysis != null) {
        		
        		Map<Long, Map<String, Object>> whoSaidWhatWhen = Tools.whoSaidWhatWhen(analysis.toString());
        		request.getServletContext().setAttribute("whoSaidWhatWhen", whoSaidWhatWhen);
    			result.put("STATUS", "OK");
        		result.put("ANSWER", Tools.toJSON(whoSaidWhatWhen));
        		
        	}
        	else {
        		result.put("ANSWER", "No valid object returned.");
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

