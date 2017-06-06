package com.websitesearcher.controllers;

import com.websitesearcher.utils.WebScraper;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Thread containing a website search task.
 *
 * @author gwalzer
 */
public class WebsiteSearchThread extends Thread {

    private WebScraper webScraper;
    private List<String> urls;
    private String keyword;
    private Set<String> matchingUrls;
    private Set<String> failedUrls;

    private static final int DEFAULT_TIMEOUT = 2000; // ms

    public WebsiteSearchThread(List<String> urls, String keyword) {
        webScraper = new WebScraper(DEFAULT_TIMEOUT);
        matchingUrls = new HashSet<>();
        failedUrls = new HashSet<>();
        this.urls = urls;
        this.keyword = keyword;
    }

    @Override
    public void run() {
        search(urls, keyword);
    }

    public Set<String> getMatchingUrls() {
        return matchingUrls;
    }

    public Set<String> getFailedUrls() {
        return failedUrls;
    }

    // @return the set of URLs with website content matching the keyword param.
    private void search(List<String> urls, String keyword) {
        for (String url : urls) {
            try {
                String pageHtml = webScraper.scrapeBody(url);
                if (pageHtml.toLowerCase().contains(keyword.toLowerCase())) {
                    matchingUrls.add(url);
                }
            } catch (IOException e) {
                failedUrls.add(url);
            }
        }
    }
}
