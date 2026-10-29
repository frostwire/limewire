/*
 * $Header: /cvs/httpclient-118/src/examples/ClientApp.java,v 1.1.1.1 2003/10/30 20:32:58 sberlin Exp $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/10/30 20:32:58 $
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "HttpClient", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * A simple Swing application that demonstrates how to use the Jakarta
 * HttpClient API.  This application loads HTML from servers and displays the
 * content as text and as rendered HTML.
 *
 * @author Sean C. Sullivan
 * @author Ortwin Glück
 * @author Michael Becke
 */
public class ClientApp {

    public static void main(String[] args) {
        HttpClientFrame f = new HttpClientFrame();
        f.setTitle("HttpClient demo application");
        f.setSize(700, 500);
        f.addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            }
        );
        f.setVisible(true);
    }

    public static class HttpClientFrame extends JFrame {

        private JComboBox cmbURL;     
        private JTextArea taTextResponse;
        private JEditorPane htmlPane;
        
        private HttpClient client;
        
        public HttpClientFrame() {            
            client = new HttpClient(new MultiThreadedHttpConnectionManager());
            client.setConnectionTimeout(30000);

            JPanel panInput = new JPanel(new FlowLayout());

            String[] aURLs = {
                "http://www.apache.org/",
                "http://www.google.com/",
                "http://www.opensource.org/",
                "http://www.anybrowser.org/",
                "http://jakarta.apache.org/",
                "http://www.w3.org/"
            };

            final JButton btnGET = new JButton("GET");
            btnGET.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        String url = (String) cmbURL.getSelectedItem();
                        if (url != null && url.length() > 0) {
                            loadPage(url);
                        }
                    }
                }
            );
            
            cmbURL = new JComboBox(aURLs);
            cmbURL.setToolTipText("Enter a URL");
            cmbURL.setEditable(true);
            cmbURL.setSelectedIndex(0);

            JLabel lblURL = new JLabel("URL:");

            panInput.add(lblURL);
            panInput.add(cmbURL);
            panInput.add(btnGET);

            taTextResponse = new JTextArea();
            taTextResponse.setEditable(false);
            taTextResponse.setCaretPosition(0);

            htmlPane = new JEditorPane();
            htmlPane.setContentType("text/html");
            htmlPane.setEditable(false);

            JSplitPane splitResponsePane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(taTextResponse),
                new JScrollPane(htmlPane)
            );
            splitResponsePane.setOneTouchExpandable(false);
            splitResponsePane.setDividerLocation(350);
            // it would be better to set resizeWeight, but this method does
            // not exist in JRE 1.2.2
//            splitResponsePane.setResizeWeight(0.5);

            this.getContentPane().setLayout(new BorderLayout());
            this.getContentPane().add(panInput, BorderLayout.NORTH);
            this.getContentPane().add(splitResponsePane, BorderLayout.CENTER);
        }

        /**
         * Sets the HTML content to be displayed.
         * 
         * @param content an HTML document
         */
        private void setDocumentContent(String content) {
        
            HTMLDocument doc = new HTMLDocument();
            try {
                doc.remove(0, doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
        
            try {
                htmlPane.read(new ByteArrayInputStream(content.getBytes()), doc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        
            htmlPane.setDocument(doc);
            htmlPane.setCaretPosition(0);

            taTextResponse.setText(content);
            taTextResponse.setCaretPosition(0);
            taTextResponse.requestFocus();
        }
        
        /**
         * Loads the page at the given URL from a separate thread.
         * @param url
         */
        private void loadPage(final String url) {
            // create a new thread to load the URL from
            new Thread() {
                public void run() {
                    GetMethod get = new GetMethod(url);
                    get.setFollowRedirects(true);
                    
                    try {
                        int iGetResultCode = client.executeMethod(get);
                        final String strGetResponseBody = get.getResponseBodyAsString();

                        if (strGetResponseBody != null) {
                            // set the HTML on the UI thread
                            SwingUtilities.invokeLater(
                                new Runnable() {
                                    public void run() {
                                        setDocumentContent(strGetResponseBody);
                                    }
                                }
                            );
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        get.releaseConnection();
                    }
                }
            }.start();
        }

    }
        
}
