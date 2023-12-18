package com.savemaster.savefromfb.streams;

import com.savemaster.savefromfb.streams.io.SharpStream;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class Mp4DashReader {
	
	private static final int ATOM_MOOF = 0x6D6F6F66;
	private static final int ATOM_MFHD = 0x6D666864;
	private static final int ATOM_TRAF = 0x74726166;
	private static final int ATOM_TFHD = 0x74666864;
	private static final int ATOM_TFDT = 0x74666474;
	private static final int ATOM_TRUN = 0x7472756E;
	private static final int ATOM_MDIA = 0x6D646961;
	private static final int ATOM_FTYP = 0x66747970;
	private static final int ATOM_SIDX = 0x73696478;
	private static final int ATOM_MOOV = 0x6D6F6F76;
	private static final int ATOM_MDAT = 0x6D646174;
	private static final int ATOM_MVHD = 0x6D766864;
	private static final int ATOM_TRAK = 0x7472616B;
	private static final int ATOM_MVEX = 0x6D766578;
	private static final int ATOM_TREX = 0x74726578;
	private static final int ATOM_TKHD = 0x746B6864;
	private static final int ATOM_MFRA = 0x6D667261;
	private static final int ATOM_MDHD = 0x6D646864;
	private static final int ATOM_EDTS = 0x65647473;
	private static final int ATOM_ELST = 0x656C7374;
	private static final int ATOM_HDLR = 0x68646C72;
	private static final int ATOM_MINF = 0x6D696E66;
	private static final int ATOM_DINF = 0x64696E66;
	private static final int ATOM_STBL = 0x7374626C;
	private static final int ATOM_STSD = 0x73747364;
	private static final int ATOM_VMHD = 0x766D6864;
	private static final int ATOM_SMHD = 0x736D6864;
	
	private static final int BRAND_DASH = 0x64617368;
	private static final int BRAND_ISO5 = 0x69736F35;
	
	private static final int HANDLER_VIDE = 0x76696465;
	private static final int HANDLER_SOUN = 0x736F756E;
	private static final int HANDLER_SUBT = 0x73756274;
	
	private final DataReader stream;
	
	private Mp4Track[] tracks = null;
	private int[] brands = null;
	
	private Box box;
	private Moof moof;
	
	private boolean chunkZero = false;
	
	private int selectedTrack = -1;
	private Box backupBox = null;
	
	public enum TrackKind {
		Audio, Video, Subtitles, Other
	}
	
	public Mp4DashReader(SharpStream source) {
		this.stream = new DataReader(source);
	}
	
	public void parse() throws IOException, NoSuchElementException {
		if (selectedTrack > -1) {
			return;
		}
		
		box = readBox(ATOM_FTYP);
		brands = parse_ftyp(box);
		switch (brands[0]) {
			case BRAND_DASH:
			case BRAND_ISO5:
				break;
			default:
				throw new NoSuchElementException("Not a MPEG-4 DASH container, major brand is not 'dash' or 'iso5' is " + boxName(brands[0]));
		}
		
		Moov moov = null;
		int i;
		
		while (box.type != ATOM_MOOF) {
			ensure(box);
			box = readBox();
			
			switch (box.type) {
				case ATOM_MOOV:
					moov = parse_moov(box);
					break;
				case ATOM_SIDX:
					break;
				case ATOM_MFRA:
					break;
			}
		}
		
		if (moov == null) {
			throw new IOException("The provided Mp4 doesn't have the 'moov' box");
		}
		
		tracks = new Mp4Track[moov.trak.length];
		
		for (i = 0; i < tracks.length; i++) {
			tracks[i] = new Mp4Track();
			tracks[i].trak = moov.trak[i];
			
			if (moov.mvex_trex != null) {
				for (Trex mvex_trex : moov.mvex_trex) {
					if (tracks[i].trak.tkhd.trackId == mvex_trex.trackId) {
						tracks[i].trex = mvex_trex;
					}
				}
			}
			
			switch (moov.trak[i].mdia.hdlr.subType) {
				case HANDLER_VIDE:
					tracks[i].kind = TrackKind.Video;
					break;
				case HANDLER_SOUN:
					tracks[i].kind = TrackKind.Audio;
					break;
				case HANDLER_SUBT:
					tracks[i].kind = TrackKind.Subtitles;
					break;
				default:
					tracks[i].kind = TrackKind.Other;
					break;
			}
		}
		
		backupBox = box;
	}
	
	Mp4Track selectTrack(int index) {
		selectedTrack = index;
		return tracks[index];
	}
	
	/**
	 * Count all fragments present. This operation requires a seekable stream
	 *
	 * @return list with a basic info
	 * @throws IOException if the source stream is not seekeable
	 */
	int getFragmentsCount() throws IOException {
		if (selectedTrack < 0) {
			throw new IllegalStateException("track no selected");
		}
		if (!stream.canRewind()) {
			throw new IOException("The provided stream doesn't allow seek");
		}
		
		Box tmp;
		int count = 0;
		
		if (box.type == ATOM_MOOF) {
			tmp = box;
		}
		else {
			ensure(box);
			tmp = readBox();
		}
		
		do {
			if (tmp.type == ATOM_MOOF) {
				ensure(readBox(ATOM_MFHD));
				Box traf;
				while ((traf = untilBox(tmp, ATOM_TRAF)) != null) {
					Box tfhd = readBox(ATOM_TFHD);
					if (parse_tfhd(tracks[selectedTrack].trak.tkhd.trackId) != null) {
						count++;
						break;
					}
					ensure(tfhd);
					ensure(traf);
				}
			}
			ensure(tmp);
		} while (stream.available() && (tmp = readBox()) != null);
		
		rewind();
		
		return count;
	}
	
	public int[] getBrands() {
		if (brands == null) throw new IllegalStateException("Not parsed");
		return brands;
	}
	
	public void rewind() throws IOException {
		if (!stream.canRewind()) {
			throw new IOException("The provided stream doesn't allow seek");
		}
		if (box == null) {
			return;
		}
		
		box = backupBox;
		chunkZero = false;
		
		stream.rewind();
		stream.skipBytes(backupBox.offset + (DataReader.INTEGER_SIZE * 2));
	}
	
	public Mp4Track[] getAvailableTracks() {
		return tracks;
	}
	
	public Mp4DashChunk getNextChunk(boolean infoOnly) throws IOException {
		Mp4Track track = tracks[selectedTrack];
		
		while (stream.available()) {
			
			if (chunkZero) {
				ensure(box);
				if (!stream.available()) {
					break;
				}
				box = readBox();
			}
			else {
				chunkZero = true;
			}
			
			switch (box.type) {
				case ATOM_MOOF:
					if (moof != null) {
						throw new IOException("moof found without mdat");
					}
					
					moof = parse_moof(box, track.trak.tkhd.trackId);
					
					if (moof.traf != null) {
						
						if (hasFlag(moof.traf.trun.bFlags, 0x0001)) {
							moof.traf.trun.dataOffset -= box.size + 8;
							if (moof.traf.trun.dataOffset < 0) {
								throw new IOException("trun box has wrong data offset, points outside of concurrent mdat box");
							}
						}
						
						if (moof.traf.trun.chunkSize < 1) {
							if (hasFlag(moof.traf.tfhd.bFlags, 0x10)) {
								moof.traf.trun.chunkSize = moof.traf.tfhd.defaultSampleSize * moof.traf.trun.entryCount;
							}
							else {
								moof.traf.trun.chunkSize = (int) (box.size - 8);
							}
						}
						if (!hasFlag(moof.traf.trun.bFlags, 0x900) && moof.traf.trun.chunkDuration == 0) {
							if (hasFlag(moof.traf.tfhd.bFlags, 0x20)) {
								moof.traf.trun.chunkDuration = moof.traf.tfhd.defaultSampleDuration * moof.traf.trun.entryCount;
							}
						}
					}
					break;
				case ATOM_MDAT:
					if (moof == null) {
						throw new IOException("mdat found without moof");
					}
					
					if (moof.traf == null) {
						moof = null;
						// find another chunk
						continue;
					}
					
					Mp4DashChunk chunk = new Mp4DashChunk();
					chunk.moof = moof;
					if (!infoOnly) {
						chunk.data = stream.getView(moof.traf.trun.chunkSize);
					}
					
					moof = null;
					
					stream.skipBytes(chunk.moof.traf.trun.dataOffset);
					return chunk;
				default:
			}
		}
		
		return null;
	}
	
	public static boolean hasFlag(int flags, int mask) {
		return (flags & mask) == mask;
	}
	
	private String boxName(Box ref) {
		return boxName(ref.type);
	}
	
	private String boxName(int type) {
		try {
			return new String(ByteBuffer.allocate(4).putInt(type).array(), "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			return "0x" + Integer.toHexString(type);
		}
	}
	
	private Box readBox() throws IOException {
		Box b = new Box();
		b.offset = stream.position();
		b.size = stream.readUnsignedInt();
		b.type = stream.readInt();
		
		if (b.size == 1) {
			b.size = stream.readLong();
		}
		
		return b;
	}
	
	private Box readBox(int expected) throws IOException {
		Box b = readBox();
		if (b.type != expected) {
			throw new NoSuchElementException("expected " + boxName(expected) + " found " + boxName(b));
		}
		return b;
	}
	
	private byte[] readFullBox(Box ref) throws IOException {
		// full box reading is limited to 2 GiB, and should be enough
		int size = (int) ref.size;
		
		ByteBuffer buffer = ByteBuffer.allocate(size);
		buffer.putInt(size);
		buffer.putInt(ref.type);
		
		int read = size - 8;
		
		if (stream.read(buffer.array(), 8, read) != read) {
			throw new EOFException(String.format("EOF reached in box: type=%s offset=%s size=%s", boxName(ref.type), ref.offset, ref.size));
		}
		
		return buffer.array();
	}
	
	private void ensure(Box ref) throws IOException {
		long skip = ref.offset + ref.size - stream.position();
		
		if (skip == 0) {
			return;
		}
		else if (skip < 0) {
			throw new EOFException(String.format("parser go beyond limits of the box. type=%s offset=%s size=%s position=%s", boxName(ref), ref.offset, ref.size, stream.position()));
		}
		
		stream.skipBytes((int) skip);
	}
	
	private Box untilBox(Box ref, int... expected) throws IOException {
		Box b;
		while (stream.position() < (ref.offset + ref.size)) {
			b = readBox();
			for (int type : expected) {
				if (b.type == type) {
					return b;
				}
			}
			ensure(b);
		}
		
		return null;
	}
	
	private Box untilAnyBox(Box ref) throws IOException {
		if (stream.position() >= (ref.offset + ref.size)) {
			return null;
		}
		
		return readBox();
	}
	
	private Moof parse_moof(Box ref, int trackId) throws IOException {
		Moof obj = new Moof();
		
		Box b = readBox(ATOM_MFHD);
		obj.mfhd_SequenceNumber = parse_mfhd();
		ensure(b);
		
		while ((b = untilBox(ref, ATOM_TRAF)) != null) {
			obj.traf = parse_traf(b, trackId);
			ensure(b);
			
			if (obj.traf != null) {
				return obj;
			}
		}
		
		return obj;
	}
	
	private int parse_mfhd() throws IOException {
		// version
		// flags
		stream.skipBytes(4);
		
		return stream.readInt();
	}
	
	private Traf parse_traf(Box ref, int trackId) throws IOException {
		Traf traf = new Traf();
		
		Box b = readBox(ATOM_TFHD);
		traf.tfhd = parse_tfhd(trackId);
		ensure(b);
		
		if (traf.tfhd == null) {
			return null;
		}
		
		b = untilBox(ref, ATOM_TRUN, ATOM_TFDT);
		
		if (b.type == ATOM_TFDT) {
			traf.tfdt = parse_tfdt();
			ensure(b);
			b = readBox(ATOM_TRUN);
		}
		
		traf.trun = parse_trun();
		ensure(b);
		
		return traf;
	}
	
	private Tfhd parse_tfhd(int trackId) throws IOException {
		Tfhd obj = new Tfhd();
		
		obj.bFlags = stream.readInt();
		obj.trackId = stream.readInt();
		
		if (trackId != -1 && obj.trackId != trackId) {
			return null;
		}
		
		if (hasFlag(obj.bFlags, 0x01)) {
			stream.skipBytes(8);
		}
		if (hasFlag(obj.bFlags, 0x02)) {
			stream.skipBytes(4);
		}
		if (hasFlag(obj.bFlags, 0x08)) {
			obj.defaultSampleDuration = stream.readInt();
		}
		if (hasFlag(obj.bFlags, 0x10)) {
			obj.defaultSampleSize = stream.readInt();
		}
		if (hasFlag(obj.bFlags, 0x20)) {
			obj.defaultSampleFlags = stream.readInt();
		}
		
		return obj;
	}
	
	private long parse_tfdt() throws IOException {
		int version = stream.read();
		stream.skipBytes(3);// flags
		return version == 0 ? stream.readUnsignedInt() : stream.readLong();
	}
	
	private Trun parse_trun() throws IOException {
		Trun obj = new Trun();
		obj.bFlags = stream.readInt();
		obj.entryCount = stream.readInt();// unsigned int
		
		obj.entries_rowSize = 0;
		if (hasFlag(obj.bFlags, 0x0100)) {
			obj.entries_rowSize += 4;
		}
		if (hasFlag(obj.bFlags, 0x0200)) {
			obj.entries_rowSize += 4;
		}
		if (hasFlag(obj.bFlags, 0x0400)) {
			obj.entries_rowSize += 4;
		}
		if (hasFlag(obj.bFlags, 0x0800)) {
			obj.entries_rowSize += 4;
		}
		obj.bEntries = new byte[obj.entries_rowSize * obj.entryCount];
		
		if (hasFlag(obj.bFlags, 0x0001)) {
			obj.dataOffset = stream.readInt();
		}
		if (hasFlag(obj.bFlags, 0x0004)) {
			obj.bFirstSampleFlags = stream.readInt();
		}
		
		stream.read(obj.bEntries);
		
		for (int i = 0; i < obj.entryCount; i++) {
			TrunEntry entry = obj.getEntry(i);
			if (hasFlag(obj.bFlags, 0x0100)) {
				obj.chunkDuration += entry.sampleDuration;
			}
			if (hasFlag(obj.bFlags, 0x0200)) {
				obj.chunkSize += entry.sampleSize;
			}
			if (hasFlag(obj.bFlags, 0x0800)) {
				if (!hasFlag(obj.bFlags, 0x0100)) {
					obj.chunkDuration += entry.sampleCompositionTimeOffset;
				}
			}
		}
		
		return obj;
	}
	
	private int[] parse_ftyp(Box ref) throws IOException {
		int i = 0;
		int[] list = new int[(int) ((ref.offset + ref.size - stream.position() - 4) / 4)];
		
		// major brand
		list[i++] = stream.readInt();
		// minor version
		stream.skipBytes(4);
		
		for (; i < list.length; i++) {
			// compatible brands
			list[i] = stream.readInt();
		}
		
		return list;
	}
	
	private Mvhd parse_mvhd() throws IOException {
		int version = stream.read();
		stream.skipBytes(3);// flags
		
		// creation entries_time
		// modification entries_time
		stream.skipBytes(2 * (version == 0 ? 4 : 8));
		
		Mvhd obj = new Mvhd();
		obj.timeScale = stream.readUnsignedInt();
		
		// chunkDuration
		stream.skipBytes(version == 0 ? 4 : 8);
		
		// rate
		// volume
		// reserved
		// matrix array
		// predefined
		stream.skipBytes(76);
		
		obj.nextTrackId = stream.readUnsignedInt();
		
		return obj;
	}
	
	private Tkhd parse_tkhd() throws IOException {
		int version = stream.read();
		
		Tkhd obj = new Tkhd();
		
		// flags
		// creation entries_time
		// modification entries_time
		stream.skipBytes(3 + (2 * (version == 0 ? 4 : 8)));
		
		obj.trackId = stream.readInt();
		
		// reserved
		stream.skipBytes(4);
		
		obj.duration = version == 0 ? stream.readUnsignedInt() : stream.readLong();
		
		// reserved
		stream.skipBytes(2 * 4);
		
		obj.bLayer = stream.readShort();
		obj.bAlternateGroup = stream.readShort();
		obj.bVolume = stream.readShort();
		
		// reserved
		stream.skipBytes(2);
		
		obj.matrix = new byte[9 * 4];
		stream.read(obj.matrix);
		
		obj.bWidth = stream.readInt();
		obj.bHeight = stream.readInt();
		
		return obj;
	}
	
	private Trak parse_trak(Box ref) throws IOException {
		Trak trak = new Trak();
		
		Box b = readBox(ATOM_TKHD);
		trak.tkhd = parse_tkhd();
		ensure(b);
		
		while ((b = untilBox(ref, ATOM_MDIA, ATOM_EDTS)) != null) {
			switch (b.type) {
				case ATOM_MDIA:
					trak.mdia = parse_mdia(b);
					break;
				case ATOM_EDTS:
					trak.edst_elst = parse_edts(b);
					break;
			}
			
			ensure(b);
		}
		
		return trak;
	}
	
	private Mdia parse_mdia(Box ref) throws IOException {
		Mdia obj = new Mdia();
		
		Box b;
		while ((b = untilBox(ref, ATOM_MDHD, ATOM_HDLR, ATOM_MINF)) != null) {
			switch (b.type) {
				case ATOM_MDHD:
					obj.mdhd = readFullBox(b);
					
					// read time scale
					ByteBuffer buffer = ByteBuffer.wrap(obj.mdhd);
					byte version = buffer.get(8);
					buffer.position(12 + ((version == 0 ? 4 : 8) * 2));
					obj.mdhd_timeScale = buffer.getInt();
					break;
				case ATOM_HDLR:
					obj.hdlr = parse_hdlr(b);
					break;
				case ATOM_MINF:
					obj.minf = parse_minf(b);
					break;
			}
			ensure(b);
		}
		
		return obj;
	}
	
	private Hdlr parse_hdlr(Box ref) throws IOException {
		// version
		// flags
		stream.skipBytes(4);
		
		Hdlr obj = new Hdlr();
		obj.bReserved = new byte[12];
		
		obj.type = stream.readInt();
		obj.subType = stream.readInt();
		stream.read(obj.bReserved);
		
		// component name (is a ansi/ascii string)
		stream.skipBytes((ref.offset + ref.size) - stream.position());
		
		return obj;
	}
	
	private Moov parse_moov(Box ref) throws IOException {
		Box b = readBox(ATOM_MVHD);
		Moov moov = new Moov();
		moov.mvhd = parse_mvhd();
		ensure(b);
		
		ArrayList<Trak> tmp = new ArrayList<>((int) moov.mvhd.nextTrackId);
		while ((b = untilBox(ref, ATOM_TRAK, ATOM_MVEX)) != null) {
			
			switch (b.type) {
				case ATOM_TRAK:
					tmp.add(parse_trak(b));
					break;
				case ATOM_MVEX:
					moov.mvex_trex = parse_mvex(b, (int) moov.mvhd.nextTrackId);
					break;
			}
			
			ensure(b);
		}
		
		moov.trak = tmp.toArray(new Trak[0]);
		
		return moov;
	}
	
	private Trex[] parse_mvex(Box ref, int possibleTrackCount) throws IOException {
		ArrayList<Trex> tmp = new ArrayList<>(possibleTrackCount);
		
		Box b;
		while ((b = untilBox(ref, ATOM_TREX)) != null) {
			tmp.add(parse_trex());
			ensure(b);
		}
		
		return tmp.toArray(new Trex[0]);
	}
	
	private Trex parse_trex() throws IOException {
		// version
		// flags
		stream.skipBytes(4);
		
		Trex obj = new Trex();
		obj.trackId = stream.readInt();
		obj.defaultSampleDescriptionIndex = stream.readInt();
		obj.defaultSampleDuration = stream.readInt();
		obj.defaultSampleSize = stream.readInt();
		obj.defaultSampleFlags = stream.readInt();
		
		return obj;
	}
	
	private Elst parse_edts(Box ref) throws IOException {
		Box b = untilBox(ref, ATOM_ELST);
		if (b == null) {
			return null;
		}
		
		Elst obj = new Elst();
		
		boolean v1 = stream.read() == 1;
		stream.skipBytes(3);// flags
		
		int entryCount = stream.readInt();
		if (entryCount < 1) {
			obj.bMediaRate = 0x00010000;// default media rate (1.0)
			return obj;
		}
		
		if (v1) {
			stream.skipBytes(DataReader.LONG_SIZE);// segment duration
			obj.MediaTime = stream.readLong();
			// ignore all remain entries
			stream.skipBytes((entryCount - 1) * (DataReader.LONG_SIZE * 2));
		}
		else {
			stream.skipBytes(DataReader.INTEGER_SIZE);// segment duration
			obj.MediaTime = stream.readInt();
		}
		
		obj.bMediaRate = stream.readInt();
		
		return obj;
	}
	
	private Minf parse_minf(Box ref) throws IOException {
		Minf obj = new Minf();
		
		Box b;
		while ((b = untilAnyBox(ref)) != null) {
			
			switch (b.type) {
				case ATOM_DINF:
					obj.dinf = readFullBox(b);
					break;
				case ATOM_STBL:
					obj.stbl_stsd = parse_stbl(b);
					break;
				case ATOM_VMHD:
				case ATOM_SMHD:
					obj.$mhd = readFullBox(b);
					break;
				
			}
			ensure(b);
		}
		
		return obj;
	}
	
	/**
	 * this only read the "stsd" box inside
	 */
	private byte[] parse_stbl(Box ref) throws IOException {
		Box b = untilBox(ref, ATOM_STSD);
		
		if (b == null) {
			return new byte[0];// this never should happens (missing codec startup data)
		}
		
		return readFullBox(b);
	}
	
	class Box {
		
		int type;
		long offset;
		long size;
	}
	
	public class Moof {
		
		int mfhd_SequenceNumber;
		public Traf traf;
	}
	
	public class Traf {
		
		public Tfhd tfhd;
		long tfdt;
		public Trun trun;
	}
	
	public class Tfhd {
		
		int bFlags;
		public int trackId;
		int defaultSampleDuration;
		int defaultSampleSize;
		int defaultSampleFlags;
	}
	
	class TrunEntry {
		
		int sampleDuration;
		int sampleSize;
		int sampleFlags;
		int sampleCompositionTimeOffset;
		
		boolean hasCompositionTimeOffset;
		boolean isKeyframe;
		
	}
	
	public class Trun {
		
		public int chunkDuration;
		public int chunkSize;
		
		public int bFlags;
		int bFirstSampleFlags;
		int dataOffset;
		
		public int entryCount;
		byte[] bEntries;
		int entries_rowSize;
		
		public TrunEntry getEntry(int i) {
			ByteBuffer buffer = ByteBuffer.wrap(bEntries, i * entries_rowSize, entries_rowSize);
			TrunEntry entry = new TrunEntry();
			
			if (hasFlag(bFlags, 0x0100)) {
				entry.sampleDuration = buffer.getInt();
			}
			if (hasFlag(bFlags, 0x0200)) {
				entry.sampleSize = buffer.getInt();
			}
			if (hasFlag(bFlags, 0x0400)) {
				entry.sampleFlags = buffer.getInt();
			}
			if (hasFlag(bFlags, 0x0800)) {
				entry.sampleCompositionTimeOffset = buffer.getInt();
			}
			
			entry.hasCompositionTimeOffset = hasFlag(bFlags, 0x0800);
			entry.isKeyframe = !hasFlag(entry.sampleFlags, 0x10000);
			
			return entry;
		}
		
		public TrunEntry getAbsoluteEntry(int i, Tfhd header) {
			TrunEntry entry = getEntry(i);
			
			if (!hasFlag(bFlags, 0x0100) && hasFlag(header.bFlags, 0x20)) {
				entry.sampleFlags = header.defaultSampleFlags;
			}
			
			if (!hasFlag(bFlags, 0x0200) && hasFlag(header.bFlags, 0x10)) {
				entry.sampleSize = header.defaultSampleSize;
			}
			
			if (!hasFlag(bFlags, 0x0100) && hasFlag(header.bFlags, 0x08)) {
				entry.sampleDuration = header.defaultSampleDuration;
			}
			
			if (i == 0 && hasFlag(bFlags, 0x0004)) {
				entry.sampleFlags = bFirstSampleFlags;
			}
			
			return entry;
		}
	}
	
	public class Tkhd {
		
		int trackId;
		long duration;
		short bVolume;
		int bWidth;
		int bHeight;
		byte[] matrix;
		short bLayer;
		short bAlternateGroup;
	}
	
	public class Trak {
		
		public Tkhd tkhd;
		public Elst edst_elst;
		public Mdia mdia;
		
	}
	
	class Mvhd {
		
		long timeScale;
		long nextTrackId;
	}
	
	class Moov {
		
		Mvhd mvhd;
		Trak[] trak;
		Trex[] mvex_trex;
	}
	
	public class Trex {
		
		private int trackId;
		int defaultSampleDescriptionIndex;
		int defaultSampleDuration;
		int defaultSampleSize;
		int defaultSampleFlags;
	}
	
	public class Elst {
		
		public long MediaTime;
		public int bMediaRate;
	}
	
	public class Mdia {
		
		public int mdhd_timeScale;
		public byte[] mdhd;
		public Hdlr hdlr;
		public Minf minf;
	}
	
	public class Hdlr {
		
		public int type;
		public int subType;
		public byte[] bReserved;
	}
	
	public class Minf {
		
		public byte[] dinf;
		public byte[] stbl_stsd;
		public byte[] $mhd;
	}
	
	public class Mp4Track {
		
		public TrackKind kind;
		public Trak trak;
		public Trex trex;
	}
	
	public class Mp4DashChunk {
		
		public InputStream data;
		public Moof moof;
		private int i = 0;
		
		public TrunEntry getNextSampleInfo() {
			if (i >= moof.traf.trun.entryCount) {
				return null;
			}
			return moof.traf.trun.getAbsoluteEntry(i++, moof.traf.tfhd);
		}
		
		public Mp4DashSample getNextSample() throws IOException {
			if (data == null) {
				throw new IllegalStateException("This chunk has info only");
			}
			if (i >= moof.traf.trun.entryCount) {
				return null;
			}
			
			Mp4DashSample sample = new Mp4DashSample();
			sample.info = moof.traf.trun.getAbsoluteEntry(i++, moof.traf.tfhd);
			sample.data = new byte[sample.info.sampleSize];
			
			if (data.read(sample.data) != sample.info.sampleSize) {
				throw new EOFException("EOF reached while reading a sample");
			}
			
			return sample;
		}
	}
	
	public class Mp4DashSample {
		
		public TrunEntry info;
		public byte[] data;
	}
	
}
