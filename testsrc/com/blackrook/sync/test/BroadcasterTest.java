/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.sync.test;

import com.blackrook.commons.Common;
import com.blackrook.sync.message.MessageBroadcaster;
import com.blackrook.sync.message.MessageListener;

public class BroadcasterTest
{
	public static MessageBroadcaster BROADCASTER;
	
	public static void main(String[] args)
	{
		BROADCASTER = new MessageBroadcaster();
		Listener a1 = new Listener("ListenerOne");
		Listener a2 = new Listener("ListenerTwo", "A");
		Listener a3 = new Listener("ListenerThree", "B", "A");
		BROADCASTER.registerListener(a1);
		BROADCASTER.registerListener(a2);
		BROADCASTER.registerListener(a3);

		BROADCASTER.broadcast("asdfasdf");
		Common.sleep(500);
		BROADCASTER.broadcast("A");
		Common.sleep(500);
		BROADCASTER.broadcast("eqweqwer");
		Common.sleep(500);
		BROADCASTER.broadcast("B");
		Common.sleep(500);
	}
	
	
	private static class Listener implements MessageListener
	{
		private String name;
		private String[] types;
		
		public Listener(String name, String ... types)
		{
			this.name = name;
			this.types = types;
		}

		@Override
		public void onMessageReceive(String messageType, Object... args)
		{
			System.out.println(name+": Received "+messageType);
		}

		@Override
		public String[] getMessageTypes()
		{
			return types;
		}
	}
	
}
