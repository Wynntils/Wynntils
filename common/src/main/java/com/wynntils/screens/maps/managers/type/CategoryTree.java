/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.type;

import com.wynntils.utils.StringUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CategoryTree {
    private final CategoryTreeNode root;

    public CategoryTree(List<String> categoryIds) {
        root = new CategoryTreeNode(null, "root");
        for (String categoryId : categoryIds) {
            String[] parts = categoryId.split(":");
            CategoryTreeNode currentNode = root;
            StringBuilder fullIdBuilder = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                if (i > 0) fullIdBuilder.append(':');
                fullIdBuilder.append(part);
                String fullId = fullIdBuilder.toString();

                CategoryTreeNode child = currentNode.getChildByName(part);
                if (child == null) {
                    // Last part is the actual category; earlier parts are just folders
                    child = new CategoryTreeNode(fullId, part, new ArrayList<>());
                    currentNode.addChild(child);
                }
                currentNode = child;
            }
        }
        sortTree(root);
    }

    private void sortTree(CategoryTreeNode node) {
        node.sortChildren(Comparator.comparing(CategoryTreeNode::getName));
        for (CategoryTreeNode child : node.getChildren()) {
            sortTree(child);
        }
    }

    public CategoryTreeNode getRoot() {
        return root;
    }

    public CategoryTreeNode getFilteredTree(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return root;
        }
        String normalized = searchText.trim();
        List<CategoryTreeNode> filteredChildren = filterChildren(root, normalized);
        return new CategoryTreeNode(null, "root", filteredChildren);
    }

    private List<CategoryTreeNode> filterChildren(CategoryTreeNode node, String searchText) {
        List<CategoryTreeNode> result = new ArrayList<>();
        for (CategoryTreeNode child : node.getChildren()) {
            List<CategoryTreeNode> filteredGrandChildren = filterChildren(child, searchText);
            boolean childMatches = StringUtils.partialMatch(child.getName(), searchText);
            boolean hasMatchingDescendant = !filteredGrandChildren.isEmpty();

            if (childMatches || hasMatchingDescendant) {
                CategoryTreeNode newNode = new CategoryTreeNode(
                        child.getFullId(),
                        child.getName(),
                        filteredGrandChildren);
                result.add(newNode);
            }
        }
        return result;
    }
}
