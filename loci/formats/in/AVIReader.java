//
// AVIReader.java
//

/*
OME Bio-Formats package for reading and converting biological file formats.
Copyright (C) 2005-@year@ UW-Madison LOCI and Glencoe Software, Inc.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package loci.formats.in;

import java.io.*;
import java.util.Vector;
import loci.formats.*;
import loci.formats.codec.*;
import loci.formats.meta.FilterMetadata;
import loci.formats.meta.MetadataStore;

/**
 * AVIReader is the file format reader for AVI files.
 *
 * Much of this code was adapted from Wayne Rasband's AVI Movie Reader
 * plugin for ImageJ (available at http://rsb.info.nih.gov/ij).
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/loci/formats/in/AVIReader.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/loci/formats/in/AVIReader.java">SVN</a></dd></dl>
 */
public class AVIReader extends FormatReader {

  // -- Constants --

  /** Supported compression types. */
  private static final int MSRLE = 1;
  private static final int MS_VIDEO = 1296126531;
  //private static final int CINEPAK = 1684633187;

  // -- Fields --

  /** Offset to each plane. */
  private Vector offsets;

  /** Number of bytes in each plane. */
  private Vector lengths;

  private String type = "error";
  private String fcc = "error";
  private int size = -1;
  private long pos;

  // Stream Format chunk fields

  private int bmpColorsUsed, bmpWidth;
  private int bmpCompression, bmpScanLineSize;
  private short bmpBitsPerPixel;
  private byte[][] lut = null;

  private byte[] lastImage;
  private int lastImageNo;

  // -- Constructor --

  /** Constructs a new AVI reader. */
  public AVIReader() {
    super("Audio Video Interleave", "avi");
    blockCheckLen = 4;
    suffixNecessary = false;
  }

  // -- IFormatReader API methods --

  /* @see loci.formats.IFormatReader#isThisType(byte[]) */
  public boolean isThisType(byte[] block) {
    if (block.length < blockCheckLen) return false;
    return block[0] == 'R' && block[1] == 'I' &&
      block[2] == 'F' && block[3] == 'F';
  }

  /**
   * @see loci.formats.IFormatReader#openBytes(int, byte[], int, int, int, int)
   */
  public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    FormatTools.assertId(currentId, true, 1);
    FormatTools.checkPlaneNumber(this, no);
    FormatTools.checkBufferSize(this, buf.length, w, h);

    int bytes = FormatTools.getBytesPerPixel(core.pixelType[0]);
    double p = ((double) bmpScanLineSize) / bmpBitsPerPixel;
    int effectiveWidth = (int) (bmpScanLineSize / p);
    if (effectiveWidth == 0 || effectiveWidth < core.sizeX[0]) {
      effectiveWidth = core.sizeX[0];
    }

    long fileOff = ((Long) offsets.get(no)).longValue();
    in.seek(fileOff);

    if (bmpCompression != 0) {
      byte[] b = uncompress(no, buf);
      int rowLen = w * bytes * core.sizeC[0];
      for (int row=0; row<h; row++) {
        System.arraycopy(b, (row + y) * core.sizeX[0] * bytes * core.sizeC[0],
          buf, row * rowLen, rowLen);
      }
      b = null;
      return buf;
    }

    if (bmpBitsPerPixel < 8) {
      int rawSize = bytes * core.sizeY[0] * effectiveWidth * core.sizeC[0];
      rawSize /= (8 / bmpBitsPerPixel);

      byte[] b = new byte[rawSize];

      int len = rawSize / core.sizeY[0];
      in.read(b);

      BitBuffer bb = new BitBuffer(b);
      bb.skipBits(bmpBitsPerPixel * len * (core.sizeY[0] - h - y));

      for (int row=h; row>=y; row--) {
        bb.skipBits(bmpBitsPerPixel * x);
        for (int col=0; col<len; col++) {
          buf[(row - y) * len + col] = (byte) bb.getBits(bmpBitsPerPixel);
        }
        bb.skipBits(bmpBitsPerPixel * (core.sizeX[0] - w - x));
      }

      return buf;
    }

    int pad = bmpScanLineSize - core.sizeX[0]*(bmpBitsPerPixel / 8);
    int scanline = w * (bmpBitsPerPixel / 8);

    in.skipBytes(scanline * (core.sizeY[0] - h - y));

    if (core.sizeX[0] == w) {
      in.read(buf);
    }
    else {
      for (int i=h - 1; i>=0; i--) {
        in.skipBytes(x * (bmpBitsPerPixel / 8));
        in.read(buf, (i - y)*scanline, scanline);
        if (bmpBitsPerPixel == 24) {
          for (int j=0; j<core.sizeX[0]; j++) {
            byte r = buf[i*scanline + j*3 + 2];
            buf[i*scanline + j*3 + 2] = buf[i*scanline + j*3];
            buf[i*scanline + j*3] = r;
          }
        }
        in.skipBytes((bmpBitsPerPixel / 8) * (core.sizeX[0] - w - x + pad));
      }
    }

    if (bmpBitsPerPixel == 16) {
      // channels are separated, need to swap them
      byte[] r = new byte[core.sizeX[0] * core.sizeY[0] * 2];
      System.arraycopy(buf, 2 * (buf.length / 3), r, 0, r.length);
      System.arraycopy(buf, 0, buf, 2 * (buf.length / 3), r.length);
      System.arraycopy(r, 0, buf, 0, r.length);
    }

    return buf;
  }

  // -- IFormatHandler API methods --

  /* @see loci.formats.IFormatHandler#close() */
  public void close() throws IOException {
    super.close();
    offsets = null;
    lengths = null;
    type = null;
    fcc = null;
    size = -1;
    pos = 0;
    bmpColorsUsed = bmpWidth = bmpCompression = bmpScanLineSize = 0;
    bmpBitsPerPixel = 0;
    lut = null;
    lastImage = null;
    lastImageNo = -1;
  }

  // -- Internal FormatReader API methods --

  /* @see loci.formats.FormatReader#initFile(String) */
  protected void initFile(String id) throws FormatException, IOException {
    if (debug) debug("AVIReader.initFile(" + id + ")");
    super.initFile(id);
    in = new RandomAccessStream(id);
    in.order(true);

    status("Verifying AVI format");

    offsets = new Vector();
    lengths = new Vector();
    lastImageNo = -1;

    String listString;

    type = in.readString(4);
    size = in.readInt();
    fcc = in.readString(4);

    if (type.equals("RIFF")) {
      if (!fcc.equals("AVI ")) {
        throw new FormatException("Sorry, AVI RIFF format not found.");
      }
    }
    else throw new FormatException("Not an AVI file");

    pos = in.getFilePointer();
    long spos = pos;

    status("Searching for image data");

    while ((in.length() - in.getFilePointer()) > 4) {
      listString = in.readString(4);
      in.seek(pos);
      if (listString.equals(" JUN")) {
        in.skipBytes(1);
        pos++;
      }

      if (listString.equals("JUNK")) {
        type = in.readString(4);
        size = in.readInt();

        if (type.equals("JUNK")) {
          in.skipBytes(size);
        }
      }
      else if (listString.equals("LIST")) {
        spos = in.getFilePointer();
        type = in.readString(4);
        size = in.readInt();
        fcc = in.readString(4);

        in.seek(spos);
        if (fcc.equals("hdrl")) {
          type = in.readString(4);
          size = in.readInt();
          fcc = in.readString(4);

          if (type.equals("LIST")) {
            if (fcc.equals("hdrl")) {
              type = in.readString(4);
              size = in.readInt();
              if (type.equals("avih")) {
                spos = in.getFilePointer();

                addMeta("Microseconds per frame", new Integer(in.readInt()));
                addMeta("Max. bytes per second", new Integer(in.readInt()));

                in.skipBytes(8);

                addMeta("Total frames", new Integer(in.readInt()));
                addMeta("Initial frames", new Integer(in.readInt()));

                in.skipBytes(8);
                core.sizeX[0] = in.readInt();

                addMeta("Frame height", new Integer(in.readInt()));
                addMeta("Scale factor", new Integer(in.readInt()));
                addMeta("Frame rate", new Integer(in.readInt()));
                addMeta("Start time", new Integer(in.readInt()));
                addMeta("Length", new Integer(in.readInt()));

                addMeta("Frame width", new Integer(core.sizeX[0]));

                if (spos + size <= in.length()) {
                  in.seek(spos + size);
                }
              }
            }
          }
        }
        else if (fcc.equals("strl")) {
          long startPos = in.getFilePointer();
          long streamSize = size;

          type = in.readString(4);
          size = in.readInt();
          fcc = in.readString(4);

          if (type.equals("LIST")) {
            if (fcc.equals("strl")) {
              type = in.readString(4);
              size = in.readInt();

              if (type.equals("strh")) {
                spos = in.getFilePointer();
                in.skipBytes(40);

                addMeta("Stream quality", new Integer(in.readInt()));
                addMeta("Stream sample size", new Integer(in.readInt()));

                if (spos + size <= in.length()) {
                  in.seek(spos + size);
                }
              }

              type = in.readString(4);
              size = in.readInt();
              if (type.equals("strf")) {
                spos = in.getFilePointer();

                in.skipBytes(4);
                bmpWidth = in.readInt();
                core.sizeY[0] = in.readInt();
                in.skipBytes(2);
                bmpBitsPerPixel = in.readShort();
                bmpCompression = in.readInt();
                in.skipBytes(4);

                addMeta("Horizontal resolution", new Integer(in.readInt()));
                addMeta("Vertical resolution", new Integer(in.readInt()));

                bmpColorsUsed = in.readInt();
                in.skipBytes(4);

                addMeta("Bitmap compression value",
                  new Integer(bmpCompression));
                addMeta("Number of colors used", new Integer(bmpColorsUsed));
                addMeta("Bits per pixel", new Integer(bmpBitsPerPixel));

                // scan line is padded with zeros to be a multiple of 4 bytes
                int npad = bmpWidth % 4;
                if (npad > 0) npad = 4 - npad;

                bmpScanLineSize = (bmpWidth + npad) * (bmpBitsPerPixel / 8);

                int bmpActualColorsUsed = 0;
                if (bmpColorsUsed != 0) {
                  bmpActualColorsUsed = bmpColorsUsed;
                }
                else if (bmpBitsPerPixel < 16) {
                  // a value of 0 means we determine this based on the
                  // bits per pixel
                  bmpActualColorsUsed = 1 << bmpBitsPerPixel;
                  bmpColorsUsed = bmpActualColorsUsed;
                }

                if (bmpCompression != MSRLE && bmpCompression != 0 &&
                  bmpCompression != MS_VIDEO/* && bmpCompression != CINEPAK*/)
                {
                  throw new FormatException("Unsupported compression type " +
                    bmpCompression);
                }

                if (!(bmpBitsPerPixel == 4 || bmpBitsPerPixel == 8 ||
                  bmpBitsPerPixel == 24 || bmpBitsPerPixel == 16 ||
                  bmpBitsPerPixel == 32))
                {
                  throw new FormatException(bmpBitsPerPixel +
                    " bits per pixel not supported");
                }

                if (bmpActualColorsUsed != 0) {
                  // read the palette
                  lut = new byte[3][bmpColorsUsed];

                  for (int i=0; i<bmpColorsUsed; i++) {
                    lut[2][i] = in.readByte();
                    lut[1][i] = in.readByte();
                    lut[0][i] = in.readByte();
                    in.skipBytes(1);
                  }
                }

                in.seek(spos + size);
              }
            }

            spos = in.getFilePointer();
            type = in.readString(4);
            size = in.readInt();
            if (type.equals("strd")) {
              in.skipBytes(size);
            }
            else {
              in.seek(spos);
            }

            spos = in.getFilePointer();
            type = in.readString(4);
            size = in.readInt();
            if (type.equals("strn")) {
              in.skipBytes(size);
            }
            else {
              in.seek(spos);
            }
          }

          if (startPos + streamSize + 8 <= in.length()) {
            in.seek(startPos + 8 + streamSize);
          }
        }
        else if (fcc.equals("movi")) {
          type = in.readString(4);
          size = in.readInt();
          fcc = in.readString(4);

          if (type.equals("LIST")) {
            if (fcc.equals("movi")) {
              spos = in.getFilePointer();
              if (spos >= in.length() - 12) break;
              type = in.readString(4);
              size = in.readInt();
              fcc = in.readString(4);
              if (!(type.equals("LIST") && (fcc.equals("rec ") ||
                fcc.equals("movi"))))
              {
                in.seek(spos);
              }

              spos = in.getFilePointer();
              type = in.readString(4);
              if (type.startsWith("ix")) {
                size = in.readInt();
                in.skipBytes(size);
                type = in.readString(4);
                size = in.readInt();
              }
              else {
                size = in.readInt();
              }

              while (type.substring(2).equals("db") ||
                type.substring(2).equals("dc") ||
                type.substring(2).equals("wb"))
              {
                if (type.substring(2).equals("db") ||
                  type.substring(2).equals("dc"))
                {
                  offsets.add(new Long(in.getFilePointer()));
                  lengths.add(new Long(size));
                  in.skipBytes(size);
                }

                spos = in.getFilePointer();

                type = in.readString(4);
                size = in.readInt();
                if (type.equals("JUNK")) {
                  in.skipBytes(size);
                  spos = in.getFilePointer();
                  type = in.readString(4);
                  size = in.readInt();
                }
              }
              in.seek(spos);
            }
          }
        }
        else {
          // skipping unknown block
          try {
            in.skipBytes(8 + size);
          }
          catch (IllegalArgumentException iae) { }
        }
      }
      else {
        // skipping unknown block
        type = in.readString(4);
        if (in.getFilePointer() + size + 4 <= in.length()) {
          size = in.readInt();
          in.skipBytes(size);
        }
      }
      pos = in.getFilePointer();
    }
    status("Populating metadata");

    core.imageCount[0] = offsets.size();

    core.rgb[0] = bmpBitsPerPixel > 8 || (bmpCompression != 0);
    core.indexed[0] = false;
    core.sizeZ[0] = 1;
    core.sizeT[0] = core.imageCount[0];
    core.littleEndian[0] = true;
    core.interleaved[0] = bmpBitsPerPixel != 16;
    core.sizeC[0] = core.rgb[0] ? 3 : 1;
    core.currentOrder[0] = core.sizeC[0] == 3 ? "XYCTZ" : "XYTCZ";
    core.falseColor[0] = false;
    core.metadataComplete[0] = true;

    if (bmpBitsPerPixel <= 8) core.pixelType[0] = FormatTools.UINT8;
    else if (bmpBitsPerPixel == 16) core.pixelType[0] = FormatTools.UINT16;
    else if (bmpBitsPerPixel == 32) core.pixelType[0] = FormatTools.UINT32;
    else if (bmpBitsPerPixel == 24) core.pixelType[0] = FormatTools.UINT8;
    else {
      throw new FormatException(
          "Unknown matching for pixel bit width of: " + bmpBitsPerPixel);
    }

    if (bmpCompression != 0) core.pixelType[0] = FormatTools.UINT8;

    MetadataStore store =
      new FilterMetadata(getMetadataStore(), isMetadataFiltered());
    store.setImageName("", 0);
    store.setImageCreationDate(
      DataTools.convertDate(System.currentTimeMillis(), DataTools.UNIX), 0);
    MetadataTools.populatePixels(store, this);

    // CTR CHECK
//    for (int i=0; i<core.sizeC[0]; i++) {
//      store.setLogicalChannel(i, null, null, null, null, null, null, null, null,
//        null, null, null, null, null,
//        null, null, null, null, null, null, null, null, null, null, null);
//    }
  }

  // -- Helper methods --

  private byte[] uncompress(int no, byte[] buf)
    throws FormatException, IOException
  {
    byte[] b = new byte[(int) ((Long) lengths.get(no)).longValue()];
    in.read(b);
    if (bmpCompression == MSRLE) {
      Object[] options = new Object[2];
      options[1] = (lastImageNo == no - 1) ? lastImage : null;
      options[0] = new int[] {core.sizeX[0], core.sizeY[0]};
      MSRLECodec codec = new MSRLECodec();
      buf = codec.decompress(b, options);
      lastImage = buf;
      lastImageNo = no;
    }
    else if (bmpCompression == MS_VIDEO) {
      Object[] options = new Object[4];
      options[0] = new Integer(bmpBitsPerPixel);
      options[1] = new Integer(core.sizeX[0]);
      options[2] = new Integer(core.sizeY[0]);
      options[3] = (lastImageNo == no - 1) ? lastImage : null;

      MSVideoCodec codec = new MSVideoCodec();
      buf = codec.decompress(b, options);
      lastImage = buf;
      lastImageNo = no;
    }
    /*
    else if (bmpCompression == CINEPAK) {
      Object[] options = new Object[2];
      options[0] = new Integer(bmpBitsPerPixel);
      options[1] = lastImage;

      CinepakCodec codec = new CinepakCodec();
      buf = codec.decompress(b, options);
      lastImage = buf;
      if (no == core.imageCount[0] - 1) lastImage = null;
      return buf;
    }
    */
    else {
      throw new FormatException("Unsupported compression : " + bmpCompression);
    }
    if (lut != null) {
      b = buf;
      buf = new byte[b.length * core.sizeC[0]];
      for (int i=0; i<b.length; i++) {
        buf[i*core.sizeC[0]] = lut[0][b[i] & 0xff];
        buf[i*core.sizeC[0] + 1] = lut[1][b[i] & 0xff];
        buf[i*core.sizeC[0] + 2] = lut[2][b[i] & 0xff];
      }
    }
    return buf;
  }

}
