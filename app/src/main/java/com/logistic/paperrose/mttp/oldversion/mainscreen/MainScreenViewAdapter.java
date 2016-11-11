package com.logistic.paperrose.mttp.oldversion.mainscreen;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.logistic.paperrose.mttp.oldversion.MainActivity;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.menu.ItemNode;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by paperrose on 05.03.2015.
 */
public class MainScreenViewAdapter extends BaseAdapter {
    MainActivity activity;
    ArrayList<ItemNode> objects;

    public MainScreenViewAdapter(MainActivity c, ArrayList<ItemNode> objects) {
        this.activity = c;
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public ItemNode getItem(int i) {
        return objects.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    class ViewHolderItem {
        TextView textViewItem;
        TextView cntViewItem;
        ImageView imageViewItem;
        ImageView imageBackItem;
        LinearLayout all;
    }

    ArrayList<ItemNode> chooseNodes(ArrayList<ItemNode> started) {
        ArrayList<ItemNode> res = new ArrayList<ItemNode>();
        for (int i = 0; i < started.size(); i++) {
            if (!started.get(i).onlySide) res.add(started.get(i));
        }
        return res;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolderItem viewHolder;
        Picasso mPicasso = Picasso.with(activity.getApplicationContext());
        //mPicasso.setIndicatorsEnabled(true);

        Typeface typeface = Typeface.createFromAsset(activity.getAssets(), "Roboto-Light.ttf");
        Typeface typeface2 = Typeface.createFromAsset(activity.getAssets(), "Roboto-Regular_0.ttf");
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = inflater.inflate(R.layout.grid_single, null);
            LinearLayout ll1 = (LinearLayout)view.findViewById(R.id.s1);
            LinearLayout ll2 = (LinearLayout)view.findViewById(R.id.s2);

            ll1.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    (activity.getWindowManager().getDefaultDisplay().getHeight()-ApplicationParameters.dpToPx(130))/6));
            ll2.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    (activity.getWindowManager().getDefaultDisplay().getHeight()-ApplicationParameters.dpToPx(130))/6));

            viewHolder = new ViewHolderItem();
            viewHolder.textViewItem = (TextView)view.findViewById(R.id.grid_text);
            viewHolder.cntViewItem = (TextView)view.findViewById(R.id.grid_cnt);
            viewHolder.all = (LinearLayout)view.findViewById(R.id.grid);
            viewHolder.imageViewItem = (ImageView)view.findViewById(R.id.grid_image);
            viewHolder.imageBackItem = (ImageView)view.findViewById(R.id.back_pic);

            //mPicasso.load(R.id.grid_image).into(viewHolder.imageViewItem);
            //mPicasso.load(R.id.back_pic).into(viewHolder.imageBackItem);

            view.setTag(viewHolder);
            view.setTag(R.id.grid_text, viewHolder.textViewItem);
            //view.setTag(R.id.grid_image, viewHolder.imageViewItem);
            //view.setTag(R.id.back_pic, viewHolder.imageBackItem);
            view.setTag(R.id.grid, viewHolder.all);
            view.setTag(R.id.grid_cnt, viewHolder.cntViewItem);
            viewHolder.all.setClickable(true);

            viewHolder.all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view2) {
                    int num = (Integer)view2.getTag();
                    activity.currentNode = getItem(num);
                    if (activity.currentNode.childNodes.size() > 0 &&
                            !activity.currentNode.key.equals("bookmark_entity") &&
                            !activity.currentNode.key.equals("cont_status") &&
                            !activity.currentNode.key.equals("gtd_status")) {

                        activity.setAdapter(new MainScreenViewAdapter(activity, chooseNodes(activity.currentNode.childNodes)));
                        activity.setupActionBarWithText(activity.currentNode.description);
                        ApplicationParameters.lastItem = activity.currentNode;
                    } else {
                        activity.nodeAction(activity.currentNode);
                    }
                }
            });
            viewHolder.all.setLongClickable(true);
            viewHolder.all.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view2) {
                    int num = (Integer)view2.getTag();
                    activity.currentNode = getItem(num);
                    if (activity.currentNode.key.equals("bookmark_entity")) {
                        activity.setAdapter(new MainScreenViewAdapter(activity, chooseNodes(activity.currentNode.childNodes)));
                    }
                    if (activity.currentNode.key.equals("cont_status")) {
                        activity.setAdapter(new MainScreenViewAdapter(activity, chooseNodes(activity.currentNode.childNodes)));
                    }
                    if (activity.currentNode.key.equals("gtd_status")) {
                        activity.setAdapter(new MainScreenViewAdapter(activity, chooseNodes(activity.currentNode.childNodes)));
                    }
                    return true;
                }
            });

        } else {
            viewHolder = (ViewHolderItem) view.getTag();
        }

        ItemNode objectItem = getItem(i);
        viewHolder.all.setTag(i);
        viewHolder.textViewItem.setTag(i);
        viewHolder.textViewItem.setMaxLines(5);
        viewHolder.imageViewItem.setTag(i);
        viewHolder.imageBackItem.setTag(i);

        if(objectItem != null) {
            viewHolder.textViewItem.setText(objectItem.description);
            if (objectItem.itemNodeLevel == 0) {
                viewHolder.textViewItem.setTypeface(typeface2);
            } else {
                viewHolder.textViewItem.setTypeface(typeface);
            }
            if (!objectItem.count.isEmpty()) {
                viewHolder.cntViewItem.setText(objectItem.count);
                viewHolder.cntViewItem.setVisibility(View.VISIBLE);
            }

            try {
                /*Bitmap bmp = ApplicationParameters.cachedImages.get(Integer.toString(objectItem.image));
                if (bmp == null) {
                    if (objectItem.image != R.drawable.circle) {
                        viewHolder.imageViewItem.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        SVG svg = new SVGBuilder().readFromResource(activity.getResources(), objectItem.image).build();
                        PictureDrawable pd = new PictureDrawable(svg.getPicture());
                        bmp = Bitmap.createBitmap(pd.getIntrinsicWidth(), pd.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bmp);
                        canvas.drawPicture(pd.getPicture());
                    } else {
                        bmp = BitmapFactory.decodeResource(activity.getResources(), R.drawable.circle);
                    }
                    ApplicationParameters.cachedImages.put(Integer.toString(objectItem.image), bmp);
                }
                viewHolder.imageViewItem.setImageBitmap(bmp);*/
                mPicasso
                        .load(objectItem.image)
                        .fit()
                        .into(viewHolder.imageViewItem);
                if (objectItem.background != -1) {
                    mPicasso
                            .load(objectItem.background)
                            .into(viewHolder.imageBackItem);
                } else {
                    viewHolder.all.setBackgroundDrawable(null);
                }
            } catch (Exception e) {

            }
        }
        view.setClickable(true);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                int num = i;
                activity.currentNode = getItem(num);
                if (activity.currentNode.childNodes.size() > 0 && !activity.currentNode.key.equals("bookmark_entity") && !activity.currentNode.key.equals("cont_status")) {
                    activity.setAdapter(new MainScreenViewAdapter(activity, chooseNodes(activity.currentNode.childNodes)));
                    activity.setupActionBarWithText(activity.currentNode.description);
                    ApplicationParameters.lastItem = activity.currentNode;

                } else {
                    activity.nodeAction(activity.currentNode);
                }
            }
        });
        view.setLongClickable(true);
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view2) {
                int num = i;
                activity.currentNode = getItem(num);
                if (activity.currentNode.key.equals("bookmark_entity")) {
                    activity.setAdapter(new MainScreenViewAdapter(activity, chooseNodes(activity.currentNode.childNodes)));
                }
                if (activity.currentNode.key.equals("cont_status")) {
                    activity.setAdapter(new MainScreenViewAdapter(activity, chooseNodes(activity.currentNode.childNodes)));
                }
                return true;
            }
        });

        return view;
    }
}
