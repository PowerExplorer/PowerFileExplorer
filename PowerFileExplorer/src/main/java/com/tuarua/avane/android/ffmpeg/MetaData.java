package com.tuarua.avane.android.ffmpeg;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Eoin Landy on 05/11/2016.
 */

public class MetaData {
    private String _album;
    private String _albumArtist;
    private String _artist;
    private String _author;
    private String _comment;
    private String _composer;
    private String _copyright;
    private String _date;
    private String _description;
    private String _encodedBy;
    private String _episodeId;
    private String _genre;
    private String _grouping;
    private String _language;
    private String _lyrics;
    private String _network;
    private String _rating;
    private String _show;
    private String _title;
    private String _track;
    private String _year;

    private Map<String, String> customKeys = new HashMap<>();
    public void addCustom(String key, String value) {
        customKeys.put(key, value);
    }

    public ArrayList<String> getAsVector() {
        ArrayList<String> vec = new ArrayList<>();
        if(_album != null)
            vec.add("album="+_album);
        if(_albumArtist != null)
            vec.add("album_artist="+_albumArtist);
        if(_artist != null)
            vec.add("artist="+_artist);
        if(_author != null)
            vec.add("author="+_author);
        if(_comment != null)
            vec.add("comment="+_comment);
        if(_composer != null)
            vec.add("composer="+_composer);
        if(_copyright != null)
            vec.add("copyright="+_copyright);
        if(_date != null)
            vec.add("date="+_date);
        if(_description != null)
            vec.add("description="+_description);
        if(_encodedBy != null)
            vec.add("encoded_by="+_encodedBy);
        if(_episodeId != null)
            vec.add("episode_id="+_episodeId);
        if(_genre != null)
            vec.add("genre"+_genre);
        if(_grouping != null)
            vec.add("grouping="+_grouping);
        if(_language != null)
            vec.add("language="+_language);
        if(_lyrics != null)
            vec.add("lyrics="+_lyrics);
        if(_network != null)
            vec.add("network="+_network);
        if(_rating != null)
            vec.add("rating="+_rating);
        if(_show != null)
            vec.add("show="+_show);
        if(_title != null)
            vec.add("title="+_title);
        if(_track != null)
            vec.add("track="+_track);
        if(_year != null)
            vec.add("year="+_year);

        for (Map.Entry<String, String> entry : customKeys.entrySet())
            vec.add(entry.getKey() + "="+entry.getValue());

        return vec;
    }


    public void setAlbum(String value) {
        _album = value;
    }
    public void setAlbumArtist(String value) {
        _albumArtist = value;
    }
    public void setArtist(String value) {
        _artist = value;
    }
    public void setAuthor(String value) {
        _author = value;
    }
    public void setComment(String value) {
        _comment = value;
    }
    public void setComposer(String value) {
        _composer = value;
    }
    public void setCopyright(String value) {
        _copyright = value;
    }
    public void setDate(String value) {
        _date = value;
    }
    public void setDescription(String value) {
        _description = value;
    }
    public void setEncoded_by(String value) {
        _encodedBy = value;
    }
    public void setEpisode_id(String value) {
        _episodeId = value;
    }
    public void setGenre(String value) {
        _genre = value;
    }
    public void setGrouping(String value) {
        _grouping = value;
    }
    public void setLanguage(String value) {
        _language = value;
    }
    public void setLyrics(String value) {
        _lyrics = value;
    }
    public void setNetwork(String value) {
        _network = value;
    }
    public void setRating(String value) {
        _rating = value;
    }
    public void setShow(String value) {
        _show = value;
    }
    public void setTitle(String value) {
        _title = value;
    }
    public void setTrack(String value) {
        _track = value;
    }
    public void setYear(String value) {
        _year = value;
    }
}
