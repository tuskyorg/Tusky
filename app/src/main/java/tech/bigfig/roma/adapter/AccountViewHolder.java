package tech.bigfig.roma.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import tech.bigfig.roma.R;
import tech.bigfig.roma.entity.Account;
import tech.bigfig.roma.interfaces.AccountActionListener;
import tech.bigfig.roma.interfaces.LinkListener;
import tech.bigfig.roma.util.CustomEmojiHelper;
import com.squareup.picasso.Picasso;

class AccountViewHolder extends RecyclerView.ViewHolder {
    private TextView username;
    private TextView displayName;
    private ImageView avatar;
    private String accountId;

    AccountViewHolder(View itemView) {
        super(itemView);
        username = itemView.findViewById(R.id.account_username);
        displayName = itemView.findViewById(R.id.account_display_name);
        avatar = itemView.findViewById(R.id.account_avatar);
    }

    void setupWithAccount(Account account) {
        accountId = account.getId();
        String format = username.getContext().getString(R.string.status_username_format);
        String formattedUsername = String.format(format, account.getUsername());
        username.setText(formattedUsername);
        CharSequence emojifiedName = CustomEmojiHelper.emojifyString(account.getName(), account.getEmojis(), displayName);
        displayName.setText(emojifiedName);
        Context context = avatar.getContext();
        Picasso.with(context)
                .load(account.getAvatar())
                .placeholder(R.drawable.avatar_default)
                .into(avatar);
    }

    void setupActionListener(final AccountActionListener listener) {
        itemView.setOnClickListener(v -> listener.onViewAccount(accountId));
    }

    void setupLinkListener(final LinkListener listener) {
        itemView.setOnClickListener(v -> listener.onViewAccount(accountId));
    }
}