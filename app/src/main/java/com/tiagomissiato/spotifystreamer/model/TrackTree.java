package com.tiagomissiato.spotifystreamer.model;

import java.io.Serializable;

/**
 * Created by tiagomissiato on 9/3/15.
 */
public class TrackTree implements Serializable{

    public Track track;

    public void addNode(int pos, Track newTrack) {

        if (track == null) {
            track = newTrack;
        } else {
            Track focusNode = track;

            Track parent;

            while (true) {

                parent = focusNode;

                if (pos < focusNode.pos) {

                    focusNode = focusNode.prev;

                    if (focusNode == null) {
                        parent.prev = newTrack;
                        return;
                    }
                } else {
                    focusNode = focusNode.next;
                    if (focusNode == null) {
                        parent.next = newTrack;
                        return;
                    }
                }
            }

        }
    }

    public Track findNode(int pos){

        Track focusNode = track;

        while (focusNode.pos != pos) {

            if (pos < focusNode.pos) {
                focusNode = focusNode.prev;
            } else {
                // Shift the focus Node to the right child
                focusNode = focusNode.next;
            }

        }

        return focusNode;

    }

}
