package com.example.s_mentor.ui.boardList;

import android.view.View;


public interface OnItemClickListener {
    void onItemClick(BoardListAdapter.UserViewHolder holder, View view, int position);
}
