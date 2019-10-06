package com.vngmedia.infochanel.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;

import com.vngmedia.infochanel.domain.InfoPlaylistUpload;
import com.vngmedia.infochanel.domain.InfoVideoUpload;
import com.vngmedia.infochanel.untils.ChanelConstants;
import com.vngmedia.infochanel.untils.ExportDataUntil;

public class MappingInfoService {
	
	private static String chanel = "";
	
	public  Map<String, InfoVideoUpload>  mergeData(String linkChanel) {

		PlaylistService playlistService = new PlaylistService();
		
		VideoService videoService = new VideoService();
		
		MappingInfoService mapping = new MappingInfoService();
		
		// list video info in page videos
		Map<String, InfoVideoUpload> listInfoVideo = new HashMap<String, InfoVideoUpload>();
		
		// list playlist info in page playlist
		Map<String, InfoVideoUpload> listInfoMappingVideo = new HashMap<String, InfoVideoUpload>();
		
		// list mapping
		List<InfoPlaylistUpload> listInfoPlaylist = new ArrayList<InfoPlaylistUpload>();
		
		// Get playlist
		playlistService.getInfoPlaylistChanel(linkChanel, listInfoPlaylist);
		
		// Get video
		videoService.getInfoVideoChanel(linkChanel, listInfoVideo);
		
		// Mapping data
		mapping.mapping(listInfoPlaylist, listInfoMappingVideo);
		
		// merge data
		Map<String, InfoVideoUpload> mergeInfoVideo = new HashMap<String, InfoVideoUpload>(listInfoVideo);
		
		for (Map.Entry<String, InfoVideoUpload> video: listInfoMappingVideo.entrySet()) {
			if (mergeInfoVideo.get(video.getKey()) == null) {
				continue;
			} else {
				mergeInfoVideo.get(video.getKey()).setPlayList(video.getValue().getPlayList());
			}
		}
		
		return mergeInfoVideo;
	}
	
	/** Mapping all video with all playlist
	 * @param List<InfoPlaylistUpload> listPlaylist
	 * @param Map<String, InfoVideoUpload> listInfoVideo
	 * */
	public void mapping(List<InfoPlaylistUpload> listPlaylist, Map<String, InfoVideoUpload> listInfoVideo) {
		
		for (InfoPlaylistUpload playlist : listPlaylist) {
			mappingVideoBaseOnPlaylist(playlist, listInfoVideo);
		}

	}
	
	/** Mapping video with playlist
	 * @param List<InfoPlaylistUpload> listPlaylist
	 * @param Map<String, InfoVideoUpload> listInfoVideo
	 * */
	public void mappingVideoBaseOnPlaylist(InfoPlaylistUpload playlist, Map<String, InfoVideoUpload> listInfoVideo) {
		String linkPlaylist = ChanelConstants.LINK_PLAYLIST;
		
		String pageContinue = ChanelConstants.FIRST_PAGE;
		
		String cToken = null;
		
		try {
			do {

				if (cToken != null) {
					linkPlaylist = "https://www.youtube.com/browse_ajax?ctoken="+cToken
							+"&continuation="+cToken
							+ "&itct="+pageContinue;
				} else {
					linkPlaylist = linkPlaylist + playlist.id + pageContinue;
				}
				
				URL url = new URL(linkPlaylist);
				HttpsURLConnection getConnect = (HttpsURLConnection) url.openConnection();
				getConnect.setRequestMethod("GET");
				getConnect.setRequestProperty("x-youtube-client-name", "1");
				getConnect.setRequestProperty("x-youtube-client-version", "2."+ new ExportDataUntil().getCurrentDate() +".00.00");
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
					
					// Get info video to list
					if (pageContinue == ChanelConstants.FIRST_PAGE) {
						cToken = getTokenFirst(jsonArrayParent);
						pageContinue = getPageFirst(jsonArrayParent);
						getFirstListVideoInfo(playlist, jsonArrayParent, listInfoVideo);
					} else {
						cToken = getToken(jsonArrayParent);
						pageContinue = getPage(jsonArrayParent);
						getListVideoInfo(playlist, jsonArrayParent, listInfoVideo);
					}
				}
				
			} while (pageContinue != null);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public void getFirstListVideoInfo(InfoPlaylistUpload playlist, JSONArray jsonArrayParent, Map<String, InfoVideoUpload> listInfoVideo) {
		try {
			JSONArray listVideo = jsonArrayParent.getJSONObject(1)
					.getJSONObject("response")
					.getJSONObject("contents")
					.getJSONObject("twoColumnBrowseResultsRenderer")
					.getJSONArray("tabs")
					.getJSONObject(0)
					.getJSONObject("tabRenderer")
					.getJSONObject("content")
					.getJSONObject("sectionListRenderer")
					.getJSONArray("contents")
					.getJSONObject(0)
					.getJSONObject("itemSectionRenderer")
					.getJSONArray("contents")
					.getJSONObject(0)
					.getJSONObject("playlistVideoListRenderer")
					.getJSONArray("contents");
			
			for (int j = 0; j < listVideo.length(); j++) {
				try {
					InfoVideoUpload youtobe = new InfoVideoUpload();
					youtobe.setId(j);
					
					String link = listVideo.getJSONObject(j)
							.getJSONObject("playlistVideoRenderer")
							.getString("videoId");
					if(checkKey(link, listInfoVideo)) {
						if (listInfoVideo.get(link).getPlayList() == null) {
							listInfoVideo.get(link).setPlayList("");
						}
						listInfoVideo.get(link).setPlayList(listInfoVideo.get(link).getPlayList().concat(",".concat(playlist.getNamePlayList())));
						continue;
					}
					youtobe.setLinkVideo(ChanelConstants.LINK_YOUTOBE
							.concat(link));
					
					youtobe.setChanel(chanel);
					
					
					youtobe.setNameVideo(listVideo.getJSONObject(j)
							.getJSONObject("playlistVideoRenderer")
							.getJSONObject("title")
							.getString("simpleText"));
					
					youtobe.setTimeVideo(listVideo.getJSONObject(j)
							.getJSONObject("playlistVideoRenderer")
							.getJSONObject("lengthText")
							.getString("simpleText"));
					
					youtobe.setPlayList(playlist.getNamePlayList());
					
					listInfoVideo.put(link, youtobe);
				} catch (Exception e) {
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getListVideoInfo(InfoPlaylistUpload playlist, JSONArray jsonArrayParent, Map<String, InfoVideoUpload> listInfoVideo) {
		try {
			JSONArray listVideo = jsonArrayParent.getJSONObject(1)
					.getJSONObject("response")
					.getJSONObject("continuationContents")
					.getJSONObject("playlistVideoListContinuation")
					.getJSONArray("contents");
			
			for (int j = 0; j < listVideo.length(); j++) {
				try {
					InfoVideoUpload youtobe = new InfoVideoUpload();
					youtobe.setId(j);
					
					String link = listVideo.getJSONObject(j)
							.getJSONObject("playlistVideoRenderer")
							.getString("videoId");
					
					if(checkKey(link, listInfoVideo)) {
						if (listInfoVideo.get(link).getPlayList() == null) {
							listInfoVideo.get(link).setPlayList(playlist.getNamePlayList());
						} else {
							listInfoVideo.get(link).setPlayList(listInfoVideo.get(link).getPlayList().concat(",".concat(playlist.getNamePlayList())));
						}
						continue;
					}
					
					youtobe.setLinkVideo(ChanelConstants.LINK_YOUTOBE.concat(link));
					
					youtobe.setChanel(chanel);
						youtobe.setNameVideo(listVideo.getJSONObject(j)
								.getJSONObject("playlistVideoRenderer")
								.getJSONObject("title")
								.getString("simpleText"));
	
					youtobe.setTimeVideo(listVideo.getJSONObject(j)
							.getJSONObject("playlistVideoRenderer")
							.getJSONObject("lengthText")
							.getString("simpleText"));
					
					youtobe.setPlayList(playlist.getNamePlayList());
					
					listInfoVideo.put(link, youtobe);
				} catch (Exception e) {
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getPageFirst(JSONArray jsonArrayParent) {
		try {
			return jsonArrayParent.getJSONObject(1)
					.getJSONObject("response")
					.getJSONObject("contents")
					.getJSONObject("twoColumnBrowseResultsRenderer")
					.getJSONArray("tabs")
					.getJSONObject(0)
					.getJSONObject("tabRenderer")
					.getJSONObject("content")
					.getJSONObject("sectionListRenderer")
					.getJSONArray("contents")
					.getJSONObject(0)
					.getJSONObject("itemSectionRenderer")
					.getJSONArray("contents")
					.getJSONObject(0)
					.getJSONObject("playlistVideoListRenderer")
					.getJSONArray("continuations")
					.getJSONObject(0)
					.getJSONObject("nextContinuationData")
					.getString("clickTrackingParams");
		} catch (JSONException e) {
			return null;
		}
	}
	
	public String getTokenFirst(JSONArray jsonArrayParent) {
		try {
			return jsonArrayParent.getJSONObject(1)
					.getJSONObject("response")
					.getJSONObject("contents")
					.getJSONObject("twoColumnBrowseResultsRenderer")
					.getJSONArray("tabs")
					.getJSONObject(0)
					.getJSONObject("tabRenderer")
					.getJSONObject("content")
					.getJSONObject("sectionListRenderer")
					.getJSONArray("contents")
					.getJSONObject(0)
					.getJSONObject("itemSectionRenderer")
					.getJSONArray("contents")
					.getJSONObject(0)
					.getJSONObject("playlistVideoListRenderer")
					.getJSONArray("continuations")
					.getJSONObject(0)
					.getJSONObject("nextContinuationData")
					.getString("continuation");
		} catch (JSONException e) {
			return null;
		}
	}
	
	
	public String getPage(JSONArray jsonArrayParent) {
		try {
			return jsonArrayParent.getJSONObject(1)
					.getJSONObject("response")
					.getJSONObject("continuationContents")
					.getJSONObject("playlistVideoListContinuation")
					.getJSONArray("continuations")
					.getJSONObject(0)
					.getJSONObject("nextContinuationData")
					.getString("clickTrackingParams");
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getToken(JSONArray jsonArrayParent) {
		try {
			return jsonArrayParent.getJSONObject(1)
					.getJSONObject("response")
					.getJSONObject("continuationContents")
					.getJSONObject("playlistVideoListContinuation")
					.getJSONArray("continuations")
					.getJSONObject(0)
					.getJSONObject("nextContinuationData")
					.getString("continuation");
		} catch (Exception e) {
			return null;
		}
	}
	
	public boolean checkKey(String link, Map<String, InfoVideoUpload> obj) {
		for (Map.Entry<String, InfoVideoUpload> entity: obj.entrySet()) {
			if(link.equals(entity.getKey())) {
				return true;
			}
		}
		return false;
	}
}
