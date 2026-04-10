package com.socialApp.demo.utils;

import org.springframework.stereotype.Component;

@Component
public class CacheKeyUtils {
    private final String roomKeyPrefix = "app:room:";

    public String roomGeoKey(){
        return roomKeyPrefix + "geo";
    }

    public String roomDetailKey(Long roomId){
        return roomKeyPrefix + "details:" + roomId;
    }

    public String roomMetaDataKey(Long roomId) { return  roomKeyPrefix + "meta" + roomId;}
}
