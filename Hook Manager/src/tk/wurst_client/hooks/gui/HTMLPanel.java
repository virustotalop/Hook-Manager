/*
 * Copyright © 2015 | Alexander01998 | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.hooks.gui;

import java.awt.GridLayout;
import java.io.File;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.JPanel;

import netscape.javascript.JSObject;
import tk.wurst_client.hooks.util.Constants;

public class HTMLPanel extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 230829324980154219L;
	private JFXPanel jfxPanel = new JFXPanel();
	private WebEngine engine;
	private Object bridge;
	private String htmlFile;
	
	/**
	 * Create the panel.
	 */
	public HTMLPanel()
	{
		setLayout(new GridLayout());
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				WebView view = new WebView();
				view.setContextMenuEnabled(false);
				engine = view.getEngine();
				jfxPanel.setScene(new Scene(view));
			}
		});
		add(jfxPanel);
	}
	
	public String getHTMLFile()
	{
		return htmlFile;
	}
	
	public void setHTMLFile(final String filename)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				String url;
				url = getClass().getClassLoader()
				.getResource(Constants.Resources.HTML_DIR + filename)
				.toExternalForm()
				.replace("rsrc:", new File(".").toURI().toString());
				engine.load(url);
			}
		});
		htmlFile = filename;
	}
	
	public void setHTML(final String html)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				engine.loadContent(html);
			}
		});
	}
	
	public Object getBridge()
	{
		return bridge;
	}
	
	public void setBridge(final Object bridge)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				((JSObject)engine.executeScript("window")).setMember("java",bridge);
			}
		});
		this.bridge = bridge;
	}
	
	public Object executeScript(String script)
	{
		return engine.executeScript(script);
	}
	
	public void executeScriptAsync(final String script)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				executeScript(script);
			}
		});
	}
	
	public void doWhenFinished(final Runnable task)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				engine.getLoadWorker().stateProperty()
					.addListener(new ChangeListener<State>()
					{
						@Override
						public void changed(ObservableValue ov, State oldState,
							State newState)
						{
							if(newState == State.SUCCEEDED)
							{
								task.run();
								engine.getLoadWorker().stateProperty()
									.removeListener(this);
							}
						}
					});
			}
		});
	}
}
