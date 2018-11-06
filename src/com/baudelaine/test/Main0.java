package com.baudelaine.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.baudelaine.tools.Tools;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class Main0 {

	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		// TODO Auto-generated method stub
		
		Path path = Paths.get("/opt/code/wb2018/res/s2t0.resp2.json");
		
		Map<String, Object> json = Tools.fromJSON(path.toFile());

		System.out.println(json.size());
		
		@SuppressWarnings("unchecked")
		List<Map<String, List<Map<String, String>>>> results = (List<Map<String, List<Map<String, String>>>>) json.get("results");
	
		for(Map<String, List<Map<String, String>>> result: results) {
			System.out.println(result.get("alternatives").get(0).get("transcript"));
		}
		
	}

}
