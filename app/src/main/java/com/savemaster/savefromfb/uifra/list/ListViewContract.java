package com.savemaster.savefromfb.uifra.list;

import com.savemaster.savefromfb.uifra.ViewContract;

public interface ListViewContract<I, N> extends ViewContract<I> {
    void showListFooter(boolean show);

    void handleNextItems(N result);
}
