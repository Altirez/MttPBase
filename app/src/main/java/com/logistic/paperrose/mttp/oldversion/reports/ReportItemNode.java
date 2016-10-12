package com.logistic.paperrose.mttp.oldversion.reports;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paperrose on 24.12.2014.
 */
public class ReportItemNode {
    public String description;
    public String key;
    public ReportItemNode parent = null;
    public ArrayList<ReportItemNode> childNodes = new ArrayList<ReportItemNode>();
    public int itemNodeLevel = 0;
    public int image;
    public int image2;
    public boolean opened = false;
    public boolean isVisible = false;
    public int position;
    public String count = "";


    public ReportItemNode(String _description, String count, ReportItemNode _parent, List<ReportItemNode> fullList, String _key) {
        parent = _parent;
        key = _key;
        itemNodeLevel = 0;
        this.count = count;
        isVisible = (parent == null);
        description = _description;
        position = fullList.size();
        if (parent != null) {
            parent.addNode(this);
            itemNodeLevel = parent.getItemNodeLevel() + 1;
        }
        fullList.add(this);
    }



    public int getItemNodeLevel() {
        return itemNodeLevel;
    }

    public ArrayList<ReportItemNode> getChildNodes() {
        return childNodes;
    }

    public void addNode(ReportItemNode node) {
        childNodes.add(node);
    }

    public ReportItemNode getParent() {
        return parent;
    }

    public void setImageID(int im1, int im2) {
        image = im1;
        image2 = im2;
    }

}
