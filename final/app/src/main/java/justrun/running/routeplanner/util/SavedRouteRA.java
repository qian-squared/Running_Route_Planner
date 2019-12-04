package justrun.running.routeplanner.util;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import justrun.running.routeplanner.MainActivity;
import justrun.running.routeplanner.R;

public class SavedRouteRA  extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter2";
    private List<String> mNames = new ArrayList<>();
    private Context mContext;


    public SavedRouteRA(Context context, List<String> names ) {
        mNames = names;
        mContext = context;
    }
    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        return new RecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        holder.number.setText(mNames.get(position));
        // holder.name.setText(mNames.get(position));


        holder.number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on an image: " + mNames.get(position));
                ((MainActivity) mContext).displayMyRoutes(position, -1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView number;

        public ViewHolder(View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.number);
        }
    }
}
