package com.savemaster.smlib;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.savemaster.pklib.BackgroundThread;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FileFragment extends Fragment {
    View rootView;
    public static final String TAG = "FileFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.savef_v_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;
        initView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rootView.post(new Runnable() {
            @Override
            public void run() {
//                listener();
//                loadNative();
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();

    }

    RecyclerView recyclerView;
    FileAdapter adapter;
    List<File> fileList = new ArrayList();

    FrameLayout banner_container_file;
    public void initView(){
        recyclerView = rootView.findViewById(R.id.recycler_view);
        adapter = new FileAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        banner_container_file = rootView.findViewById(R.id.banner_container_file);
        adapter.addAll(fileList);
        loadData();
    }

    public void loadDataIfNO(){
        loadData();
    }

    public void loadData(){
        BackgroundThread.post(new Runnable() {
            @Override
            public void run() {
                if(getActivity() == null || getActivity().isFinishing()){
                    return;
                }
                File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if(dir == null || dir.listFiles() == null){
                    return;
                }
                List<File> tempList = new ArrayList();
                tempList.clear();

                for(File file : dir.listFiles()){
//                    if(file.getName().endsWith(Commons.VOICE_TEMP)){
////                        tempList.add(file);
//                    } else {
                        tempList.add(file);
//                    }
                }
//                fileList.addAll(Arrays.asList());
//                Collections.sort(tempList, new FileComparator());

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fileList.clear();
                        fileList.addAll(tempList);
                        adapter.addAll(fileList);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    class FileAdapter extends RecyclerView.Adapter{
        List<File> allFiles = new ArrayList<>();
        public void addAll(List<File> lists){
            if(lists == null || lists.isEmpty()){
                return;
            }
            allFiles.clear();
            allFiles.addAll(lists);
        }

        public List<File> getAllFiles() {
            return allFiles;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.savef_file_item_v,parent,false);
            FileItemViewHolder viewHolder = new FileItemViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            final File file = allFiles.get(position);
            ((FileItemViewHolder)holder).fileNameTv.setText(file.getName());
            ((FileItemViewHolder)holder).fileDateTv.setText(format(file.lastModified()));
            ((FileItemViewHolder)holder).fileSizeTv.setText(readableFileSize(file.length()));

            holder.itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    openFile(file);
                }
            });

            ((FileItemViewHolder)holder).audio_more_action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    showPop(view, file, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return allFiles.size();
        }
    }

    public static String format(long timel){
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateTime=df.format(new Date(timel));
        return dateTime;
    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    class FileItemViewHolder extends RecyclerView.ViewHolder{
        TextView fileNameTv;
        TextView fileSizeTv;
        TextView fileDateTv;
        ImageView audio_more_action;
        public FileItemViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTv = itemView.findViewById(R.id.file_name);
            fileSizeTv = itemView.findViewById(R.id.file_size);
            fileDateTv = itemView.findViewById(R.id.file_date);
            audio_more_action = itemView.findViewById(R.id.audio_more_action);
        }
    }

    private void openFile(File file){
        try{

            int lastindex = file.getName().lastIndexOf('.');
//            if(lastindex > 0 && lastindex < file.getName().length()){
//                String na = file.getName().substring(0, file.getName().lastIndexOf('.'));
//                String tempName = file.getParent() + "/" + na + Commons.VOICE_TEMP;
//                if(new File(tempName).exists()){
//                    Toast.makeText(getActivity(), getString(R.string.no_voice_alert),
//                            Toast.LENGTH_LONG).show();
//                }
//            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri= FileProvider.getUriForFile(rootView.getContext(),
                    "save.from.net.tubevideodownloader.savefrom.net" + ".provider",file);

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            String extension = getExtension(file.getName());
            if("gif".equalsIgnoreCase(extension)
                    || "jpg".equalsIgnoreCase(extension) || "png".equalsIgnoreCase(extension)){
                intent.setDataAndType(uri, "image/*");
            } else {
                intent.setDataAndType(uri, "video/*");
            }

            rootView.getContext().startActivity(intent);
        }catch (IllegalArgumentException e){
            Toast.makeText(rootView.getContext(), "error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static String getExtension(String fileName) {
        char ch;
        int len;
        if(fileName==null ||
                (len = fileName.length())==0 ||
                (ch = fileName.charAt(len-1))=='/' || ch=='\\' || //in the case of a directory
                ch=='.' ) //in the case of . or ..
            return "";
        int dotInd = fileName.lastIndexOf('.'),
                sepInd = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        if( dotInd<=sepInd )
            return "";
        else
            return fileName.substring(dotInd+1).toLowerCase();
    }
}
