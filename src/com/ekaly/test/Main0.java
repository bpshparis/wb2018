package com.ekaly.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.ekaly.tools.Speaker;
import com.ekaly.tools.Timestamp;
import com.ekaly.tools.Tools;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main0 {

	
	
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		// TODO Auto-generated method stub
		
		
		Path path = Paths.get("/opt/wks/wb2018/res/s2t0.resp.sound1.json");
		
		Map<String, Object> json = Tools.fromJSON(path.toFile());

		ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
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

//		System.out.println(Tools.toJSON(timestampMap));
		
		Map<Double, Speaker> speakerMap = new HashMap<Double, Speaker>();
		
		@SuppressWarnings("unchecked")
		List<Speaker> speakers = (List<Speaker>) Tools.fromJSON(Tools.toJSON(json.get("speaker_labels")), new TypeReference<List<Speaker>>(){});
		
		for(Speaker speaker: speakers) {
			double key = speaker.getFrom();
			String word = timestampMap.get(key).getWord();
			speaker.setWord(word);
			speakerMap.put(speaker.getFrom(), speaker);
		}
		
//		System.out.println(Tools.toJSON(speakerMap));
		
		Map<Double, Speaker> speakerMapSorted = (Map<Double, Speaker>) speakerMap.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		
//		System.out.println(Tools.toJSON(speakerMapSorted));
		
//		Map<Integer, List<String>> whoSaidWhat = new HashMap<Integer, List<String>>();
//		
//		StringBuffer sb = new StringBuffer();
//		int curSpeaker = -1;
//		
//		for(Map.Entry<Double, Speaker> map: speakerMapSorted.entrySet()) {
//			int speaker = map.getValue().getSpeaker();
//			if(!whoSaidWhat.containsKey(speaker)){
//				whoSaidWhat.put(speaker, new ArrayList<String>());
//			}
//			if(curSpeaker == -1) {
//				curSpeaker = map.getValue().getSpeaker();
//			}
//			
//			if(curSpeaker != speaker) {
//				whoSaidWhat.get(curSpeaker).add(sb.toString().trim());
//				sb = new StringBuffer();
//			}
//
//			curSpeaker = speaker;
//			
//			sb.append(map.getValue().getWord() + " ");
//			
//		}
//		
//		whoSaidWhat.get(curSpeaker).add(sb.toString().trim());
//		
//		System.out.println(Tools.toJSON(whoSaidWhat));
		
		Map<Integer, Map<String, Object>> whoSaidWhatWhen = new HashMap<Integer, Map<String, Object>>();
		
		StringBuffer sb = new StringBuffer();
		int curSpeaker = -1;
		double when = 0D;
		int i = 0;
		
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
		
		Map<Integer, Map<String, Object>> whoSaidWhatWhenSorted = (Map<Integer, Map<String, Object>>) whoSaidWhatWhen.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		
		System.out.println(Tools.toJSON(whoSaidWhatWhenSorted));
		
	}

}
