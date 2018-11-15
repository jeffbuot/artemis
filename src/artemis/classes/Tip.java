/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.classes;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.extended.window.WebPopOver;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;

/**
 *
 * @author Jeff
 */
public class Tip {
    public final static String DEF_TIP = "<html><b>Double Click</b> a row to update the data.<br>"
            + "<b>CTRL+N</b> for new form.</html>";
    public static void showTip(Component c,ActionEvent evt,String msg){
            final WebPopOver popOver = new WebPopOver(c);
            popOver.setCloseOnFocusLoss(true);
            popOver.setMargin(10);
            popOver.setLayout(new VerticalFlowLayout());
            popOver.add(new JLabel(msg));
            popOver.show((Component) evt.getSource());
    }
}
