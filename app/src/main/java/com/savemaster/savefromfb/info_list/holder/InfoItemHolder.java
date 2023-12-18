package com.savemaster.savefromfb.info_list.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import savemaster.save.master.pipd.InfoItem;

import androidx.recyclerview.widget.RecyclerView;

import com.savemaster.savefromfb.info_list.InfoItemBuilder;

public abstract class InfoItemHolder extends RecyclerView.ViewHolder {
    protected final InfoItemBuilder itemBuilder;

    public InfoItemHolder(InfoItemBuilder infoItemBuilder, int layoutId, ViewGroup parent) {
        super(LayoutInflater.from(infoItemBuilder.getContext()).inflate(layoutId, parent, false));
        this.itemBuilder = infoItemBuilder;
    }

    public abstract void updateFromItem(final InfoItem infoItem);
}
