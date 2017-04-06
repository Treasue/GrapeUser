package grape.app.GrapeUser;

import httpServer.booter;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	booter booter = new booter();
  		System.out.println("GrapeUser!");
  		try {
  			System.setProperty("AppName", "GrapeUser");
  			booter.start(6008);
  		} catch (Exception e) {

  		}

//        System.out.println( "Hello World!" );
    }
}
