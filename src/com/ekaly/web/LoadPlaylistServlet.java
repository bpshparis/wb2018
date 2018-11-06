package com.ekaly.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ekaly.tools.Tools;

/**
 * Servlet implementation class GetImportedKeysServlet
 */
@WebServlet("/LPL")
public class LoadPlaylistServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoadPlaylistServlet() {
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
			Path plPath = Paths.get(realPath + "/playlist");
			
			if(Files.exists(plPath)) {
				File[] fs = plPath.toFile().listFiles();
				
				Arrays.sort(fs, new Comparator<File>() {
				    public int compare(File f1, File f2) {
				        return Long.compare(f2.lastModified(), f1.lastModified());
				    }
				});			
				
				List<Map<String, String>> answer = new ArrayList<Map<String, String>>();
				for(File f: fs){
					Map<String, String> list = new HashMap<String, String>();
					list.put("name", f.getName());
					list.put("path", "playlist/" + f.getName());
					answer.add(list);
				}		
				result.put("ANSWER", answer);
				result.put("STATUS", "OK");
				
			}
			else {
        		result.put("ANSWER", "No playlist directory found.");
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

