package org.squirrelsql;

import javafx.application.Platform;
import org.squirrelsql.services.MessageHandler;
import org.squirrelsql.services.MessageHandlerDestination;

import java.io.PrintStream;

public class ExceptionHandler
{

   public static void initHandling()
   {
      if(false == Platform.isFxApplicationThread())
      {
         throw new IllegalStateException("MUst bot");
      }

      Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
      {
         @Override
         public void uncaughtException(Thread t, Throwable e)
         {
            handle(e);
         }
      });
   }

   private static void handle(final Throwable t)
   {
      t.printStackTrace(System.err);
      MessageHandler mh = new MessageHandler(ExceptionHandler.class, MessageHandlerDestination.MESSAGE_LOG);
      mh.error(t);
   }


}
