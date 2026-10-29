package net.sf.fmj.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Interface to read/write registry contents from/to somewhere.
 * @author Ken Larson
 * @author Warren Bloomer
 *
 */
interface RegistryIO
{

	public void load(InputStream is) throws IOException;
	public void write(OutputStream os) throws IOException;

}
