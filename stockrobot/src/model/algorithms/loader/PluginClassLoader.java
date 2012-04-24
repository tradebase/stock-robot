package model.algorithms.loader;



import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import utils.SettingParser;



public class PluginClassLoader extends ClassLoader {
	private static final String EXPORTEDPACKAGE = "model.algorithms";

	@Override
	public Class<?> findClass (String name) {
		byte[] data = loadClassData (name);
		
		return defineClass(EXPORTEDPACKAGE + "." + name, data, 0, data.length);
	}

	private byte[] loadClassData (String name) {
		File f = new File(SettingParser.getAlgorithmPath() + name + ".class");
		ByteArrayOutputStream classBuffer = new ByteArrayOutputStream();

		try {
			FileInputStream stream = new FileInputStream(f);
			while(stream.available() != 0){
				classBuffer.write(stream.read());
			}
			classBuffer.flush();
			stream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return classBuffer.toByteArray();
	}
}