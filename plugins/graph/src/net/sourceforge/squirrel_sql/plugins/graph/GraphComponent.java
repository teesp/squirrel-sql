package net.sourceforge.squirrel_sql.plugins.graph;

import java.awt.*;

public interface GraphComponent
{
   void paint(Graphics g);

   Dimension getRequiredSize();
}
