package com.websitesearcher.models;


import java.util.Set;

/**
 * Model containing website search results
 *
 * @author gwalzer
 */
public class WebsiteSearchResults {

    public Set<String> matchingUrls;
    public Set<String> failedUrls;

    public WebsiteSearchResults(Set<String> matchingUrls, Set<String> failedUrls) {
        this.matchingUrls = matchingUrls;
        this.failedUrls = failedUrls;
    }
}
