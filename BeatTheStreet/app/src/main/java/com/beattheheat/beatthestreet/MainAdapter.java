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

// TODO: Sort by favorites
// TODO: Show only favorites if no location available
class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private Context context;
    private ArrayList<OCStop> stops;
    private Comparator<OCStop> locationSort;
    private Location user;
    private FavoritesStorage faveRoutes;
    private OCTranspo octAPI;
    private ArrayList<OCBus[]> busList;

    MainAdapter(Context context, ArrayList<OCStop> stopList) {
        this.context = context;
        this.user = LocationWrapper.getInstance().getLocation();
        this.faveRoutes = new FavoritesStorage(context);
        octAPI = OCTranspo.getInstance();

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

        // Sort the list
        this.stops = sortStops(stopList);
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_layout, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MainViewHolder viewHolder, int position) {
        String stopCode = "" + stops.get(position).getStopCode();
        final String stopName = stops.get(position).getStopName();

        octAPI.GetNextTripsForStopAllRoutes(stopCode, new SCallable<HashMap<Integer, OCBus[]>>() {
            @Override
            public void call(HashMap<Integer, OCBus[]> arg) {
                busList = new ArrayList<>();
                // Filter out routes that have no upcoming stops
                for (OCBus[] busArray : arg.values()) {
                    if (busArray != null && busArray.length > 0)
                        busList.add(busArray);
                }

                // Sort ArrayList of OCBus arrays by route number
                Collections.sort(busList, new Comparator<OCBus[]>() {
                    public int compare(OCBus[] busArray, OCBus[] otherArray) {
                        return busArray[0].compareTo(otherArray[0]);
                    }
                });

                OCBus[] currentList = busList.get(viewHolder.getAdapterPosition());

                // Set the route number and name
                final String routeNumber = "" + currentList[0].getRouteNo();
                String routeName = "" + currentList[0].getRouteHeading();
                String routeNumberName = (routeNumber + " " + routeName);
                viewHolder.routeNumberName.setText(routeNumberName);

                // Set the stop name
                viewHolder.stopName.setText(stopName);

                // Set up the trip views
                String minsTilArrival;
                String busTimeIsLive = "GPS";
                for (int i = 0; i < currentList.length; i++) {
                    final TextView currentTrip = viewHolder.trips[i];
                    final CardView currentCard = viewHolder.cards[i];

                    // Set time till arrival
                    minsTilArrival = currentList[i].getMinsTilArrival() + " min";
                    currentTrip.setText(minsTilArrival);

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

                // Click listener to go to timetable for chosen route at current stop
                // TODO: Set up call to an onClick method in MainActivity

                // Set whether we start with a fav or unfav icon
                if (faveRoutes.isFav(routeNumber, FavoritesStorage.FAV_TYPE.ROUTE))
                    viewHolder.favIcon.setImageResource(R.drawable.ic_favorite);
                else
                    viewHolder.favIcon.setImageResource(R.drawable.ic_unfavorite);

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
            }
        });


    }

    @Override
    public int getItemCount() { return stops.size(); }

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
}
