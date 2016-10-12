package com.logistic.paperrose.mttp.oldversion.menu;

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
public class TreeListViewAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    public ArrayList<ItemNode> objects;

    public TreeListViewAdapter(Context context, int layoutResourceID, ArrayList<ItemNode> products) {
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

    ArrayList<ItemNode> chooseNodes(ArrayList<ItemNode> started) {
        ArrayList<ItemNode> res = new ArrayList<ItemNode>();
        for (int i = 0; i < started.size(); i++) {
            if (started.get(i).secondSide) res.add(started.get(i));
        }
        return res;
    }

    public void showItemChildNodes(ItemNode node) {
        node.opened = true;
        ArrayList<ItemNode> nodes = node.getChildNodes();
        for (ItemNode p : nodes) {
            changeItemNodeVisibility(p, true);
            getView(p.position, null, null);

        }
    }

    private int getItemOffset(ItemNode node) {
        return 40*(node.getItemNodeLevel() > 0 ? 1 : 0) + 5 + node.getItemNodeLevel()*20;
    }

    public void changeItemNodeVisibility(ItemNode node, boolean b) {
        node.isVisible = b;

    }

    public boolean hasNode(ItemNode parent, ItemNode child) {
        ArrayList<ItemNode> nodes = parent.getChildNodes();
        if (parent.equals(child)) return true;
        for (ItemNode p : nodes) {
            if (hasNode(p, child)) {
                return true;
            }
        }
        return false;
    }

    public void hideItemChildNodes(ItemNode node) {
        ArrayList<ItemNode> nodes = node.getChildNodes();
        node.opened = false;
        for (ItemNode p : nodes) {
            hideItemChildNodes(p);
            changeItemNodeVisibility(p, false);
            getView(p.position, null, null);
        }
    }
    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        ItemNode p = getProduct(position);

        View view = convertView;
        if (view == null) {

            view = lInflater.inflate(R.layout.tree_node_item, null);
        }


        ImageView imageView = (ImageView) view.findViewById(R.id.topLevelNodeImage);
        //imageView.setVisibility((p.getItemNodeLevel() == 0) ? View.VISIBLE : View.GONE);
        imageView.setVisibility((p.getItemNodeLevel() == 0) ? View.VISIBLE : View.GONE);
        imageView.setImageResource(p.image);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        TextView textView = (TextView) view.findViewById(R.id.description);
        textView.setTextSize(19 - 2*p.getItemNodeLevel());
        textView.setText(p.description);
        textView.setPadding(getItemOffset(p), 5, 5, 5);


        LinearLayout parentLayout = (LinearLayout) view.findViewById(R.id.parentLayout);
        if (p.isVisible) {
            parentLayout.setVisibility(View.VISIBLE);
        } else {
            parentLayout.setVisibility(View.GONE);
        }
        if (p.getItemNodeLevel() == 0 && (p.description.equals("Грузы")
                || p.description.equals("Декларации")
                || p.description.equals("Справочники")
                || p.description.equals("Закладки")
                || p.description.equals("Генератор отчетов"))) {
            view.setVisibility(View.GONE);
            parentLayout.setVisibility(View.GONE);
        } else if (p.getItemNodeLevel() == 0) {
            view.setVisibility(View.VISIBLE);
            parentLayout.setVisibility(View.VISIBLE);
        }
        return view;
    }



    // товар по позиции
    ItemNode getProduct(int position) {
        return ((ItemNode) getItem(position));
    }
}
