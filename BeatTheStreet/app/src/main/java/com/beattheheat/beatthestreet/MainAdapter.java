package com.beattheheat.beatthestreet;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beattheheat.beatthestreet.Networking.LocationWrapper;
import com.beattheheat.beatthestreet.Networking.OC_API.OCBus;
import com.beattheheat.beatthestreet.Networking.OC_API.OCStop;
import com.beattheheat.beatthestreet.Networking.OC_API.OCTranspo;
import com.beattheheat.beatthestreet.Networking.SCallable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by Matt on 2017-11-18.
 *
 * Adapter to display trips in MainActivity
 */

// TODO: Show only favorites if no location available
class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private Context context;
    private OCTranspo octAPI;
    private FavoritesStorage faveRoutes;
    private ArrayList<MainAdapterHelper> tripCollection;
    private Location user;
    private Comparator<OCStop> locationSort;

    MainAdapter(Context context) {
        this.context = context;
        this.octAPI = OCTranspo.getInstance();
        this.faveRoutes = new FavoritesStorage(context);
        this.tripCollection = new ArrayList<>();
        this.user = LocationWrapper.getInstance().getLocation();

        // Set up a comparator to sort stops by distance from user
        locationSort = new Comparator<OCStop>() {
            @Override
            public int compare(OCStop o1, OCStop o2) {
                if (user == null) {
                    // Sort by stopCode if we don't have location permission
                    return o1.getStopCode() - o2.getStopCode();
                }

                float dist1 = o1.getLocation().distanceTo(user);
                float dist2 = o2.getLocation().distanceTo(user);

                return (int)(dist1 - dist2);
            }
        };

        // Prepare data for the adapter
        prepareList();
        sortFavorites(tripCollection);
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_layout, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MainViewHolder viewHolder, int position) {
        // Set the route name, number, and stop name
        String routeNumberName = tripCollection.get(position).routeNumberName;
        String stopName = tripCollection.get(position).stopName;
        viewHolder.routeNumberName.setText(routeNumberName);
        viewHolder.stopName.setText(stopName);

        // Set up each upcoming trip
        String minsTillArrival;
        String busTimeIsLive = "GPS";
        final OCBus[] currentList = tripCollection.get(position).busArray;
        for (int i = 0; i < currentList.length; i++) {
            final TextView currentTrip = viewHolder.trips[i];
            final CardView currentCard = viewHolder.cards[i];

            // Set the time till arrival
            minsTillArrival = currentList[i].getMinsTilArrival() + " min";
            currentTrip.setText(minsTillArrival);

            // Set whether time is live
            if (currentList[i].isTimeLive())
                viewHolder.gps[i].setText(busTimeIsLive);

            // Set up a click listener so we can set an alarm for this trip
            // TODO: Implement alarm here
            currentTrip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO: Replace this with something better
                    currentCard.setBackgroundColor(Color.BLACK);
                }
            });
        }

        // Set whether we start with a fav or unfav icon
        final String routeNumber = tripCollection.get(position).routeNumber;
        if (faveRoutes.isFav(routeNumber, FavoritesStorage.FAV_TYPE.ROUTE))
            viewHolder.favIcon.setBackgroundResource(R.drawable.ic_favorite);
        else
            viewHolder.favIcon.setBackgroundResource(R.drawable.ic_unfavorite);

        // Favorite/Unfavorite functionality
        viewHolder.favIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (faveRoutes.toggleFav(routeNumber, FavoritesStorage.FAV_TYPE.ROUTE)) {
                    // Route was added to favorites
                    viewHolder.favIcon.setImageResource(R.drawable.ic_favorite);
                } else {
                    // Route was removed from favorites
                    viewHolder.favIcon.setImageResource(R.drawable.ic_unfavorite);
                }
            }
        });

        // TODO: this
        // Click listener to go to timetable for chosen route at current stop
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get route number and pass it back to MainActivity
                ((MainActivity)context).onClick("" + currentList[0].getRouteNo());
            }
        });
    }

    @Override
    public int getItemCount() { return tripCollection.size(); }

    // Update the adapter with a new list of data
    private void updateCollection(ArrayList<MainAdapterHelper> newList) {
        // Remove entries from newList that are already in the tripCollection
        for (MainAdapterHelper newRoute : newList) {
            for (MainAdapterHelper oldRoute : tripCollection) {
                if (newRoute.routeNumberName.equals(oldRoute.routeNumberName)) {
                    newList.remove(newRoute);
                }
            }
        }

        tripCollection.addAll(newList);
        tripCollection = sortFavorites(tripCollection);
        notifyDataSetChanged();
    }

    // Take in a list of stops and return a location-sorted list of stops within a given range
    private ArrayList<OCStop> sortStops(ArrayList<OCStop> inList) {
        ArrayList<OCStop> outList = new ArrayList<>();

        // We only want stops within a certain range
        if (user != null) {
            for (OCStop stop : inList) {
                if (stop.getLocation().distanceTo(user) <= 1000)
                    outList.add(stop);
            }

            // Sort our list of nearby stops
            Collections.sort(outList, locationSort);
        }

        return outList;
    }

    private ArrayList<MainAdapterHelper> sortFavorites(ArrayList<MainAdapterHelper> inList) {
        // Separate list into favorites and not favorites
        ArrayList<MainAdapterHelper> favorites = new ArrayList<>();
        ArrayList<MainAdapterHelper> unFavorites = new ArrayList<>();
        for (MainAdapterHelper currentTrip : inList) {
            if (faveRoutes.isFav(currentTrip.routeNumber, FavoritesStorage.FAV_TYPE.ROUTE))
                favorites.add(currentTrip);
            else unFavorites.add(currentTrip);
        }

        // Put the lists back together with favorites being first
        ArrayList<MainAdapterHelper> outList = new ArrayList<>();
        outList.addAll(favorites);
        outList.addAll(unFavorites);

        return outList;
    }

    // Initial setup method
    private void prepareList() {
        /* Get the list of all nearby stops */
        // Get the list of all stops
        ArrayList<OCStop> stopList = new ArrayList<>(octAPI.gtfsTable.getStopList());
        // Sort the list and only keep nearby stops
        stopList = sortStops(stopList);
        // Trim excess quotes from stopName
        for(OCStop stop : stopList)
            stop.setStopName(stop.getStopName().replaceAll("\"", ""));

        /* Get the list of all routes that service nearby stops */
        for (OCStop stop : stopList) {
            // Get the stop name
            final String stopName = stop.getStopName();
            String stopCode = "" + stop.getStopCode(); // Needed for the API call

            // Get every route that services the current stop
            octAPI.GetNextTripsForStopAllRoutes(stopCode, new SCallable<HashMap<Integer, OCBus[]>>() {
                @Override
                public void call(HashMap<Integer, OCBus[]> arg) {
                    ArrayList<MainAdapterHelper> currentCollection = new ArrayList<>();
                    ArrayList<OCBus[]> routeList = new ArrayList<>();
                    // Filter out routes that have no upcoming stops
                    for (OCBus[] busArray : arg.values()) {
                        if (busArray != null && busArray.length > 0)
                            routeList.add(busArray);
                    }

                    // Sort ArrayList of OCBus arrays by route number
                    Collections.sort(routeList, new Comparator<OCBus[]>() {
                        public int compare(OCBus[] busArray, OCBus[] otherArray) {
                            return busArray[0].compareTo(otherArray[0]);
                        }
                    });

                    // Add the complete set of information on stop and route to the collection
                    for (OCBus[] busArray : routeList) {
                        currentCollection.add(new MainAdapterHelper(busArray, stopName));
                    }

                    /* Done collecting data for this API call, refresh the adapter */
                    updateCollection(currentCollection);
                }
            });
        }
    }

    /* Helper class to assign route and stop info to the appropriate parts of the layout */
    static class MainViewHolder extends RecyclerView.ViewHolder {
        TextView routeNumberName;
        TextView stopName;
        TextView[]  trips = new TextView[3];
        ImageView[] icons = new ImageView[3];
        TextView[]  gps   = new TextView[3];
        CardView[]  cards = new CardView[3];
        ImageView favIcon;

        MainViewHolder(View itemView) {
            super(itemView);
            routeNumberName = itemView.findViewById(R.id.main_route_number_name);
            stopName = itemView.findViewById(R.id.main_stop_name);
            trips[0] = itemView.findViewById(R.id.main_stop_time_0);
            trips[1] = itemView.findViewById(R.id.main_stop_time_1);
            trips[2] = itemView.findViewById(R.id.main_stop_time_2);
            icons[0] = itemView.findViewById(R.id.main_icon_0);
            icons[1] = itemView.findViewById(R.id.main_icon_1);
            icons[2] = itemView.findViewById(R.id.main_icon_2);
            gps[0]   = itemView.findViewById(R.id.main_gps_0);
            gps[1]   = itemView.findViewById(R.id.main_gps_1);
            gps[2]   = itemView.findViewById(R.id.main_gps_2);
            cards[0] = itemView.findViewById(R.id.main_card_0);
            cards[1] = itemView.findViewById(R.id.main_card_1);
            cards[2] = itemView.findViewById(R.id.main_card_2);
            favIcon  = itemView.findViewById(R.id.main_fav_button);
        }
    }

    /* Helper class so we can easily pass information to onBindViewHolder */
    private class MainAdapterHelper {
        OCBus[] busArray;
        String stopName;
        String routeNumber;
        String routeNumberName;

        MainAdapterHelper(OCBus[] busArray, String stopName) {
            this.busArray = busArray;
            this.stopName = stopName;
            this.routeNumber = "" + busArray[0].getRouteNo();
            this.routeNumberName = routeNumber + " " + busArray[0].getRouteHeading();
        }
    }
}
