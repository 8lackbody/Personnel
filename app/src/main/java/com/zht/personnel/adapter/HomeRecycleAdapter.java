package com.zht.personnel.adapter;


import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.zht.personnel.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/*
① 创建一个继承RecyclerView.Adapter<VH>的Adapter类
② 创建一个继承RecyclerView.ViewHolder的静态内部类
③ 在Adapter中实现3个方法：
   onCreateViewHolder()
   onBindViewHolder()
   getItemCount()
*/
public class HomeRecycleAdapter extends RecyclerView.Adapter<HomeRecycleAdapter.MyViewHolder> {

    private Context context;
    private List<EPCTag> tags;
    private View inflater;

    //构造方法，传入数据
    public HomeRecycleAdapter(Context context, List<EPCTag> tags) {
        this.context = context;
        this.tags = tags;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //创建ViewHolder，返回每一项的布局
        inflater = LayoutInflater.from(context).inflate(R.layout.home_item, parent, false);
        return new MyViewHolder(inflater);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        //将数据和控件绑定
        holder.textEpc.setText(tags.get(position).getEpc());
        holder.textName.setText(tags.get(position).getName());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        holder.textDate.setText(sdf.format(new Date(Long.parseLong(tags.get(position).getDate()))));
        if (tags.get(position).getAlert() == 1) {
            if (tags.get(position).getStatus().equals("已确认")) {
                holder.textstats.setTextColor(context.getColor(R.color.confirm));
                holder.textstats.setText(tags.get(position).getStatus());
            } else {
                holder.textstats.setTextColor(context.getColor(R.color.white));
                holder.textstats.setText(tags.get(position).getStatus());
            }
            startMp3();
            holder.itemView.setBackground(context.getDrawable(R.drawable.warning));
        } else {
            if (tags.get(position).getStatus().equals("已确认")) {
                holder.textstats.setTextColor(context.getColor(R.color.confirm));
                holder.textstats.setText(tags.get(position).getStatus());
            } else {
                holder.textstats.setTextColor(context.getColor(R.color.ksw_md_solid_disable));
                holder.textstats.setText(tags.get(position).getStatus());
            }
            holder.itemView.setBackground(null);
        }

    }

    protected void startMp3() {

    }

    @Override
    public int getItemCount() {
        //返回Item总条数
        return tags.size();
    }

    public void setList(List<EPCTag> tags) {
        this.tags = tags;
    }

    //内部类，绑定控件
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        TextView textEpc;
        TextView textDate;
        TextView textstats;

        public MyViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_name);
            textEpc = itemView.findViewById(R.id.text_epc);
            textDate = itemView.findViewById(R.id.text_date);
            textstats = itemView.findViewById(R.id.text_status);
        }
    }
}
