package kg.kloop.android.redbutton.groups;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alexwalker.sendsmsapp.R;

import java.util.ArrayList;

public class GroupListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<GroupMembership> groupList;
    private Fragment fragment;

    public GroupListAdapter(Context context, ArrayList<GroupMembership> list, Fragment fragment) {
        this.context = context;
        this.groupList = list;
        this.fragment = fragment;
    }

    @Override
    public int getCount() {
        return groupList.size();
    }

    @Override
    public Object getItem(int position) {
        return groupList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_list_item, parent, false);
        }

        TextView groupName = (TextView) convertView.findViewById(R.id.group_item_groupname);
        final TextView membershipInfo = (TextView) convertView.findViewById(R.id.group_item_membership);
        final ImageView image = (ImageView) convertView.findViewById(R.id.group_item_image);

        final GroupMembership thisGroup = groupList.get(position);

        groupName.setText(thisGroup.getGroupName());
        boolean isMember = thisGroup.isMember();
        boolean isPending = thisGroup.isPending();

        if (isMember) {
            image.setImageResource(R.drawable.member);
            membershipInfo.setText("вы состоите в этой группе");
        } else if (isPending) {
            image.setImageResource(R.drawable.pending);
            membershipInfo.setText("запрос отправлен");
        } else {
            image.setImageResource(R.drawable.send_request);
        }

        final boolean finalIsPending = isPending;
        final boolean finalIsMember = isMember;

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //if clicked on Tab2
                if (fragment instanceof Tab2) {
                    GroupMembership group = groupList.get(position);

                    // если является членом группы
                    if (group.isMember()) {
                        ((Tab2) fragment).checkGroupAndUserStatus(group.getGroupName());
                    } else {
                        Toast.makeText(context, "Ваша заявка еще на рассмотрении", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if clicked on Tab1
                if (fragment instanceof Tab1) {
                    if (!finalIsMember && !finalIsPending) {
                        createSendRequestAlertDialog(context, fragment, thisGroup);
                    }
                }
                //if clicked on Tab2
                if (fragment instanceof Tab2) {

                }
            }
        });

        return convertView;
    }

    private void createSendRequestAlertDialog(Context context, final Fragment fragment, final GroupMembership groupMembership) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Отправить запрос в группу " + groupMembership.getGroupName() + "?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((Tab1) fragment).sendRequest(groupMembership.getGroupName());
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertdialog = builder.create();
        alertdialog.show();
    }
}