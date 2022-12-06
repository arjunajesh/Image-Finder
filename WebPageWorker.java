package com;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.eulerity.hackathon.imagefinder.WebPageThreadPool.InOut;



public class WebPageWorker implements Callable<InOut> {

    InOut i;
    public WebPageWorker(InOut i){
        this.i = i;
    }
    @Override
    public InOut call() throws Exception, HttpStatusException {
        String url = i.getUrl();
        if(url==null) return null;

        //Find all links on the page
		try{
            Document d = Jsoup.connect(url).get();
            List<String> links = new ArrayList<>();
            d.select("a").forEach((e) -> links.add(e.attr("href"))); 
            for(String link : links){
                if(link == null || link.equalsIgnoreCase("#") || link.equalsIgnoreCase("/")){ 
                    continue;
                }
                if(isAbsoluteURL(link)){ //if link is full URL ex: https://www.sample.com/music/album1
                    
                    URL tempURL = new URL(i.getUrl());
                    String domain = tempURL.getHost();
                    domain = domain.replace("www","");
                    if(link.contains(domain)){ //makes sure that the link is within the domain before adding it to threadpool
                        i.getTp().addWebPage(link);
                    }
                } else { //link is only the path ex: /music/album1
                    String nextURLStr = i.getUrl() + link; 
                    URL url1 = new URL(nextURLStr);
                    String path = url1.getPath();
                    if(path.startsWith("//")){ //catches doubles slashes in the path and replaces it with a single slash
                        path = path.replaceFirst("\\/\\/", "/");
                    }
                    String url2 = url1.getProtocol() + "://" + url1.getHost() +  path; //constructs working url 
                    i.getTp().addWebPage(url2); //send the link to the threadpool
                    
                }
                
            }
            
            //Find all images on the page
            Elements imgs = d.select("img");
    
            for(Element el: imgs){
                i.getTp().addImageURL(el.absUrl("src")); //adds images to master list of images in the threadpool class
            }
        }
        catch(Exception e){
            System.out.println("Caught Exception with url: " + url + " " + e.getMessage());
        }
        return i;
    }
 
    private boolean isAbsoluteURL(String urlString) {
        boolean result = false;
        try
        {
            URL url = new URL(urlString);
            String protocol = url.getProtocol();
            if (protocol != null && protocol.trim().length() > 0)
                result = true;
        }
        catch (MalformedURLException e)
        {
            return false;
        }
        return result;
    }
    
}
