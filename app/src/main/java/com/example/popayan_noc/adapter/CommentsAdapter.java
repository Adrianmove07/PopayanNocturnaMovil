package com.example.popayan_noc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.popayan_noc.R;

import org.json.JSONObject;
import java.util.ArrayList;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private ArrayList<JSONObject> comments;

    public CommentsAdapter() {
        comments = new ArrayList<>();
    }

    public void setComments(ArrayList<JSONObject> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        JSONObject comment = comments.get(position);
        try {
            // Obtener el nombre del usuario
            JSONObject usuario = comment.getJSONObject("usuario");
            String userName = usuario.getString("nombre");
            holder.tvCommentUser.setText(userName);
            
            // Obtener el contenido del comentario
            String commentContent = comment.getString("comentario");
            holder.tvCommentContent.setText(commentContent);
            
            // Obtener el nombre del lugar
            JSONObject evento = comment.getJSONObject("evento");
            String placeName = evento.getString("nombre");
            holder.tvCommentDate.setText("En: " + placeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentUser, tvCommentContent, tvCommentDate;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentUser = itemView.findViewById(R.id.tvCommentUser);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
            tvCommentDate = itemView.findViewById(R.id.tvCommentDate);
        }
    }
}
