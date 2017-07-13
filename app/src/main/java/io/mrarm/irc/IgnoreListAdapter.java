package io.mrarm.irc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import io.mrarm.irc.util.ColoredTextBuilder;

public class IgnoreListAdapter extends RecyclerView.Adapter<IgnoreListAdapter.ItemHolder> {

    private ServerConfigData mServer;
    private int mTextColorSecondary;
    private int mTextColorNick;
    private int mTextColorUser;
    private int mTextColorHost;

    public IgnoreListAdapter(Context context, ServerConfigData server) {
        TypedArray ta = context.obtainStyledAttributes(R.style.AppTheme,
                new int[]{android.R.attr.textColorSecondary});
        mTextColorSecondary = ta.getColor(0, Color.BLACK);
        ta.recycle();
        mTextColorNick = context.getResources().getColor(R.color.ignoreEntryNick);
        mTextColorUser = context.getResources().getColor(R.color.ignoreEntryUser);
        mTextColorHost = context.getResources().getColor(R.color.ignoreEntryHost);
        mServer = server;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simple_list_item, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        holder.bind(mServer.ignoreList.get(position));
    }

    @Override
    public int getItemCount() {
        return mServer.ignoreList == null ? 0 : mServer.ignoreList.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        private TextView mText;

        public ItemHolder(View itemView) {
            super(itemView);
            mText = (TextView) itemView;
            mText.setOnClickListener((View v) -> {
                startEdit();
            });
            mText.setOnLongClickListener((View v) -> {
                Context context = v.getContext();
                new AlertDialog.Builder(context)
                        .setTitle(mText.getText())
                        .setItems(new CharSequence[] {
                                context.getString(R.string.action_edit),
                                context.getString(R.string.action_delete)
                        }, (DialogInterface i, int which) -> {
                            if (which == 0) {
                                startEdit();
                            } else if (which == 1) {
                                mServer.ignoreList.remove(getAdapterPosition());
                                notifyItemRemoved(getAdapterPosition());
                                try {
                                    ServerConfigManager.getInstance(context).saveServer(mServer);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show();
                return true;
            });
        }

        private void startEdit() {
            Intent intent = new Intent(mText.getContext(), EditIgnoreEntryActivity.class);
            intent.putExtra(EditIgnoreEntryActivity.ARG_SERVER_UUID, mServer.uuid.toString());
            intent.putExtra(EditIgnoreEntryActivity.ARG_ENTRY_INDEX, getAdapterPosition());
            mText.getContext().startActivity(intent);
        }

        public void bind(ServerConfigData.IgnoreEntry entry) {
            ColoredTextBuilder builder = new ColoredTextBuilder();
            if (entry.nick == null || entry.nick.equals("*"))
                builder.append("*", new ForegroundColorSpan(mTextColorSecondary));
            else
                builder.append(entry.nick, new ForegroundColorSpan(mTextColorNick));

            builder.append("!");
            if (entry.user == null || entry.user.equals("*"))
                builder.append("*", new ForegroundColorSpan(mTextColorSecondary));
            else
                builder.append(entry.user, new ForegroundColorSpan(mTextColorUser));

            builder.append("@");
            if (entry.host == null || entry.host.equals("*"))
                builder.append("*", new ForegroundColorSpan(mTextColorSecondary));
            else
                builder.append(entry.host, new ForegroundColorSpan(mTextColorHost));

            if (entry.comment != null) {
                builder.append(" ");
                builder.append(entry.comment, new ForegroundColorSpan(mTextColorSecondary));
            }
            mText.setText(builder.getSpannable());
        }

    }

}
