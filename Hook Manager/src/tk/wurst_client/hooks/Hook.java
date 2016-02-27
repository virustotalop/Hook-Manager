package tk.wurst_client.hooks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class Hook {
	
	private ArrayList<Method> methods = new ArrayList<Method>();
	
	public void hook(Object[] params)
	{
		for(Method meth : methods)
		{
			try 
			{
				meth.invoke(null, params);
			} 
			catch (IllegalAccessException | IllegalArgumentException| InvocationTargetException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public void addMethod(Method method)
	{
		methods.add(method);
	}
}