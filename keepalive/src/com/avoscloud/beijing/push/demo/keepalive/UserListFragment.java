package com.avoscloud.beijing.push.demo.keepalive;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.avos.avoscloud.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class UserListFragment extends Fragment {

  ListView onlineUserListView;
  View joinGroup;
  List<ChatUser> onlineUsers;
  List<String> peerIds;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.onlinelist, null);
    onlineUserListView = (ListView) rootView.findViewById(R.id.onlineList);
    joinGroup = rootView.findViewById(R.id.add_new);
    joinGroup.setVisibility(View.GONE);
    final String selfId = AVUser.getCurrentUser().getObjectId();
    if (onlineUsers == null) {
      onlineUsers = new LinkedList<ChatUser>();
    }

    // 取出所有用户，显示出来
    AVQuery<AVUser> q = AVUser.getQuery();
    q.setLimit(1000);
    q.orderByDescending("updatedAt");
    q.findInBackground(new FindCallback<AVUser>() {
      @Override
      public void done(List<AVUser> parseObjects, AVException parseException) {
        if (parseException == null) {
          if (!onlineUsers.isEmpty()) {
            onlineUsers.clear();
          }
          peerIds = new LinkedList<String>();
          for (AVUser o : parseObjects) {
            HTBApplication.registerLocalNameCache(o.getObjectId(),
                o.getUsername());
            if (!o.getObjectId().equals(selfId)) {
              ChatUser u = new ChatUser();
              u.objectId = o.getObjectId();
              u.username = o.getUsername();
              onlineUsers.add(u);
              peerIds.add(o.getObjectId());
            }
          }
          // max watch people=100
          if (peerIds.size() > 100) {
            peerIds = peerIds.subList(0, 100);
          }
          SessionManager.getInstance(selfId)
              .watchPeers(peerIds);

          UserListAdapter adapter = new UserListAdapter(getActivity(), onlineUsers);
          onlineUserListView.setAdapter(adapter);
          onlineUserListView.setOnItemClickListener(adapter);
        }
      }
    });
    return rootView;
  }

  public static class ChatUser {
    String objectId;
    String username;
  }

  public static class UserListAdapter extends BaseAdapter implements OnItemClickListener {

    public UserListAdapter(Context context, List<ChatUser> users) {
      this.onlineUsers = users;
      this.mContext = context;
      random = new Random();
    }

    Context mContext;
    List<ChatUser> onlineUsers;
    Random random;

    @Override
    public int getCount() {
      return onlineUsers.size();
    }

    @Override
    public ChatUser getItem(int position) {
      // TODO Auto-generated method stub
      return onlineUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder holder = null;
      if (convertView == null) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_target, null);
        holder = new ViewHolder();
        holder.username = (TextView) convertView.findViewById(R.id.onlinetarget);
        holder.avatar = (ImageView) convertView.findViewById(R.id.online_icon);
        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }
      int avatarColor =
          Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

      holder.avatar.setBackgroundColor(avatarColor);
      holder.username.setText(this.getItem(position).username);

      return convertView;
    }

    public class ViewHolder {
      TextView username;
      ImageView avatar;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long itemId) {
      ChatUser u = this.getItem(position);
      Intent i = new Intent(mContext, PrivateConversationActivity.class);
      i.putExtra(PrivateConversationActivity.DATA_EXTRA_SINGLE_DIALOG_TARGET, u.objectId);
      mContext.startActivity(i);
      ((Activity) mContext).overridePendingTransition(android.R.anim.slide_in_left,
          android.R.anim.slide_out_right);
    }
  }

}
