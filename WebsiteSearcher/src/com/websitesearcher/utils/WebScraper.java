package com.websitesearcher.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Wrapper class for Jsoup's web scraping library.
 *
 * @author gwalzer
 */
public class WebScraper {

    private int defaultTimeout;

    public WebScraper(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public String scrapeBody(String url) throws IOException {
        Document document = Jsoup.connect(url).timeout(defaultTimeout).get();
        return document.html();
    }
}
