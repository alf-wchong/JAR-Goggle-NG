package chongwm.utility.java.jargoggle.ui;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;
import java.nio.file.Path;

public final class ResultTreeRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
        if (userObject instanceof Path path) {
            setText(path.toString());
        }
        return this;
    }
}
