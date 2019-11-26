package spartons.com.googlemapspolylineexample.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import spartons.com.googlemapspolylineexample.MainActivity;
import spartons.com.googlemapspolylineexample.R;

/**
 * Created by User on 2/12/2018.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";

    //vars
    private ArrayList<String> mNames = new ArrayList<>();
    private Context mContext;
    private LatLng mOrigin;
    private LatLng mDestination;




    public RecyclerViewAdapter(Context context, ArrayList<String> names, LatLng Origin, LatLng Destination ) {
        mNames = names;
        mContext = context;
        mOrigin = Origin;
        mDestination = Destination;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        holder.number.setText(String.valueOf(position+1));
        holder.name.setText(mNames.get(position));




        holder.number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on an image: " + mNames.get(position));

                String origin = mOrigin.latitude + "," + mOrigin.longitude;
                String destination = mDestination.latitude + "," + mDestination.longitude;
                ((MainActivity) mContext).fetchDirections(origin, destination);
            }
        });



    }

    @Override
    public int getItemCount() {
        return mNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView number;
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.number);
            name = itemView.findViewById(R.id.name);
        }
    }

}
