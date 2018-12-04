package com.ekaly.test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.ekaly.tools.BasicAuthInterceptor;
import com.ekaly.tools.UnsafeOkHttpClient;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneOptions;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Main3 {

	public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException, IOException {
		
		IamOptions options = new IamOptions.Builder()
				  .apiKey("sCf5t09pbloZT7J9tHQ3k3gCuDJ0XYoWtJ4aNEUsyPi-")
				  .build();

		SpeechToText s2t = new SpeechToText(options);
		s2t.setEndPoint("https://gateway-wdc.watsonplatform.net/speech-to-text/api");
		
		System.out.println("s2t=" + s2t);
		
		OkHttpClient ta0 = new UnsafeOkHttpClient().getUnsafeOkHttpClient().newBuilder()
			.addInterceptor(new BasicAuthInterceptor("apikey", "xdV_2mbjDY5rd7GWm6SItoUw8eonDjpHMxSNb_B14wbV"))
		    .build();
		
		
//		RequestBody body = new MultipartBody.Builder()
//			.setType(MultipartBody.FORM)
//	        .addFormDataPart("File", "images_file", RequestBody.create(MediaType.parse("image/jpeg"), path.toFile()))
//	        .build();

		HttpUrl.Builder urlBuilder = HttpUrl.parse("https://gateway-wdc.watsonplatform.net/tone-analyzer/api/v3/tone").newBuilder();
		urlBuilder.addQueryParameter("version", "2017-09-21");
		urlBuilder.addQueryParameter("sentences", String.valueOf(true));
		String url = urlBuilder.toString();
		
		
		Request.Builder requestBuilder = new Request.Builder()
			.addHeader("Content-Language", "fr")
			.addHeader("Accept-Language", "fr")
			.addHeader("Content-Type", "application/json")
			.url(url);		

		String json = "{\"text\": \"On en a gros ! On fait un cul de chouette ?\"}";
		RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);		
		
		Request request = requestBuilder
			.post(body)
			.build();
		
		Response response = ta0.newCall(request).execute();
        System.out.println(response.body().string());		
		
//		options = new IamOptions.Builder()
//				  .apiKey("fLbevJlOcM3EiQYH7lzhfM-iwe3pHOrP0g49OPj0zHhX-")
//				  .build();
//		
//		ToneAnalyzer service = new ToneAnalyzer("2017-09-21", "apikey", "fLbevJlOcM3EiQYH7lzhfM-iwe3pHOrP0g49OPj0zHhX-");
//		service.setEndPoint("https://gateway-wdc.watsonplatform.net/tone-analyzer/api");
//	
//		String text =
//		  "I know the times are difficult! Our sales have been "
//		      + "disappointing for the past three quarters for our data analytics "
//		      + "product suite. We have a competitive data analytics product "
//		      + "suite in the industry. But we need to do our job selling it! "
//		      + "We need to acknowledge and fix our sales challenges. "
//		      + "We can’t blame the economy for our lack of execution! "
//		      + "We are missing critical sales opportunities. "
//		      + "Our product is in no way inferior to the competitor products. "
//		      + "Our clients are hungry for analytical tools to improve their "
//		      + "business outcomes. Economy has nothing to do with it.";
//	
//		// Call the service and get the tone
//		ToneOptions toneOptions = new ToneOptions.Builder()
//		  .html(text)
//		  .build();
//	
//		ToneAnalysis tone = service.tone(toneOptions).execute();
//		System.out.println(tone);
		
	}
	
}
