package org.ifcopenshell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;

public class IfcGeomServerClient implements AutoCloseable, Iterator<IfcGeomServerClientEntity>, Iterable<IfcGeomServerClientEntity> {
	private static final Logger LOGGER = LoggerFactory.getLogger(IfcGeomServerClient.class);
	
	private Process process = null;
	private LittleEndianDataInputStream dis = null;
	private LittleEndianDataOutputStream dos = null;
	private boolean hasMore = false;
	
	@Override
	public void close() throws Exception {
		terminate();
	}
	
	@Override
	public boolean hasNext() {
		return hasMore;
	}

	@Override
	public IfcGeomServerClientEntity next() {
		return getNext();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<IfcGeomServerClientEntity> iterator() {
		return this;
	}
	
	public IfcGeomServerClient(String executableFilename, InputStream ifcInputStream) {
		try {
			process = Runtime.getRuntime().exec(executableFilename);
			dos = new LittleEndianDataOutputStream(process.getOutputStream());
			dis = new LittleEndianDataInputStream(process.getInputStream());
			
			if (dis.readInt() != HELLO) {
				terminate();
				LOGGER.error("Invalid welcome message received");
				return;
			}
			Hello h = new Hello(); h.read(dis);
			
			String reportedVersion = h.getString();
			if (!VERSION.equals(reportedVersion)) {
				terminate();
				LOGGER.error(String.format("Version mismatch: Plugin version %s does not match IfcOpenShell version %s", VERSION, reportedVersion));
				return;
			}
			
			IfcModel m = new IfcModel(ifcInputStream);
			m.write(dos);
			
			askForMore();
		} catch (IOException e) {}
	}

	private static final int HELLO     = 0xff00;
	private static final int IFC_MODEL = HELLO     + 1;
	private static final int GET       = IFC_MODEL + 1;
	private static final int ENTITY    = GET       + 1;
	private static final int MORE      = ENTITY    + 1;
	private static final int NEXT      = MORE      + 1;
	private static final int BYE       = NEXT      + 1;
	private static final int GET_LOG   = BYE       + 1;
	private static final int LOG       = GET_LOG   + 1;
	
	private static String VERSION = "IfcOpenShell-0.5.0-dev";
	
	abstract static class Command {
		abstract void read_contents(LittleEndianDataInputStream s) throws IOException;
		abstract void write_contents(LittleEndianDataOutputStream s) throws IOException;
		
		int iden;
		int len;
		
		void read(LittleEndianDataInputStream s) throws IOException {
			len = s.readInt();
			read_contents(s);
		}
		
		void write(LittleEndianDataOutputStream s) throws IOException {
			s.writeInt(iden);
			ByteArrayOutputStream oss = new ByteArrayOutputStream();
			write_contents(new LittleEndianDataOutputStream(oss));
			
			// Comment Ruben: It seems redundant to send the size twice (when sending a String, LittleEndianness should not change the size I think)
			// Also storing the intermediate results in another buffer can be avoided I think, why not send the original s variable to write_contents?
			s.writeInt(oss.size());
			s.write(oss.toByteArray());
			s.flush();
		}
		
		Command(int iden) {
			this.iden = iden;
		}
		
		protected String readString(LittleEndianDataInputStream s) throws IOException {
			int len = s.readInt();
			byte[] b = new byte[len];
			s.readFully(b);
			String str = new String(b);
			while (len++ % 4 != 0) s.read();
			return str;
		}
		
		protected float[] readFloatArray(LittleEndianDataInputStream s) throws IOException {
			int len = s.readInt() / 4;
			float[] fs = new float[len];
			for (int i = 0; i < len; ++i) {
				fs[i] = s.readFloat();
			}
			return fs;
		}
		
		protected int[] readIntArray(LittleEndianDataInputStream s) throws IOException {
			int len = s.readInt() / 4;
			int[] is = new int[len];
			for (int i = 0; i < len; ++i) {
				is[i] = s.readInt();
			}
			return is;
		}
		
		protected void writeString(LittleEndianDataOutputStream s, String str) throws IOException {
			byte[] b = str.getBytes(Charsets.UTF_8);
			int len = b.length;
			s.writeInt(len);
			s.write(b);
			while (len++ % 4 != 0) s.write(0);
		}

		protected void writeStringBinary(LittleEndianDataOutputStream s, byte[] data) throws IOException {
			int len = data.length;
			s.writeInt(len);
			s.write(data);
			while (len++ % 4 != 0) s.write(0);
		}
	}
	
	static class Hello extends Command {
		private String string;
		
		public String getString() {
			return string;
		}
		
		Hello() {
			super(HELLO);
		}

		@Override
		void read_contents(LittleEndianDataInputStream s) throws IOException {
			string = readString(s);
		}

		@Override
		void write_contents(LittleEndianDataOutputStream s) {
			throw new UnsupportedOperationException();
		}
	}
	
	static class More extends Command {
		private Boolean more;
		
		public Boolean hasMore() {
			return more;
		}
		
		More() {
			super(MORE);
		}

		@Override
		void read_contents(LittleEndianDataInputStream s) throws IOException {
			more = s.readInt() == 1;
		}

		@Override
		void write_contents(LittleEndianDataOutputStream s) {
			throw new UnsupportedOperationException();
		}
	}
	
	static class IfcModel extends Command {
		private InputStream ifcInputStream;
		
		IfcModel(InputStream ifcInputStream) {
			super(IFC_MODEL);
			this.ifcInputStream = ifcInputStream;
		}

		@Override
		void read_contents(LittleEndianDataInputStream s) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		void write_contents(LittleEndianDataOutputStream s) throws IOException {
			// This is now the point where memory problems will arise for large models
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(ifcInputStream, baos);
			writeStringBinary(s, baos.toByteArray());
		}
	}
	
	static class Get extends Command {
		Get() {
			super(GET);
		}

		@Override
		void read_contents(LittleEndianDataInputStream s) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		void write_contents(LittleEndianDataOutputStream s) {}
	}
	
	static class Next extends Command {
		Next() {
			super(NEXT);
		}

		@Override
		void read_contents(LittleEndianDataInputStream s) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		void write_contents(LittleEndianDataOutputStream s) {}
	}
	
	static class Bye extends Command {
		Bye() {
			super(BYE);
		}

		@Override
		void read_contents(LittleEndianDataInputStream s) throws IOException {}

		@Override
		void write_contents(LittleEndianDataOutputStream s) {}
	}
	
	static class Entity extends Command {
		private IfcGeomServerClientEntity entity;

		Entity() {
			super(ENTITY);
		}

		@Override
		void read_contents(LittleEndianDataInputStream s) throws IOException {
			entity = new IfcGeomServerClientEntity(
				s.readInt(),
				readString(s),
				readString(s),
				readString(s),
				s.readInt(),
				readFloatArray(s),
				s.readInt(),
				readFloatArray(s),
				readFloatArray(s),
				readIntArray(s),
				readFloatArray(s),
				readIntArray(s)
			);
		}
		
		public IfcGeomServerClientEntity getEntity() {
			return entity;
		}

		@Override
		void write_contents(LittleEndianDataOutputStream s) {
			throw new UnsupportedOperationException();
		}
	}
	
	static class GetLog extends Command {
		GetLog() {
			super(GET_LOG);
		}

		@Override
		void read_contents(LittleEndianDataInputStream s) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		void write_contents(LittleEndianDataOutputStream s) {}
	}
	
	static class Log extends Command {
		private String string;
		
		Log() {
			super(LOG);
		}

		@Override
		void read_contents(LittleEndianDataInputStream s) throws IOException {
			string = readString(s);
		}

		@Override
		void write_contents(LittleEndianDataOutputStream s) {
			throw new UnsupportedOperationException();
		}
		
		public String getString() {
			return string;
		}
	}
	
	private void terminate() {
		hasMore = false;
		if (process == null) return;
		
		// Try and get the conversion log and say goodbye to the executable
		try {
			GetLog gl = new GetLog();
			gl.write(dos);
			
			if (dis.readInt() != LOG) {
				LOGGER.error("Invalid command sequence encountered");
				throw new IOException();
			}
			
			Log lg = new Log();
			lg.read(dis);
			
			final String log = lg.getString().trim();
			if (log.length() > 0) {
				LOGGER.info("\n" + log);
			}
			
			Bye b = new Bye();
			b.write(dos);
			
			if (dis.readInt() != BYE) {
				LOGGER.error("Invalid command sequence encountered");
				throw new IOException();
			}			
			b.read(dis);
		} catch (IOException e) {}						

		// Give the executable some time to terminate by itself or kill
		// it after 2 seconds have passed
		for (int n = 0;;) {
			try {
				if (process.exitValue() != 0) {
					LOGGER.error(String.format("Exited with non-zero exit code: %d", process.exitValue()));
				}
				break;
			} catch (IllegalThreadStateException e) {
				if (n++ == 20) {
					process.destroy();
					LOGGER.error("Forcefully terminated IfcOpenShell process");
					break;
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		
		dis = null;
		dos = null;
		process = null;
	}
	
	private void askForMore() throws IOException {
		hasMore = false;
		if (dis.readInt() != MORE) {
			LOGGER.error("Invalid command sequence encountered");
			throw new IOException();
		}
	
		More mr = new More(); 
		mr.read(dis);
		
		hasMore = mr.hasMore();
	}
	
	private IfcGeomServerClientEntity getNext() {
		try {
			Get g = new Get();
			g.write(dos);
			
			if (dis.readInt() != ENTITY) {
				LOGGER.error("Invalid command sequence encountered");
				throw new IOException();
			}
			Entity e = new Entity();
			e.read(dis);
			
			Next n = new Next();
			n.write(dos);
			
			askForMore();
			
			return e.getEntity();
		} catch (IOException e) {
			terminate();
			return null;
		}
	}
}
