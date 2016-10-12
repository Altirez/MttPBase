package com.logistic.paperrose.mttp.oldversion.reports;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.logistic.paperrose.mttp.R;

import java.util.ArrayList;


/**
 * Created by paperrose on 24.12.2014.
 */
public class ReportTreeListViewAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    public ArrayList<ReportItemNode> objects;

    public ReportTreeListViewAdapter(Context context, int layoutResourceID, ArrayList<ReportItemNode> products) {
        ctx = context;
        objects = products;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }


    public void showItemChildNodes(ReportItemNode node) {
        node.opened = true;
        ArrayList<ReportItemNode> nodes = node.getChildNodes();
        for (ReportItemNode p : nodes) {
            changeItemNodeVisibility(p, true);
            getView(p.position, null, null);

        }
        notifyDataSetChanged();
    }

    private int getItemOffset(ReportItemNode node) {
        return 40*(node.getItemNodeLevel() > 0 ? 1 : 0) + 5 + node.getItemNodeLevel()*20;
    }

    public void changeItemNodeVisibility(ReportItemNode node, boolean b) {
        node.isVisible = b;
        notifyDataSetChanged();
    }

    public boolean hasNode(ReportItemNode parent, ReportItemNode child) {
        ArrayList<ReportItemNode> nodes = parent.getChildNodes();
        if (parent.equals(child)) return true;
        for (ReportItemNode p : nodes) {
            if (hasNode(p, child)) {
                return true;
            }
        }
        return false;
    }

    public void hideItemChildNodes(ReportItemNode node) {
        ArrayList<ReportItemNode> nodes = node.getChildNodes();
        node.opened = false;
        for (ReportItemNode p : nodes) {
            hideItemChildNodes(p);
            changeItemNodeVisibility(p, false);
            getView(p.position, null, null);
        }
        notifyDataSetChanged();
    }
    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        ReportItemNode p = getProduct(position);

        View view = convertView;
        if (view == null) {

            view = lInflater.inflate(R.layout.report_tree_node_item, null);
        }


        ImageView imageView = (ImageView) view.findViewById(R.id.topLevelNodeImage);
        imageView.setVisibility((p.getChildNodes().size() == 0) ? View.GONE : View.VISIBLE);
        TextView textView = (TextView) view.findViewById(R.id.description);
        textView.setTextSize(22 - 2*p.getItemNodeLevel());
        textView.setText(p.description);
        textView.setPadding(getItemOffset(p) + ((p.getChildNodes().size() == 0) ? 40 : 0), 5, 5, 5);
        TextView textViewCount = (TextView) view.findViewById(R.id.count);
        textViewCount.setTextSize(20 - 2*p.getItemNodeLevel());
        textViewCount.setText(p.count);
        LinearLayout parentLayout = (LinearLayout) view.findViewById(R.id.parentLayout);
        if (p.isVisible) {
            parentLayout.setVisibility(View.VISIBLE);
            imageView.setImageResource(p.image);
        } else {
            parentLayout.setVisibility(View.GONE);
            imageView.setImageResource(p.image2);
        }
        return view;
    }



    // товар по позиции
    ReportItemNode getProduct(int position) {
        return ((ReportItemNode) getItem(position));
    }
}
