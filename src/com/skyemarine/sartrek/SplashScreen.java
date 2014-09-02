package com.skyemarine.sartrek;

import java.util.*;

import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
//#ifdef OS4.6
import net.rim.device.api.ui.decor.*;

/**
 * This class implements a generic splash screen for the Blackberry. It appears for a specified interval,
 * during which time it can be escaped using either the menu or the escape key. When it terminates it
 * starts display of the application's main screen.
 */
public class SplashScreen extends MainScreen {
    private UiApplication _application;
    private MainScreen _nextScreen;
    private static Timer _timer = new Timer();
         
    public SplashScreen(UiApplication ui, MainScreen nextScreen, Bitmap bitmap, int timeout) {
        super(Field.USE_ALL_WIDTH | Field.USE_ALL_HEIGHT);
        _application = ui;
        _nextScreen = nextScreen;

        // Center the logo bitmap horizontally and vertically
        BitmapField bitmapField = new BitmapField(bitmap, Field.FIELD_HCENTER | Field.FIELD_VCENTER)
        {
            public void paint(Graphics graphics) {
                // Sets the BackgroundColor
                graphics.setBackgroundColor(CustomMainScreen.BackgroundColor);
                //int[] xInds = new int[] { 0, getExtent().width, getExtent().width, 0 };
                //int[] yInds = new int[] { 0, 0, getExtent().height, getExtent().height };
                //final int[] cols = new int[]{ Color.AZURE, Color.AZURE, Color.CADETBLUE, Color.CADETBLUE };
                //graphics.drawShadedFilledPath(xInds, yInds, null, cols, null);
                
                // Clears the entire graphic area to the current background
                graphics.clear();
                super.paint(graphics);
            }
        };
        bitmapField.setBackground(BackgroundFactory.createLinearGradientBackground(Color.AZURE, Color.AZURE, Color.CADETBLUE, Color.CADETBLUE));        
        bitmapField.setSpace(
            (Display.getWidth() - bitmap.getWidth()) / 2,
            (Display.getHeight() - bitmap.getHeight()) / 2);
        this.add(bitmapField);
        
        // Listen for escape events
        SplashScreenListener listener = new SplashScreenListener(this);
        this.addKeyListener(listener);
        
        // Start a timer
        _timer.schedule(new CountDown(), timeout * 1000);
        
        // Display the splash screen
        _application.pushScreen(this);
    }
    
    public void dismiss() {
        _timer.cancel();
        _application.popScreen(this);
        _application.pushScreen(_nextScreen);
    }
    
    private class CountDown extends TimerTask {
        public void run() {
            DismissThread dThread = new DismissThread();
            _application.invokeLater(dThread);
        }
    }
    
    private class DismissThread implements Runnable {
        public void run() {
            dismiss();
        }
    }
    
    protected boolean navigationClick(int status, int time) {
        dismiss();
        return true;
    }
    
    protected boolean navigationUnclick(int status, int time) {
        return false;
    }
    
    protected boolean navigationMovement(int dx, int dy, int status, int time) {
        return false;
    }
    
    protected boolean touchEvent(TouchEvent message) {
    	if (message.getEvent() == TouchEvent.CLICK) {
	        dismiss();
	        return true;
    	}
    	return false;
	}
    
    public static class SplashScreenListener implements KeyListener {
        private SplashScreen screen;
    
        public boolean keyChar(char key, int status, int time) {
            //intercept the ESC and MENU key - exit the splash screen
            boolean retval = false;
            switch (key) {
                case Characters.CONTROL_MENU:
                case Characters.ESCAPE:
                screen.dismiss();
                retval = true;
                break;
            }
            return retval;
        }
    
        public boolean keyDown(int keycode, int time) {
            return false;
        }
    
        public boolean keyRepeat(int keycode, int time) {
            return false;
        }
    
        public boolean keyStatus(int keycode, int time) {
            return false;
        }
    
        public boolean keyUp(int keycode, int time) {
            return false;
        }
    
        public SplashScreenListener(SplashScreen splash) {
            screen = splash;
        }
    }
} 

