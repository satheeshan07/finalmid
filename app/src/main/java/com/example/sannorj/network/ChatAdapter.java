package com.example.sannorj.network;


import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private int SELF = 100;
    private ArrayList<Message> messageArrayList;


    public ChatAdapter(ArrayList<Message> messageArrayList) {
        this.messageArrayList=messageArrayList;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        // view type render
        // left or right
        if (viewType == SELF) {
            // self
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_self, parent, false);
        } else {
            // Bot
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_watson, parent, false);
        }


        return new ViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageArrayList.get(position);
        if (message.getId()!=null && message.getId().equals("1")) {
            return SELF;
        }

        return position;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Message message = messageArrayList.get(position);
        message.setMessage(message.getMessage());
     //   PicassoImageGetter imageGetter = new PicassoImageGetter(message);
     ((ViewHolder) holder).message.setText(Html.fromHtml(message.getMessage()));
       // ((ViewHolder) holder).message.setText(Html.fromHtml(message.getMessage(), ((message) imageGetter), null));
        //((ViewHolder) holder).message.setText(message.getMessage());
       // URLImageParser p = new URLImageParser(message.getMessage(), this);

        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
    html = (Spannable) Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY, imageGetter, null);
} else {
    html = (Spannable) Html.fromHtml(content, imageGetter, null);
}
mytextView.setText(html);*/
}
//(Html.fromHtml(getText().toString()));
    @Override
    public int getItemCount() {
            return messageArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView message;

        public ViewHolder(View view) {
            super(view);
            message = (TextView) itemView.findViewById(R.id.message);


        }
    }


}