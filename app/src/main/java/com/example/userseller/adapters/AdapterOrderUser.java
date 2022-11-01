package com.example.userseller.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.userseller.R;
import com.example.userseller.models.ModelOrderUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.HolderOrderUser>
{
    private Context context;
    private ArrayList<ModelOrderUser> orderUserList;

    public AdapterOrderUser(Context context, ArrayList<ModelOrderUser> orderUserList) {
        this.context = context;
        this.orderUserList = orderUserList;
    }

    @NonNull
    @Override
    public AdapterOrderUser.HolderOrderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //infllate layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_order_user,parent,false);
        return new HolderOrderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterOrderUser.HolderOrderUser holder, int position) {
        //get data
        ModelOrderUser modelOrderUser=orderUserList.get(position);
        String orderId=modelOrderUser.getOrderId();
        String orderBy=modelOrderUser.getOrderBy();
        String orderCost=modelOrderUser.getOrderCost();
        String orderStatus=modelOrderUser.getOrderStatus();
        String orderTime=modelOrderUser.getOrderTime();
        String orderTo=modelOrderUser.getOrderTo();

        //get shop info
        loadShopInfo(modelOrderUser,holder);

        //set data
        holder.amountTv.setText("Amount: $"+orderCost);
        holder.statusTv.setText(orderStatus);
        holder.orderIdTv.setText("OrderID:"+orderId);
        //change order status text color
        if (orderStatus.equals("In Progress")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.black));
        }
        else if(orderStatus.equals("Completed")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.black));
        }
        else if(orderStatus.equals("Cancelled")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.red));
        }
        //convert timestamp to proper format
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedDate= DateFormat.format("dd/MM/yyyy",calendar).toString();
        holder.dateTv.setText(formatedDate);
    }

    private void loadShopInfo(ModelOrderUser modelOrderUser, HolderOrderUser holder) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(modelOrderUser.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName=""+snapshot.child("ShopName").getValue();
                        holder.shopNameTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderUserList.size();
    }

    //view holder class
    public class HolderOrderUser extends RecyclerView.ViewHolder{
        //views of layout
        private TextView orderIdTv,dateTv,shopNameTv,amountTv,statusTv;


        public HolderOrderUser(@NonNull View itemView) {
            super(itemView);
            //init views of laoyout
            orderIdTv=itemView.findViewById(R.id.orderIdTv);
            dateTv=itemView.findViewById(R.id.dateTv);
            shopNameTv=itemView.findViewById(R.id.shopNameTv);
            amountTv=itemView.findViewById(R.id.amountTv);
            statusTv=itemView.findViewById(R.id.statusTv);
        }
    }
}
