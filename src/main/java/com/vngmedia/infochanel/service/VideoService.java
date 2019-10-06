package com.vngmedia.infochanel.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;

import com.vngmedia.infochanel.domain.InfoVideoUpload;
import com.vngmedia.infochanel.untils.ChanelConstants;
import com.vngmedia.infochanel.untils.ExportDataUntil;
import com.vngmedia.infochanel.untils.HandleHeaderRequest;

public class VideoService {
	
	private static String chanel = "";
	
	public Map<String, InfoVideoUpload> getListVideo(String linkChanel) {
		VideoService video = new VideoService();
		Map<String, InfoVideoUpload> listVideo = new HashMap<String, InfoVideoUpload>();
		video.getInfoVideoChanel(linkChanel, listVideo);
		return listVideo;
	}
	
	/**
	 * @param String linkChanel youtobe
	 * @return void
	 * */
	public void getInfoVideoChanel(String linkChanel, Map<String, InfoVideoUpload> listInfoVideo) {
		String pageContinue = ChanelConstants.FIRST_PAGE_VIDEO_PUBLISH;
		
		String cToken = null;
		String date = new ExportDataUntil().getCurrentDate();
		do {
			try {
				
				if (cToken != null) {
					linkChanel = "https://www.youtube.com/browse_ajax?ctoken="+cToken
							+"&continuation"+cToken
							+ "&itct="+pageContinue;
				} else {
					linkChanel = linkChanel + pageContinue;
				}
				
				URL url = new URL(linkChanel);
				HttpsURLConnection getConnect = (HttpsURLConnection) url.openConnection();
				getConnect.setRequestMethod("GET");
				getConnect.setRequestProperty("x-youtube-client-name", "1");
				getConnect.setRequestProperty("x-youtube-client-version", "2."+date+".00.00");
				getConnect.setDoOutput(true);
				
				int responseCode = getConnect.getResponseCode();
				
				if (responseCode == HttpsURLConnection.HTTP_OK) {
					
					BufferedReader bufferedReader = 
							new BufferedReader(new InputStreamReader(getConnect.getInputStream()));
					
					StringBuffer response = new StringBuffer();
					
					String readLine = null;
					while ((readLine = bufferedReader.readLine()) != null) {
						
						response.append(readLine);
					}
					
					bufferedReader.close();
					
					JSONArray jsonArrayParent = new JSONArray(response.toString());
					
					HandleHeaderRequest headerRequest = new HandleHeaderRequest();
					// Get info video to list
					if (pageContinue == ChanelConstants.FIRST_PAGE_VIDEO_PUBLISH) {
						cToken = headerRequest.getTokenFirstContinue(jsonArrayParent);
						pageContinue =headerRequest.getPageFirstContinue(jsonArrayParent);
						chanel = jsonArrayParent.getJSONObject(1).getJSONObject("response").getJSONObject("microformat").getJSONObject("microformatDataRenderer").getString("title");
						getFirstListVideoInfo(jsonArrayParent, listInfoVideo);
					} else {
						cToken = headerRequest.getContinueToken(jsonArrayParent);
						pageContinue = headerRequest.getPageContinue(jsonArrayParent);
						getContinueListVideoInfo(jsonArrayParent, listInfoVideo);
					}
				}
			} catch (Exception e) {
				System.out.println("Get error");
				e.printStackTrace();
			}
		} while (pageContinue != null);
	}
	
	/** Get info video
	 * @param JSONArray json data
	 * @param List<InformationYoutobe> list video
	 * */
	public void getFirstListVideoInfo(JSONArray jsonArrayParent, Map<String, InfoVideoUpload> listInfoVideo) {
		try {
			JSONArray listVideo = jsonArrayParent.getJSONObject(1)
					.getJSONObject("response")
					.getJSONObject("contents")
					.getJSONObject("twoColumnBrowseResultsRenderer")
					.getJSONArray("tabs")
					.getJSONObject(1)
					.getJSONObject("tabRenderer")
					.getJSONObject("content")
					.getJSONObject("sectionListRenderer")
					.getJSONArray("contents")
					.getJSONObject(0)
					.getJSONObject("itemSectionRenderer")
					.getJSONArray("contents")
					.getJSONObject(0)
					.getJSONObject("gridRenderer")
					.getJSONArray("items");
			
			for (int j = 0; j < listVideo.length(); j++) {
				try {
					InfoVideoUpload youtobe = new InfoVideoUpload();
					String link = listVideo.getJSONObject(j)
							.getJSONObject("gridVideoRenderer")
							.getString("videoId");
					youtobe.setLinkVideo(ChanelConstants.LINK_YOUTOBE.concat(link));
					
					youtobe.setChanel(chanel);
					
					youtobe.setNameVideo(listVideo.getJSONObject(j)
							.getJSONObject("gridVideoRenderer")
							.getJSONObject("title")
							.getString("simpleText"));
					
					youtobe.setTimeVideo(listVideo.getJSONObject(j)
							.getJSONObject("gridVideoRenderer")
							.getJSONArray("thumbnailOverlays")
							.getJSONObject(0)
							.getJSONObject("thumbnailOverlayTimeStatusRenderer")
							.getJSONObject("text")
							.getString("simpleText"));
					listInfoVideo.put(link, youtobe);
				} catch (Exception e) {
					continue;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public void getContinueListVideoInfo(JSONArray jsonArrayParent, Map<String, InfoVideoUpload> listInfoVideo) {
		try {
			JSONArray listVideo = jsonArrayParent.getJSONObject(1)
					.getJSONObject("response")
					.getJSONObject("continuationContents")
					.getJSONObject("gridContinuation")
					.getJSONArray("items");
			
			for (int j = 0; j < listVideo.length(); j++) {
				InfoVideoUpload youtobe = new InfoVideoUpload();
				String link = listVideo.getJSONObject(j)
						.getJSONObject("gridVideoRenderer")
						.getString("videoId");
				youtobe.setLinkVideo(ChanelConstants.LINK_YOUTOBE.concat(link));
				
				youtobe.setChanel(chanel);
				
				youtobe.setNameVideo(listVideo.getJSONObject(j)
						.getJSONObject("gridVideoRenderer")
						.getJSONObject("title")
						.getString("simpleText"));
				
				youtobe.setTimeVideo(listVideo.getJSONObject(j)
						.getJSONObject("gridVideoRenderer")
						.getJSONArray("thumbnailOverlays")
						.getJSONObject(0)
						.getJSONObject("thumbnailOverlayTimeStatusRenderer")
						.getJSONObject("text")
						.getString("simpleText"));
				listInfoVideo.put(link, youtobe);
			}
		} catch (Exception e) {
				// TODO: handle exception
		}
	}
}
