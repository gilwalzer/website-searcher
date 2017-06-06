package com.websitesearcher.controllers;

import com.websitesearcher.models.WebsiteSearchResults;
import com.websitesearcher.utils.CsvReader;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.exit;

/**
 * Controller for the website search main function.
 *
 * @gwalzer
 */
public class WebsiteSearch {

    private static final int MAX_THREADS = 20;

    // run MAX_THREADS threads that search a separate set of lists
    private static WebsiteSearchResults run(List<String> urls, String keyword) throws IOException {
        List<WebsiteSearchThread> threads = new LinkedList<>();
        for (List<String> subListUrls : sliceUrls(urls, MAX_THREADS)) {
            if (subListUrls.size() > 0) {
                WebsiteSearchThread thread = new WebsiteSearchThread(subListUrls, keyword);
                thread.setName(subListUrls.get(0));
                thread.start();
                threads.add(thread);
            }
        }

        while (someThreadsAlive(threads)) {}

        Set<String> failed = combineSets(threads.stream().map(WebsiteSearchThread::getFailedUrls).collect(Collectors.toList()));
        System.err.println("Failed count: " + failed.size() + " " + failed);

        Set<String> matching = combineSets(threads.stream().map(WebsiteSearchThread::getMatchingUrls).collect(Collectors.toList()));
        System.out.println("Matching count: " + matching.size() + " " + matching);

        WebsiteSearchResults results = new WebsiteSearchResults(matching, failed);
        return results;
    }

    // aggregate urls matching the keyword from each sublist
    private static Set<String> combineSets(List<Set<String>> setList) {
        Set<String> fullSet = new HashSet<>();
        for (Set<String> set : setList) {
            fullSet.addAll(set);
        }
        return fullSet;
    }

    // ensure no threads are still scraping web pages. Default response timeout is 2 seconds per request
    private static boolean someThreadsAlive(List<WebsiteSearchThread> threads) {
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                return true;
            }
        }
        return false;
    }

    // divide the set of lists into MAX_THREADS sublists
    private static List<List<String>> sliceUrls(List<String> urls, int numSubLists) {
        int i = 1;
        List<List<String>> subLists = new LinkedList<>();
        List<String> subList = new LinkedList<>();
        for (String url : urls) {
            if (i % numSubLists == 0) {
                subLists.add(subList);
                subList = new LinkedList<>();
            }
            subList.add(url);
            i++;
        }
        subLists.add(subList);
        return subLists;
    }

    // format the url properly
    private static String formatUrl(String unformatted) {
        return "http://" + unformatted.substring(1, unformatted.length() - 2);
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Run with arguments [fileName] [keyword] [outputFileName]");
            exit(1);
        }
        String fileName = args[0];
        String keyword = args[1];
        String outputFileName = args[2];

        final int URL_INDEX = 1; // index of URL in csv input file

        List<String> urls = new LinkedList<>();
        Set<String> matchingUrls = new HashSet<>();
        Set<String> failedUrls = new HashSet<>();

        try {
            System.out.println("Reading from file " + fileName + "...");
            InputStream inputStream = WebsiteSearch.class.getResourceAsStream(fileName);
            BufferedReader bufferedReader;
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            } else {
                bufferedReader = new BufferedReader(new FileReader(fileName));
            }
            CsvReader csvReader = new CsvReader(bufferedReader);
            csvReader.readLine(); //skip header line

            while (true) {
                String[] columns = csvReader.readLine();
                if (columns == null) {
                    break;
                }
                String url = columns[URL_INDEX];
                urls.add(formatUrl(url));
            }
            matchingUrls = run(urls, keyword).matchingUrls;
            failedUrls = run(urls, keyword).failedUrls;
        } catch (FileNotFoundException e) {
            System.err.println("Unable to locate file with file name " + fileName + ". Please try again.");
            exit(1);
        } catch (IOException e) {
            System.err.println("Input error when parsing file with file name " + fileName + ". Please try again.");
            exit(1);
        }

        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            File outputFile = new File(outputFileName);
            fileWriter = new FileWriter(outputFile);
            bufferedWriter = new BufferedWriter(fileWriter);
            for (String url : urls) {
                bufferedWriter.write(url);
                bufferedWriter.write(",");
                bufferedWriter.write(matchingUrls.contains(url) ? "1" : "0");
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error when writing to output file " + outputFileName + ". Please try again.");
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                System.out.println("Error when closing output file " + outputFileName + ". Please try again.");
            }
        }
    }
}
