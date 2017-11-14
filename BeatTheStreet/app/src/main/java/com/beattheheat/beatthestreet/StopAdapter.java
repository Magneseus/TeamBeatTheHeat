package com.beattheheat.beatthestreet;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beattheheat.beatthestreet.Networking.LocationWrapper;
import com.beattheheat.beatthestreet.Networking.OC_API.OCStop;

import java.lang.reflect.Array;
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
    Comparator<OCStop> locationSort;

     StopAdapter(final Context context, ArrayList<OCStop> stopList) {
         this.context = context;
         this.stops = stopList;

         locationSort = new Comparator<OCStop>() {
             @Override
             public int compare(OCStop o1, OCStop o2) {
                 Location user = LocationWrapper.getInstance(context).getLocation();
                 if (user == null) {
                     // Try to sort by stopCode if GPS isn't available
                     user = new Location("NO_GPS");
                     //user.setLongitude(-75.696353);
                     //user.setLatitude(45.384906);
                     return o1.getStopCode() - o2.getStopCode();
                 }

                 float dist1 = o1.getLocation().distanceTo(user);
                 float dist2 = o2.getLocation().distanceTo(user);

                 return (int)(dist1 - dist2);
             }
         };

         Collections.sort(stops, locationSort);
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
        viewHolder.stopName.setText(stops.get(position).getStopName());
        viewHolder.stopCode.setText("" + stops.get(position).getStopCode());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get stopCode and pass it back to DisplayStopsActivity
                ((MainActivity)context).onClick(viewHolder.stopCode.getText().toString());
            }
        });
    }

    @Override
    public int getItemCount() { return stops.size(); }

    /* Helper class that takes OCStop info and
       puts it into the layout at the appropriate position */
    static class StopViewHolder extends RecyclerView.ViewHolder {
        TextView stopName;
        TextView stopCode;

        StopViewHolder(View itemView) {
            super(itemView);
            stopName = (TextView)itemView.findViewById(R.id.stop_name);
            stopCode = (TextView)itemView.findViewById(R.id.stop_code);
        }
    }

    void setFilter(ArrayList<OCStop> newList) {
        //setFilter(newList, null);
        stops = new ArrayList<>(); // Reset our stop list with the new list
        stops.addAll(newList);

        Collections.sort(stops, locationSort); // Resort the list

        notifyDataSetChanged(); // Refresh the adapter
    }

    // Replaces the current list of stops with the ones that match the search
/*    void setFilter(ArrayList<OCStop> newList, Comparator<OCStop> comparator) {
        stops = new ArrayList<>();
        stops.addAll(newList);

        if (comparator != null) {
            Collections.sort(stops, comparator);
        } else {
            Collections.sort(stops);
        }
        notifyDataSetChanged(); // Refresh the adapter
    }*/
}
