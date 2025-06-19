package com.example.popayan_noc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.popayan_noc.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private final JSONArray commentList;
    private final Context context;

    public CommentAdapter(Context context, JSONArray commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        JSONObject comment = commentList.optJSONObject(position);
        if (comment != null) {
            holder.tvCommentUser.setText(comment.optString("usuario_nombre", "Usuario"));
            holder.tvCommentContent.setText(comment.optString("contenido", ""));
            holder.tvCommentDate.setText(comment.optString("fecha", ""));
            String estado = comment.optString("estado", "aprobado");
            if (!"aprobado".equalsIgnoreCase(estado)) {
                holder.tvCommentStatus.setVisibility(View.VISIBLE);
                holder.tvCommentStatus.setText("Pendiente de aprobación");
            } else {
                holder.tvCommentStatus.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return commentList.length();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentUser, tvCommentContent, tvCommentDate, tvCommentStatus;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentUser = itemView.findViewById(R.id.tvCommentUser);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
            tvCommentDate = itemView.findViewById(R.id.tvCommentDate);
            tvCommentStatus = itemView.findViewById(R.id.tvCommentStatus);
        }
    }
}
