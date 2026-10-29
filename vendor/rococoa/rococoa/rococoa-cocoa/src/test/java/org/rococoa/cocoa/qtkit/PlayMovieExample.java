/*
 * Copyright 2007, 2008 Duncan McGregor
 * 
 * This file is part of Rococoa, a library to allow Java to talk to Cocoa.
 * 
 * Rococoa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Rococoa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Rococoa.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.rococoa.cocoa.qtkit;

import javax.swing.*;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

public class PlayMovieExample {

    static final File FILE = new File("/Users/gkellum/Movies/big_buck_bunny_720p_h264.mov");

    static {
    	// load library
        @SuppressWarnings("unused")
        QTKit instance = QTKit.instance;
    }

    public static void main(String[] args) {
        QTMovieView movieView = QTMovieView.CLASS.create();
        movieView.setControllerVisible(true);
        movieView.setPreservesAspectRatio(true);

        MovieComponent component = new MovieComponent(movieView);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(component);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width, screenSize.height);
        frame.setPreferredSize(new Dimension(screenSize.width, screenSize.height));
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = environment.getDefaultScreenDevice();
        //frame.setUndecorated(true);
        device.setFullScreenWindow(frame);
        
        KeyAdapter keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    GraphicsDevice device = environment.getDefaultScreenDevice();
                    device.setFullScreenWindow(null);
                    System.exit(1);
                }
            }
        };
        frame.addKeyListener(keyListener);
        component.addKeyListener(keyListener);
        
        QTMovie movie = QTMovie.movieWithFile_error(FILE.getPath(), null);
        movieView.setMovie(movie);                
        movie.gotoBeginning();
        frame.pack();
        frame.setVisible(true);

        KeyboardFocusManager kbfm  =   KeyboardFocusManager.getCurrentKeyboardFocusManager();
        kbfm.addKeyEventDispatcher(new FullScreenKeyEventDispatcher(movie));
    }

    public static class FullScreenKeyEventDispatcher implements KeyEventDispatcher {
        private QTMovie movie;
        
        public FullScreenKeyEventDispatcher(QTMovie movie) {
            this.movie = movie;
        }
        
        public boolean dispatchKeyEvent(KeyEvent keyEvent) {
            if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE && keyEvent.getID() == KeyEvent.KEY_PRESSED) {
                //GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
                //GraphicsDevice device = environment.getDefaultScreenDevice();
                //device.setFullScreenWindow(null);
                System.exit(1);
                return true;
            } else if (keyEvent.getKeyCode() == KeyEvent.VK_SPACE && keyEvent.getID() == KeyEvent.KEY_PRESSED) {
                movie.play();
            }

            return false;
        }
    }
}
