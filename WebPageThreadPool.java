package com.eulerity.hackathon.imagefinder;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.WebPageWorker;
import com.eulerity.Constants;

public class WebPageThreadPool{

    private ExecutorService e = Executors.newFixedThreadPool(Constants.THREADS_COUNT);
    private Set<String> urlList = new HashSet<>();
    private Set<String> imgList = new HashSet<>();
    private List<Future<InOut>> futures = new ArrayList<>();
    int  i =0;

    public Set<String> getImgList() {
        return imgList;
    }
    
    synchronized public void addWebPage(String url){
        try {
            URL url1 = new URL(url);
            url = url1.toURI().toString();
            
        } catch (MalformedURLException | URISyntaxException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        if(urlList.contains(url)) return;
        if(i > Constants.MAX_PAGES_TO_PROCESS){
            return;
        }
        i++;
        urlList.add(url);
        InOut i = new InOut(url, this);
        WebPageWorker w = new WebPageWorker(i);
        futures.add(e.submit(w));
    }

    synchronized public void addImageURL(String url){
        imgList.add(url);
    }

    public void waitForTermination(){
        do{
            Future<InOut> f = futures.get(0);
        
            try {
                InOut i = f.get();
                System.out.println("Completed Processing: " + i.getUrl());
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
            }
            futures.remove(0);
        } while(futures.size()>0);
        System.out.println("Total Web Pages Processed: " + i);
        System.out.println("Total Images Found: " + imgList.size());

    }

    public static class InOut{
    
        private String url;

        private WebPageThreadPool tp;

        public WebPageThreadPool getTp() {
            return tp;
        }

        public void setTp(WebPageThreadPool tp) {
            this.tp = tp;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public InOut(String url, WebPageThreadPool tp){
            this.url = url;
            this.tp = tp;
        } 
    }
}

