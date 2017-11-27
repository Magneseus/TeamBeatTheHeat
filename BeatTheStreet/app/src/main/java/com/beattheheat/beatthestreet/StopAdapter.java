package com.beattheheat.beatthestreet;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beattheheat.beatthestreet.Networking.LocationWrapper;
import com.beattheheat.beatthestreet.Networking.OC_API.OCStop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Matt on 2017-10-27
 *
 * Takes data from OCStop objects and puts it into the RecyclerView layout
 */

class StopAdapter extends RecyclerView.Adapter<StopAdapter.StopViewHolder> {

    private Context context;
    private ArrayList<OCStop> stops;
    private Comparator<OCStop> locationSort;
    private FavoritesStorage faveStops;

     StopAdapter(final Context context, ArrayList<OCStop> stopList) {
         this.context = context;
         this.faveStops = new FavoritesStorage(context);

         // Set up a comparator to sort stops by distance from user
         locationSort = new Comparator<OCStop>() {
             @Override
             public int compare(OCStop o1, OCStop o2) {
                 Location user = LocationWrapper.getInstance().getLocation();
                 if (user == null) {
                     // Sort by stopCode if we don't have location permission
                     return o1.getStopCode() - o2.getStopCode();
                 }

                 float dist1 = o1.getLocation().distanceTo(user);
                 float dist2 = o2.getLocation().distanceTo(user);

                 return (int)(dist1 - dist2);
             }
         };

         // Sort the list
         this.stops = sortStops(stopList);
    }

    @Override
    public StopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stop_layout, parent, false);
        return new StopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final StopViewHolder viewHolder , int position) {
        // Set the stop name and code
        final String stopNameStr = stops.get(position).getStopName();
        viewHolder.stopName.setText(stopNameStr);
        final String stopCodeStr = "" + stops.get(position).getStopCode();
        viewHolder.stopCode.setText(stopCodeStr);

        // Set the alarm icon
        viewHolder.alarmIcon.setBackgroundResource(R.drawable.ic_set_alarm);

        // Click listener for alarm icon
        viewHolder.alarmIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Proximity alarm goes here

            }
        });

        // Set whether we start with a fav or unfav icon
        if (faveStops.isFav(stopCodeStr, FavoritesStorage.FAV_TYPE.STOP))
            viewHolder.favIcon.setBackgroundResource(R.drawable.ic_favorite);
        else
            viewHolder.favIcon.setBackgroundResource(R.drawable.ic_unfavorite);

        // Favorite/Unfavorite functionality
        viewHolder.favIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (faveStops.toggleFav(stopCodeStr, FavoritesStorage.FAV_TYPE.STOP)) {
                    // Route was added to favorites
                    viewHolder.favIcon.setBackgroundResource(R.drawable.ic_favorite);
                } else {
                    // Route was removed from favorites
                    viewHolder.favIcon.setBackgroundResource(R.drawable.ic_unfavorite);
                }
            }
        });

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get stopCode and pass it back to DisplayStopsActivity
                ((DisplayStopsActivity)context).onClick(stopCodeStr, stopNameStr);
            }
        });
    }

    @Override
    public int getItemCount() { return stops.size(); }

    /* Helper class that takes OCStop info and
       puts it into the layout at the appropriate position */
    static class StopViewHolder extends RecyclerView.ViewHolder {
        TextView  stopName;
        TextView  stopCode;
        ImageView alarmIcon;
        ImageView favIcon;

        StopViewHolder(View itemView) {
            super(itemView);
            stopName  = itemView.findViewById(R.id.stop_name);
            stopCode  = itemView.findViewById(R.id.stop_code);
            alarmIcon = itemView.findViewById(R.id.stop_alarm_button);
            favIcon   = itemView.findViewById(R.id.stop_fav_button);

        }
    }

    void setFilter(ArrayList<OCStop> newList) {
        /*stops = new ArrayList<>(); // Reset our stop list with the new list
        stops.addAll(newList);

        Collections.sort(stops, locationSort); // Resort the list*/

        stops = sortStops(newList);

        notifyDataSetChanged(); // Refresh the adapter
    }

    // Takes care of sorting a list of stops into favorites and not favorites, both
    // sorted by distance from user
    private ArrayList<OCStop> sortStops(ArrayList<OCStop> inList) {
        // Separate stops into favorites and not favorites
        ArrayList<OCStop> favorites   = new ArrayList<>();
        ArrayList<OCStop> unFavorites = new ArrayList<>();
        for (OCStop stop : inList) {
            if (faveStops.isFav("" + stop.getStopCode(), FavoritesStorage.FAV_TYPE.STOP))
                favorites.add(stop);
            else unFavorites.add(stop);
        }

        // Sort both lists by distance from user
        Collections.sort(favorites, locationSort);
        Collections.sort(unFavorites, locationSort);

        // Put the stop list back together
        ArrayList<OCStop> outList = new ArrayList<>();
        outList.addAll(favorites);
        outList.addAll(unFavorites);

        return outList;
    }
}
