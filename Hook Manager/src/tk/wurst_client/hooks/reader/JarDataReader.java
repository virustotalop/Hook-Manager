/*
 * Copyright � 2015 | Alexander01998 | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.hooks.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import tk.wurst_client.hooks.reader.data.JarData;

public class JarDataReader
{
	private JTree tree;
	
	public JarDataReader(JTree tree)
	{
		this.tree = tree;
	}
	
	public JarData read(File file) throws IOException
	{
		DefaultMutableTreeNode root =
			new DefaultMutableTreeNode(file.getName());
		JarData jarData = new JarData();
		JarInputStream input = new JarInputStream(new FileInputStream(file));
		for(JarEntry entry; (entry = input.getNextJarEntry()) != null;)
			if(entry.isDirectory())
				root.add(new DefaultMutableTreeNode(entry.getName()
					.substring(0, entry.getName().length() - 1)
					.replace("/", "."), true));
			else if(entry.getName().endsWith(".class"))
			{
				ClassReader reader = new ClassReader(input);
				ClassDataReader wReader =
					new ClassDataReader(Opcodes.ASM4, jarData);
				reader.accept(wReader, 0);
				
				if(entry.getName().contains("/"))
					getNode(
						root,
						entry.getName()
							.substring(0, entry.getName().lastIndexOf("/"))
							.replace("/", ".")).add(
						new DefaultMutableTreeNode(entry.getName().substring(
							entry.getName().lastIndexOf("/") + 1), false));
				else
					root.add(new DefaultMutableTreeNode(entry.getName(), false));
			}else if(entry.getName().contains("/"))
				getNode(
					root,
					entry.getName()
						.substring(0, entry.getName().lastIndexOf("/"))
						.replace("/", ".")).add(
					new DefaultMutableTreeNode(entry.getName().substring(
						entry.getName().lastIndexOf("/") + 1), false));
			else
				root.add(new DefaultMutableTreeNode(entry, false));
		input.close();
		getNode(root, "META-INF").add(
			new DefaultMutableTreeNode("MANIFEST.MF", false));
		sortNode(root);
		tree.setModel(new DefaultTreeModel(root));
		return jarData;
	}
	
	private DefaultMutableTreeNode getNode(DefaultMutableTreeNode root, String s)
	{
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> nodes =
			root.depthFirstEnumeration();
		while(nodes.hasMoreElements())
		{
			DefaultMutableTreeNode node = nodes.nextElement();
			if(node.toString().equalsIgnoreCase(s))
				return node;
		}
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(s, true);
		root.add(node);
		return node;
	}
	
	private void sortNode(DefaultMutableTreeNode node)
	{
		List<DefaultMutableTreeNode> packages =
			new ArrayList<DefaultMutableTreeNode>();
		List<DefaultMutableTreeNode> classes =
			new ArrayList<DefaultMutableTreeNode>();
		List<DefaultMutableTreeNode> files =
			new ArrayList<DefaultMutableTreeNode>();
		for(Enumeration subnodes = node.children(); subnodes.hasMoreElements();)
		{
			DefaultMutableTreeNode subnode =
				(DefaultMutableTreeNode)subnodes.nextElement();
			if(subnode.getAllowsChildren())
			{
				if(subnode.getChildCount() > 0)
				{
					sortNode(subnode);
					packages.add(subnode);
				}
			}else if(subnode.toString().endsWith(".class"))
				classes.add(new DefaultMutableTreeNode(subnode.toString()
					.substring(0, subnode.toString().length() - 6), false));
			else
				files.add(subnode);
		}
		
		node.removeAllChildren();
		
		Comparator<DefaultMutableTreeNode> nodeComparator = new Comparator<DefaultMutableTreeNode>()
		{
			@Override
			public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2)
			{
					return o1.toString().compareTo(o2.toString());
			}
		};
		
		Collections.sort(packages, nodeComparator);
		Collections.sort(classes, nodeComparator);
		Collections.sort(files, nodeComparator);
		
		for(DefaultMutableTreeNode packageNode : packages)
			node.add(packageNode);
		
		
			//classes
			//.sort((DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) -> o1
			//	.toString().compareToIgnoreCase(o2.toString()));
		for(DefaultMutableTreeNode classNode : classes)
        node.add(classNode);
		
		
		//files.sort((DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) -> o1
	//		.toString().compareToIgnoreCase(o2.toString()));
		for(DefaultMutableTreeNode fileNode : files)
			node.add(fileNode);
	}
}
