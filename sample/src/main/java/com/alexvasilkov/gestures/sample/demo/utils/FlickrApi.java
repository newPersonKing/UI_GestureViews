package com.alexvasilkov.gestures.sample.demo.utils;

import com.alexvasilkov.events.EventResult;
import com.alexvasilkov.events.Events.Background;
import com.alexvasilkov.events.Events.Subscribe;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.photos.PhotosInterface;
import com.googlecode.flickrjandroid.photos.SearchParameters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlickrApi {

    public static final String LOAD_IMAGES_EVENT = "LOAD_IMAGES_EVENT";

    private static final String API_KEY = "7f6035774a01a39f9056d6d7bde60002";
    private static final String SEARCH_QUERY = "landscape";
    private static final int PER_PAGE = 30;
    private static final int MAX_PAGES = 5;

    private static final Set<String> photoParams = new HashSet<>();

    static {
        photoParams.add("url_t");
        photoParams.add("url_m");
        photoParams.add("url_h");
        photoParams.add("owner_name");
    }

    private static final List<Photo> photos = new ArrayList<>();
    private static final List<PhotoList> pages = new ArrayList<>();

    private FlickrApi() {}

    @Background(singleThread = true)
    @Subscribe(LOAD_IMAGES_EVENT)
    private static synchronized EventResult loadImages(int count) throws Exception {
        SearchParameters params = new SearchParameters();
        params.setText(SEARCH_QUERY);
        params.setSafeSearch(Flickr.SAFETYLEVEL_SAFE);
        params.setSort(SearchParameters.INTERESTINGNESS_DESC);
        params.setExtras(photoParams);

        boolean hasNext = hasNext();

        final PhotosInterface flickrPhotos = new Flickr(API_KEY).getPhotosInterface();
        while (photos.size() < count && hasNext) {
            final PhotoList loaded = flickrPhotos.search(params, PER_PAGE, pages.size() + 1);
            pages.add(loaded);
            photos.addAll(loaded);

            hasNext = hasNext();
        }

        int resultSize = Math.min(photos.size(), count);

        List<Photo> result = new ArrayList<>(photos.subList(0, resultSize));
        if (!hasNext) {
            hasNext = photos.size() > count;
        }

        return EventResult.create().result(result, hasNext).build();
    }

    private static boolean hasNext() {
        if (pages.isEmpty()) {
            return true;
        } else if (pages.size() >= MAX_PAGES) {
            return false;
        } else {
            PhotoList page = pages.get(pages.size() - 1);
            return page.getPage() * page.getPerPage() < page.getTotal();
        }
    }

}
