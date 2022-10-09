package com.example.s_mentor.ui.board;

import android.view.View;


public interface OnItemClickListener {
    void onItemClick(BoardAdapter.UserViewHolder holder, View view, int position);
}
