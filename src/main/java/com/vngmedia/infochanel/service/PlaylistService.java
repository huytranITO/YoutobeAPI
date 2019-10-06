package com.vngmedia.infochanel.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;

import com.vngmedia.infochanel.domain.InfoPlaylistUpload;
import com.vngmedia.infochanel.untils.ChanelConstants;
import com.vngmedia.infochanel.untils.ExportDataUntil;
import com.vngmedia.infochanel.untils.HandleHeaderRequest;


public class PlaylistService {
	public static String chanel = "";
	
	public List<InfoPlaylistUpload> getListPlaylist(String linkChanel) {
		
		List<InfoPlaylistUpload> listInfoPlaylist = new ArrayList<InfoPlaylistUpload>();
		
		PlaylistService service = new PlaylistService();
		service.getInfoPlaylistChanel(linkChanel, listInfoPlaylist);
		
		return listInfoPlaylist;
	}
	
	/** Get list playlist chanel
	 * @param String linkChanel youtobe
	 * @return void
	 * */
	public void getInfoPlaylistChanel(String linkChanel, List<InfoPlaylistUpload> listInfoPlaylist) {
		
		String pageContinue = ChanelConstants.PAGE_FIRST_PLAYLIST;
		
		String cToken = null;

		do {
			try {
				
				if (cToken != null) {
					linkChanel = "https://www.youtube.com/browse_ajax?ctoken="+cToken
							+"&continuation="+cToken
							+ "&itct="+pageContinue;
				} else {
					linkChanel = linkChanel + pageContinue;
				}
				
				URL url = new URL(linkChanel);
				HttpsURLConnection getConnect = (HttpsURLConnection) url.openConnection();
				getConnect.setRequestMethod("GET");
				getConnect.setRequestProperty("x-youtube-client-name", "1");
				getConnect.setRequestProperty("x-youtube-client-version", "2."+ new ExportDataUntil().getCurrentDate()+".00.00");
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
					if (pageContinue == ChanelConstants.PAGE_FIRST_PLAYLIST) {
						chanel = jsonArrayParent.getJSONObject(1)
								.getJSONObject("response")
								.getJSONObject("header")
								.getJSONObject("c4TabbedHeaderRenderer")
								.getString("title");
						cToken = headerRequest.getTokenFirst(jsonArrayParent);
						pageContinue =headerRequest. getPageFirstPlaylist(jsonArrayParent);
						getFirstInfoPlayList(jsonArrayParent, listInfoPlaylist);
					} else {
						cToken = headerRequest.getContinueToken(jsonArrayParent);
						pageContinue = headerRequest.getPageContinue(jsonArrayParent);
						getContinueListPlaylistInfo(jsonArrayParent, listInfoPlaylist);
					}
				}
			} catch (Exception e) {
				System.out.println("Get error");
				e.printStackTrace();
			}
		} while (pageContinue != null);
	}
	
	public static void getFirstInfoPlayList (JSONArray jsonArrayParent, List<InfoPlaylistUpload> lisInfoPlaylist) {
		try {
			JSONArray listPlaylist = jsonArrayParent.getJSONObject(1)
					.getJSONObject("response")
					.getJSONObject("contents")
					.getJSONObject("twoColumnBrowseResultsRenderer")
					.getJSONArray("tabs")
					.getJSONObject(2)
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
			
			for (int j = 0; j < listPlaylist.length(); j++) {
				InfoPlaylistUpload playlist = new InfoPlaylistUpload();
				
				playlist.setId(listPlaylist.getJSONObject(j).getJSONObject("gridPlaylistRenderer").getString("playlistId"));
				
				playlist.setTypePlaylist(ChanelConstants.LIST_CREATED);
				
				playlist.setNamePlayList(listPlaylist.getJSONObject(j).getJSONObject("gridPlaylistRenderer").getJSONObject("title")
						.getJSONArray("runs").getJSONObject(0).getString("text"));
				
				lisInfoPlaylist.add(playlist);
			}	
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public static void getContinueListPlaylistInfo(JSONArray jsonArrayParent, List<InfoPlaylistUpload> lisInfoPLaylist) {
		try {
			JSONArray listPlaylist = jsonArrayParent.getJSONObject(1)
					.getJSONObject("response")
					.getJSONObject("continuationContents")
					.getJSONObject("gridContinuation")
					.getJSONArray("items");
			
			for (int j = 0; j < listPlaylist.length(); j++) {
				InfoPlaylistUpload playlist = new InfoPlaylistUpload();
				
				playlist.setId(listPlaylist.getJSONObject(j).getJSONObject("gridPlaylistRenderer").getString("playlistId"));
				
				playlist.setTypePlaylist(ChanelConstants.LIST_CREATED);
				
				playlist.setNamePlayList(listPlaylist.getJSONObject(j).getJSONObject("gridPlaylistRenderer").getJSONObject("title")
						.getJSONArray("runs").getJSONObject(0).getString("text"));
				
				lisInfoPLaylist.add(playlist);
			}
		} catch (Exception e) {
				// TODO: handle exception
		}
	}
	
}
