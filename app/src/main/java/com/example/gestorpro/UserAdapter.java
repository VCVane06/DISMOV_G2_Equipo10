package com.example.gestorpro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestorpro.database.Usuario;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<Usuario> userList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onEditClick(Usuario usuario);
        void onDeleteClick(Usuario usuario);
    }

    public UserAdapter(List<Usuario> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    public void setUserList(List<Usuario> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Usuario usuario = userList.get(position);
        holder.bind(usuario, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole;
        ImageView ivPhoto, btnEdit, btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserNameItem);
            tvEmail = itemView.findViewById(R.id.tvUserEmailItem);
            tvRole = itemView.findViewById(R.id.tvUserRoleItem);
            ivPhoto = itemView.findViewById(R.id.ivUserPhoto);
            btnEdit = itemView.findViewById(R.id.btnEditUser);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
        }

        public void bind(Usuario usuario, OnUserClickListener listener) {
            tvName.setText(usuario.getNombre());
            tvEmail.setText(usuario.getCorreo());
            tvRole.setText(usuario.getRol());
            
            btnEdit.setOnClickListener(v -> listener.onEditClick(usuario));
            btnDelete.setOnClickListener(v -> listener.onDeleteClick(usuario));
        }
    }
}
