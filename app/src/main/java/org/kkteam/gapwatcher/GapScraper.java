package org.kkteam.gapwatcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Federico
 */
public class GapScraper {
    
    private static final String USER_AGENT = "PostmanRuntime/7.18.0";
    private static final String URL = "http://gap.adm.unipi.it/GAP-U-Ingegneria/newGAP-SI.cgi";
    
    public GapScraper() {
    }
    
    protected HttpURLConnection getHttpURLConnection() throws MalformedURLException, IOException {
        URL url = new URL(URL);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Host", "gap.adm.unipi.it");
        con.setRequestProperty("Connection", "keep-alive");
        con.setRequestProperty("Accept", "*/*");
        con.setRequestProperty("Cache-Control", "no-cache");
        con.setRequestProperty("Accept-Language", "it-IT,it;q=0.9,en-US;q=0.8,en;q=0.7,la;q=0.6");
        con.setRequestProperty("Origin", "http://gap.adm.unipi.it");
        con.setRequestProperty("Referer", "http://gap.adm.unipi.it/GAP-U-Ingegneria/");
        con.setRequestProperty("Accept", "*/*");
        con.setRequestProperty("Content-Length", "16");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setInstanceFollowRedirects(false);
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes("query=guida_oggi");
        wr.flush();
        wr.close();
        return con;
    }
    
    protected static String getPageDump(HttpURLConnection con) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
    
    public RoomStatus[] getRoomsStatus() {
        String pageDump;
        try {
            //Creo la connessione
            HttpURLConnection con = getHttpURLConnection();
            //Invio la richiesta
            int responseCode = con.getResponseCode();
            //Response code deve essere 200
            if(responseCode != 200) {
                System.err.println("getRoomsStatus() -> responseCode diverso da 200. responseCode: " + responseCode);
                return new RoomStatus[0];
            }
            pageDump = getPageDump(con);
        }
        catch(IOException e) {
            e.printStackTrace();
            return new RoomStatus[0];
        }
        
        //Scraping
        Document doc = Jsoup.parse(pageDump);
        Element daytab = doc.getElementById("daytab");
        Elements trs = daytab.getElementsByTag("tr");
        String[] intervalsNamesStr;
        {
            Elements intervalsNames = trs.get(1).getElementsByTag("td");
            intervalsNamesStr = new String[intervalsNames.size()-1];
            for(int i = 1; i < intervalsNames.size(); ++i) {
                intervalsNamesStr[i-1] = intervalsNames.get(i).text();
            }
        }
        RoomStatus[] roomsStatus = new RoomStatus[trs.size()-4];
        for(int i = 0; i < trs.size()-4; ++i) {
            Element tr = trs.get(2+i);
            String name = tr.getElementsByTag("th").get(0).text();
            ArrayList<Interval> intervals = new ArrayList<Interval>();
            Elements tds = tr.getElementsByTag("td");
            int pointer = 0;
            intervals.add(new Interval(null, null));
            for(int j = 0; j < tds.size(); ++j) {
                Element td = tds.get(j);
                if(isTdEmpty(td)) {
                    if(intervals.get(intervals.size()-1).getStart() == null)
                        intervals.get(intervals.size()-1).setStart(intervalsNamesStr[pointer]);
                    ++pointer;
                } else {
                    if(intervals.get(intervals.size()-1).getStart() != null) {
                        intervals.get(intervals.size()-1).setEnd(intervalsNamesStr[pointer]);
                        intervals.add(new Interval(null, null));
                    }
                    if(!td.attr("colspan").isEmpty())
                        pointer += Integer.parseInt(td.attr("colspan"));
                    else
                        pointer += 1;
                }
            }
            {
                Interval lastInterval = intervals.get(intervals.size()-1);
                if(lastInterval.getStart() == null)
                    intervals.remove(lastInterval);
                else if(lastInterval.getEnd() == null)
                    lastInterval.setEnd("20:30");
            }
            roomsStatus[i] = new RoomStatus(name, intervals.toArray(new Interval[0]));
        }
        
        return roomsStatus;
    }
    
    public String getRoomsStatusJsonString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(getRoomsStatus());
        } catch (JsonProcessingException ex) {
            Logger.getLogger(GapScraper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "{}";
    }
    
    protected boolean isTdEmpty(Element td) {
        return td.attr("bgcolor").toLowerCase().equals("#f0f0f0");
    }
    
}