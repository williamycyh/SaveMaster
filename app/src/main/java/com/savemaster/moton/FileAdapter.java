package com.savemaster.moton;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.savemaster.smlib.FileItem;
import com.savemaster.savefromfb.R;

import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends BaseAdapter {
    Context mContext;
    LayoutInflater inflater;
    List<ListData> lists = new ArrayList<>();

    public FileAdapter(Context context, List<ListData> datas){
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        lists = datas;
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int i) {
        return lists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        myViewHolder hv;
        if (null==convertView){
            hv = new myViewHolder();
            convertView = inflater.inflate(R.layout.savemasterdown_m_video_list,null,false);
            hv.tvInfo = convertView.findViewById(R.id.video_info);
//            hv.tvProver = convertView.findViewById(R.id.pro_version);
            hv.tvDesc = convertView.findViewById(R.id.desc);
            convertView.setTag(hv);

        }else {
            hv = (myViewHolder)convertView.getTag();
        }
        if (lists!=null && !lists.isEmpty()){
            hv.tvInfo.setText(lists.get(i).name);
//            if(lists.get(i).prov == 1){
//                hv.tvProver.setVisibility(View.VISIBLE);
//            } else {
//                hv.tvProver.setVisibility(View.GONE);
//            }
            if(lists.get(i).item.getVoiceFile() != null){
                hv.tvDesc.setVisibility(View.VISIBLE);
            } else {
                hv.tvDesc.setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    static class myViewHolder{
        TextView tvInfo;
//        TextView tvProver;
        TextView tvDesc;
    }

    public static class ListData{
        public String name;
        public int prov;
        public FileItem item;
    }
}

