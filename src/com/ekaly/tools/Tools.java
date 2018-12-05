package com.ekaly.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Tools {
	
	@SuppressWarnings("unchecked")
	public final static Map<String, String> getCredentialFromVS(String serviceName){
		
		Map<String, String> result = new HashMap<String, String>();
		
		try {
			Map<String, Object> input = Tools.fromJSON(System.getenv("VCAP_SERVICES"));
			
			List<Map<String, Object>> l0s = (List<Map<String, Object>>) input.get(serviceName);
			
			for(Map<String, Object> l0: l0s){
				for(Map.Entry<String, Object> e: l0.entrySet()){
					if(e.getKey().equalsIgnoreCase("credentials")){
						Map<String, Object> credential = (Map<String, Object>) e.getValue();
						result.put("url", (String) credential.get("url"));
						result.put("apikey", (String) credential.get("apikey"));
					}
				}
			}	
			return result;
		}
		catch(Exception e) {
			e.printStackTrace(System.err);
		}
		return null;
	}
	

	public final static String toJSON(Object o){
		try{
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		StringWriter sw = new StringWriter();
		String jsonResult = null;
		mapper.writeValue(sw, o);
		sw.flush();
		jsonResult = sw.toString();
		sw.close();
		return jsonResult;
		}
		catch(Exception e){
			e.printStackTrace(System.err);
		}
		return null;
	}
	
	public final static List<Map<String, Object>> fromJSON2ML(InputStream is){
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		
		try{
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			mapList = mapper.readValue(br, new TypeReference<List<Map<String, Object>>>(){});
			return mapList;
		}
		catch(Exception e){
			e.printStackTrace(System.err);
		}
		
        return null;
	}	
	
	public final static List<Map<String, Object>> fromJSON2ML(File file) throws FileNotFoundException{
		return fromJSON2ML(new FileInputStream(file));
	}
	
	public final static List<Map<String, Object>> fromJSON2ML(String string) throws FileNotFoundException{
		return fromJSON2ML(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
	}
	
	public final static Map<String, Object> fromJSON(InputStream is){
		Map<String, Object>	map = new HashMap<String, Object>();
		
		try{
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			map = mapper.readValue(br, new TypeReference<Map<String, Object>>(){});
			return map;
		}
		catch(Exception e){
			e.printStackTrace(System.err);
		}
		
        return null;
	}
	
	public final static Map<String, Object> fromJSON(File file) throws FileNotFoundException{
		return fromJSON(new FileInputStream(file));
	}

	public final static Map<String, Object> fromJSON(String string){
		return fromJSON(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
	}
	
	public final static <T> Object fromJSON(InputStream is, TypeReference<T> t){
		
		try{
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			ObjectMapper mapper = new ObjectMapper();
	        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			return mapper.readValue(br, t);		
		}
		catch(Exception e){
			e.printStackTrace(System.err);
		}
		
        return null;
	}

	public final static <T> Object fromJSON(File file, TypeReference<T> t) throws FileNotFoundException{
		return fromJSON(new FileInputStream(file), t);
	}
	
	public final static <T> Object fromJSON(String string, TypeReference<T> t) throws FileNotFoundException{
		return fromJSON(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)), t);
	}	

	
	public final static Map<Long, Map<String, Object>> whoSaidWhatWhen(File file) throws FileNotFoundException{
		return whoSaidWhatWhen(new FileInputStream(file));
	}

	public final static Map<Long, Map<String, Object>> whoSaidWhatWhen(String string){
		return whoSaidWhatWhen(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
	}
	
	public final static Map<Long, Map<String, Object>> whoSaidWhatWhen (InputStream is) {
		
		try {

			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	        Map<String, Object> json = mapper.readValue(br, new TypeReference<Map<String, Object>>(){});
			
			@SuppressWarnings("unchecked")
			List<Map<String, List<Map<String, List<Object>>>>> results = (List<Map<String, List<Map<String, List<Object>>>>>) json.get("results");
	
			Map<Double, Timestamp> timestampMap = new HashMap<Double, Timestamp>();
			
			for(Map<String, List<Map<String, List<Object>>>> result: results) {
				List<Object> os = result.get("alternatives").get(0).get("timestamps"); 
				
				for(Object o: os) {
					Timestamp ts = new Timestamp();
					List<String> l = mapper.readValue(Tools.toJSON(o), new TypeReference<List<String>>(){});
					ts.setWord(l.get(0));
					ts.setFrom(Double.parseDouble(l.get(1)));
					ts.setTo(Double.parseDouble(l.get(2)));
					timestampMap.put(Double.parseDouble(l.get(1)), ts);
				}
			}
			
			Map<Double, Speaker> speakerMap = new HashMap<Double, Speaker>();
			
			@SuppressWarnings("unchecked")
			List<Speaker> speakers = (List<Speaker>) Tools.fromJSON(Tools.toJSON(json.get("speaker_labels")), new TypeReference<List<Speaker>>(){});
			
			for(Speaker speaker: speakers) {
				double key = speaker.getFrom();
				String word = timestampMap.get(key).getWord();
				speaker.setWord(word);
				speakerMap.put(speaker.getFrom(), speaker);
			}
			
			Map<Double, Speaker> speakerMapSorted = (Map<Double, Speaker>) speakerMap.entrySet().stream()
					.sorted(Map.Entry.comparingByKey())
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			
			Map<Long, Map<String, Object>> whoSaidWhatWhen = new HashMap<Long, Map<String, Object>>();
			
			StringBuffer sb = new StringBuffer();
			int curSpeaker = -1;
			double when = 0D;
			long i = 0L;
			
			for(Map.Entry<Double, Speaker> map: speakerMapSorted.entrySet()) {
				int speaker = map.getValue().getSpeaker();
				if(curSpeaker == -1) {
					curSpeaker = map.getValue().getSpeaker();
					when = map.getValue().getFrom();
				}

				if(curSpeaker != speaker) {
					Map<String, Object> whoSW = new HashMap<String, Object>();
					whoSW.put("when", when);
					whoSW.put("speaker", curSpeaker);
					whoSW.put("sentence", sb.toString().trim());
					whoSaidWhatWhen.put(i++, whoSW);
					when = map.getValue().getFrom();
					sb = new StringBuffer();
				}

				curSpeaker = speaker;
				
				sb.append(map.getValue().getWord() + " ");
				
			}
		
			Map<String, Object> end = new HashMap<String, Object>();
			end.put("when", when);
			end.put("speaker", curSpeaker);
			end.put("sentence", sb.toString().trim());
			
			whoSaidWhatWhen.put(i, end);
			
			Map<Long, Map<String, Object>> whoSaidWhatWhenSorted = (Map<Long, Map<String, Object>>) whoSaidWhatWhen.entrySet().stream()
					.sorted(Map.Entry.comparingByKey())
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			
			return whoSaidWhatWhenSorted;
		}
		catch(Exception e) {
			e.printStackTrace(System.err);
		}
		
		return null;
	}
	
}
