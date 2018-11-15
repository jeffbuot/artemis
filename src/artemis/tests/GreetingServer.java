/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.tests;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class GreetingServer extends Thread
{
       private ServerSocket serverSocket;
       Socket server;

       public GreetingServer(int port) throws IOException, SQLException, ClassNotFoundException, Exception
       {
          serverSocket = new ServerSocket(port);
          serverSocket.setSoTimeout(180000);
       }

       public void run()
       {
           while(true)
          { 
               try
               {
                  server = serverSocket.accept();
                  BufferedImage img=ImageIO.read(ImageIO.createImageInputStream(server.getInputStream()));
                  JFrame frame = new JFrame();
                  frame.getContentPane().add(new JLabel(new ImageIcon(img)));
                  frame.setSize(500,700);
                  frame.pack();
                  frame.setLocationRelativeTo(null);
                  frame.setVisible(true);                  
              }
             catch(SocketTimeoutException st)
             {
                   System.out.println("Socket timed out!");
                  break;
             }
             catch(IOException e)
             {
                  e.printStackTrace();
                  break;
             }
             catch(Exception ex)
            {
                  System.out.println(ex);
            }
          }
       }

       public static void main(String [] args) throws IOException, SQLException, ClassNotFoundException, Exception
       {
              Thread t = new GreetingServer(6066);
              t.start();
       }
}