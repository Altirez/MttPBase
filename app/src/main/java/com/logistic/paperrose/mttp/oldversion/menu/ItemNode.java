package com.logistic.paperrose.mttp.oldversion.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paperrose on 24.12.2014.
 */
public class ItemNode {
    public String description;
    public String bookmarkDescription;
    public String key;
    public ItemNode parent = null;
    public ArrayList<ItemNode> childNodes = new ArrayList<ItemNode>();
    public int itemNodeLevel = 0;
    public int image;
    public int background;
    public int bookmarkID = -1;
    public boolean isBookmark = false;
    public boolean isCustomBookmark = false;
    public boolean opened = false;
    public boolean isVisible = false;
    public int position;
    public boolean isHistoryItem = false;
    public String count = "";
    public boolean onlySide = false;
    public boolean secondSide = false;


    public ItemNode(int _position, String _description, ItemNode _parent, int _image, List<ItemNode> fullList, String _key, int _background) {
        parent = _parent;
        key = _key;
        image = _image;
        itemNodeLevel = 0;
        isVisible = (parent == null);
        background = _background;
        description = _description;
        position = fullList.size();
        if (parent != null) {
            parent.addNode(this);
            itemNodeLevel = parent.getItemNodeLevel() + 1;
        }
        fullList.add(this);
    }

    public ItemNode(boolean onlySide, int _position, String _description, ItemNode _parent, int _image, List<ItemNode> fullList, String _key, int _background) {
        parent = _parent;
        key = _key;
        image = _image;
        itemNodeLevel = 0;
        background = _background;
        isVisible = (parent == null);
        description = _description;
        position = fullList.size();
        this.onlySide = true;
        if (parent != null) {
            parent.addNode(this);
            itemNodeLevel = parent.getItemNodeLevel() + 1;
        }
        fullList.add(this);
    }
    public ItemNode(boolean onlySide, boolean secondSide, int _position, String _description, ItemNode _parent, int _image, List<ItemNode> fullList, String _key, int _background) {
        parent = _parent;
        key = _key;
        image = _image;
        itemNodeLevel = 0;
        background = _background;
        isVisible = (parent == null);
        description = _description;
        position = fullList.size();
        this.onlySide = onlySide;
        this.secondSide = true;
        if (parent != null) {
            parent.addNode(this);
            itemNodeLevel = parent.getItemNodeLevel() + 1;
        }
        fullList.add(this);
    }

    public ItemNode(int _position, String _description, ItemNode _parent, int _image, List<ItemNode> fullList, String _key, String bookmarkDesc, int bookmarkID, int _background) {
        parent = _parent;
        key = _key;
        image = _image;
        isBookmark = true;
        this.bookmarkID = bookmarkID;
        bookmarkDescription = bookmarkDesc;
        itemNodeLevel = 0;
        background = _background;
        isVisible = (parent == null);
        description = _description;
        position = fullList.size();
        if (parent != null) {
            parent.addNode(this);
            itemNodeLevel = parent.getItemNodeLevel() + 1;
        }
        fullList.add(this);
    }

    public ItemNode(int _position, String _description, ItemNode _parent, List<ItemNode> fullList, String _key, String bookmarkDesc) {
        parent = _parent;
        key = _key;
        isHistoryItem = true;
        bookmarkDescription = bookmarkDesc;
        itemNodeLevel = 0;
        isVisible = (parent == null);
        description = _description;
        position = _position-1;
        if (parent != null) {
            parent.addNode(this);
            itemNodeLevel = parent.getItemNodeLevel() + 1;
        }
        fullList.add(this);
    }

    public int getItemNodeLevel() {
        return itemNodeLevel;
    }

    public ArrayList<ItemNode> getChildNodes() {
        return childNodes;
    }

    public void addNode(ItemNode node) {
        childNodes.add(node);
    }

    public ItemNode getParent() {
        return parent;
    }



}
